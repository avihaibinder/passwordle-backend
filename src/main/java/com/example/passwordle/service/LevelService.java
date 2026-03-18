package com.example.passwordle.service;

import com.example.passwordle.dto.DailyLevelRequest;
import com.example.passwordle.dto.DailyLevelResponse;
import com.example.passwordle.exception.LevelNotFoundException;
import com.example.passwordle.model.Level;
import com.example.passwordle.model.LevelResult;
import com.example.passwordle.model.SkeletonLevel;
import com.example.passwordle.dto.DailyLevelResultRequest;
import com.example.passwordle.dto.DailyLevelMetadataResponse;
import com.example.passwordle.dao.DailyLevelDao;
import com.example.passwordle.model.DailyLevel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class LevelService {

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final DailyLevelDao dailyLevelDao;
    private final Map<Integer, SkeletonLevel> skeletonLevelCache = new ConcurrentHashMap<>();
    private final Map<LocalDate, DailyLevelResponse> dailyLevelCache = new ConcurrentHashMap<>();
    private final Map<LocalDate, LevelResult> levelResultCache = new ConcurrentHashMap<>();

    public LevelService(ObjectMapper objectMapper, ResourceLoader resourceLoader, DailyLevelDao dailyLevelDao) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.dailyLevelDao = dailyLevelDao;
    }

    private SkeletonLevel getSkeletonLevel(int levelNumber) {
        log.info("Loading skeleton level: {}", levelNumber);
        return skeletonLevelCache.computeIfAbsent(levelNumber, this::loadSkeletonLevelFromFile);
    }

    private SkeletonLevel loadSkeletonLevelFromFile(Integer levelNumber) {
        log.info("Loading skeleton level from file: {}", levelNumber);
        Resource resource = resourceLoader.getResource("classpath:skeleton_levels/" + levelNumber + ".json");

        if (!resource.exists()) {
            log.error("Skeleton level file not found: {}", levelNumber);
            throw new LevelNotFoundException("Level " + levelNumber + " not found!");
        }

        try {
            SkeletonLevel sl = objectMapper.readValue(resource.getInputStream(), SkeletonLevel.class);
            log.info("Loaded skeleton level {}: rows={}, password={}, numbersEachRow={}",
                    levelNumber, sl.getSkeletonLevelRows().size(), sl.getPassword(), sl.getNumbersEachRow());
            return sl;
        } catch (IOException e) {
            log.error("Failed to deserialize skeleton level {}: {}", levelNumber, e.getMessage(), e);
            throw new RuntimeException("Failed to load SkeletonLevel from file for level: " + levelNumber, e);
        }
    }

    public DailyLevelResponse getDailyLevel() {
        LocalDate today = LocalDate.now();
        log.info("getDailyLevel called, today: {}, cache contains key: {}", today, dailyLevelCache.containsKey(today));

        DailyLevelResponse response = dailyLevelCache.get(today);
        if (response != null) {
            log.info("Returning cached daily level for today: {}, id: {}", today, response.getId());
            return response;
        }

        log.info("No daily level in cache for today ({}), checking DynamoDB", today);
        String dailyId = today.toString();
        java.util.Optional<DailyLevel> dbLevelOpt = dailyLevelDao.getById(dailyId);
        if (dbLevelOpt.isPresent()) {
            log.info("Found daily level in DynamoDB for today ({})", today);
            DailyLevelResponse dbResponse = new DailyLevelResponse(dbLevelOpt.get().getLevelId(),
                    dbLevelOpt.get().getLevel());
            dailyLevelCache.put(today, dbResponse);
            return dbResponse;
        } else {
            log.info("No daily level found for today ({})", today);
            throw new LevelNotFoundException("Daily level not found for date: " + today);
        }
    }

    public DailyLevelResponse createDailyLevel(DailyLevelRequest request) {
        LocalDate date = resolveDate(request);
        return createDailyLevel(date);
    }

    public DailyLevelResponse createDailyLevel(LocalDate date) {
        log.info("createDailyLevel for date: {}", date);
        return dailyLevelCache.computeIfAbsent(date, this::generateNewDailyLevel);
    }

    private DailyLevelResponse generateNewDailyLevel(LocalDate date) {
        int randomSkeletonId = ThreadLocalRandom.current().nextInt(50);
        log.info("Generating new daily level for date: {}, using skeleton id: {}", date, randomSkeletonId);

        SkeletonLevel skeletonLevel = getSkeletonLevel(randomSkeletonId);
        Level level = Level.ofSkeletonLevel(skeletonLevel);
        log.info("Generated level with {} rows, password length: {}", level.getLevelRows().size(),
                level.getPassword().length());

        // Use the date itself as the ID for the daily level response
        if (date == null) {
            log.error("Date is null when generating daily level");
            throw new IllegalStateException("Date cannot be null when generating a daily level");
        }
        String dailyId = date.toString();

        // Save to DynamoDB first
        DailyLevel newDailyLevel = new DailyLevel(dailyId, level);
        log.info("Saving daily level to DynamoDB with id: {}", dailyId);
        dailyLevelDao.save(newDailyLevel);

        levelResultCache.putIfAbsent(date, new LevelResult(dailyId, date));
        log.info("Created daily level with id: {}", dailyId);

        return new DailyLevelResponse(dailyId, level);
    }

    public void postDailyLevelResult(DailyLevelResultRequest request) {
        log.info("postDailyLevelResult called: id={}, date={}, result={}", request.getId(), request.getDate(),
                request.getResult());
        log.info("Result field type: {}, value: '{}'",
                request.getResult() != null ? request.getResult().getClass().getName() : "null", request.getResult());

        LevelResult levelResult = levelResultCache.get(request.getDate());
        if (levelResult != null) {
            log.info("Found existing LevelResult for date: {}", request.getDate());
            if ("success".equalsIgnoreCase(request.getResult())) {
                levelResult.incrementSuccess();
                log.info("Incremented success counter");
            } else if ("failure".equalsIgnoreCase(request.getResult())) {
                levelResult.incrementFailure();
                log.info("Incremented failure counter");
            } else {
                log.warn("Unknown result value: '{}'", request.getResult());
            }
        } else {
            log.info("No existing LevelResult for date: {}, creating new one", request.getDate());
            LevelResult newRes = new LevelResult(request.getId(), request.getDate());
            if ("success".equalsIgnoreCase(request.getResult())) {
                newRes.incrementSuccess();
            } else if ("failure".equalsIgnoreCase(request.getResult())) {
                newRes.incrementFailure();
            }
            levelResultCache.put(request.getDate(), newRes);
        }
        log.info("levelResultCache size: {}", levelResultCache.size());
    }

    public DailyLevelMetadataResponse getDailyLevelMetadata(DailyLevelRequest request) {
        LocalDate date = resolveDate(request);
        log.info("getDailyLevelMetadata for date: {}", date);
        LevelResult levelResult = levelResultCache.getOrDefault(date, new LevelResult());
        float pct = levelResult.getSuccessPercentage();
        log.info("Returning metadata: id={}, date={}, successPercentage={}", levelResult.getId(), levelResult.getDate(),
                pct);
        return new DailyLevelMetadataResponse(levelResult.getId(), levelResult.getDate(), pct);
    }

    private LocalDate resolveDate(DailyLevelRequest request) {
        if (request.getDate() != null) {
            log.info("resolveDate: using request.date={}", request.getDate());
            return request.getDate();
        }
        if (request.getId() != null) {
            log.info("resolveDate: parsing date from id={}", request.getId());
            try {
                return LocalDate.parse(request.getId());
            } catch (Exception e) {
                log.error("resolveDate: failed to parse id '{}' as date: {}", request.getId(), e.getMessage());
                throw new IllegalArgumentException("Invalid id format. Expected YYYY-MM-DD", e);
            }
        }
        LocalDate today = LocalDate.now();
        log.info("resolveDate: no date or id provided, defaulting to today: {}", today);
        return today;
    }
}
