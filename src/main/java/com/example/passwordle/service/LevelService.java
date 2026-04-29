package com.example.passwordle.service;

import com.example.passwordle.dto.DailyLevelRequest;
import com.example.passwordle.dto.DailyLevelResponse;
import com.example.passwordle.exception.LevelNotFoundException;
import com.example.passwordle.model.Level;
import com.example.passwordle.model.LevelResult;
import com.example.passwordle.model.SkeletonLevel;
import com.example.passwordle.dto.DailyLevelResultRequest;
import com.example.passwordle.dto.DailyLevelMetadataResponse;
import com.example.passwordle.dto.DailyLevelReportEntry;
import com.example.passwordle.dao.DailyLevelDao;
import com.example.passwordle.dao.DailyLevelResultDao;
import com.example.passwordle.model.DailyLevel;
import com.example.passwordle.model.DailyLevelResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Service
public class LevelService {

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final DailyLevelDao dailyLevelDao;
    private final DailyLevelResultDao dailyLevelResultDao;
    private final Map<Integer, SkeletonLevel> skeletonLevelCache = new ConcurrentHashMap<>();
    private final Map<String, DailyLevelResponse> dailyLevelCache = new ConcurrentHashMap<>();
    private final Map<String, LevelResult> levelResultCache = new ConcurrentHashMap<>();

    public LevelService(ObjectMapper objectMapper, ResourceLoader resourceLoader, DailyLevelDao dailyLevelDao, DailyLevelResultDao dailyLevelResultDao) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.dailyLevelDao = dailyLevelDao;
        this.dailyLevelResultDao = dailyLevelResultDao;
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
        String dailyId = today.toString();
        log.info("getDailyLevel called, today: {}, cache contains key: {}", today, dailyLevelCache.containsKey(dailyId));

        DailyLevelResponse response = dailyLevelCache.get(dailyId);
        if (response != null) {
            log.info("Returning cached daily level for today: {}, id: {}", today, response.getId());
            return response;
        }

        log.info("No daily level in cache for today ({}), checking DynamoDB", today);
        Optional<DailyLevel> dbLevelOpt = dailyLevelDao.getById(dailyId);
        if (dbLevelOpt.isPresent()) {
            log.info("Found daily level in DynamoDB for today ({})", today);
            DailyLevelResponse dbResponse = new DailyLevelResponse(dbLevelOpt.get().getLevelId(),
                    dbLevelOpt.get().getLevel());
            dailyLevelCache.put(dailyId, dbResponse);
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
        String dailyId = date.toString();
        return dailyLevelCache.computeIfAbsent(dailyId, k -> generateNewDailyLevel(date));
    }

    private DailyLevelResponse generateNewDailyLevel(LocalDate date) {
        int randomSkeletonId = ThreadLocalRandom.current().nextInt(10, 49);
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

        levelResultCache.putIfAbsent(dailyId, new LevelResult(dailyId, date));
        log.info("Created daily level with id: {}", dailyId);

        return new DailyLevelResponse(dailyId, level);
    }

    public void postDailyLevelResult(DailyLevelResultRequest request) {
        log.info("postDailyLevelResult called: id={}, date={}, result={}", request.getId(), request.getDate(),
                request.getResult());
        log.info("Result field type: {}, value: '{}'",
                request.getResult() != null ? request.getResult().getClass().getName() : "null", request.getResult());

        String dailyId;
        LocalDate date = request.getDate();
        if (date != null) {
            dailyId = date.toString();
        } else if (request.getId() != null) {
            dailyId = request.getId();
            try {
                date = LocalDate.parse(dailyId);
            } catch (Exception e) {
                log.warn("Failed to parse date from id: {}, defaulting to today", dailyId);
                date = LocalDate.now();
            }
        } else {
            date = LocalDate.now();
            dailyId = date.toString();
        }

        final LocalDate resolvedDate = date;
        LevelResult levelResult = levelResultCache.computeIfAbsent(dailyId,
                k -> {
                    log.info("Creating new LevelResult for id: {}", k);
                    return new LevelResult(k, resolvedDate);
                });

        if ("success".equalsIgnoreCase(request.getResult())) {
            levelResult.incrementSuccess();
            log.info("Incremented success counter for id: {}", dailyId);
        } else if ("failure".equalsIgnoreCase(request.getResult())) {
            levelResult.incrementFailure();
            log.info("Incremented failure counter for id: {}", dailyId);
        } else {
            log.warn("Unknown result value: '{}'", request.getResult());
        }
        log.info("levelResultCache size: {}", levelResultCache.size());
    }

    public DailyLevelMetadataResponse getDailyLevelMetadata(DailyLevelRequest request) {
        LocalDate date = resolveDate(request);
        String dailyId = date.toString();
        log.info("getDailyLevelMetadata for date: {}", date);
        LevelResult levelResult = levelResultCache.getOrDefault(dailyId, new LevelResult(dailyId, date));
        float pct = levelResult.getSuccessPercentage();
        log.info("Returning metadata: id={}, date={}, successPercentage={}", levelResult.getId(), levelResult.getDate(),
                pct);
        return new DailyLevelMetadataResponse(levelResult.getId(), levelResult.getDate(), pct);
    }

    public List<DailyLevelReportEntry> getDailyLevelReport() {
        log.info("getDailyLevelReport called, levelResultCache size: {}", levelResultCache.size());

        // Use a map keyed by dateId to merge persisted + in-memory, in-memory wins
        Map<String, DailyLevelReportEntry> merged = new LinkedHashMap<>();

        // 1. Load all persisted results from DynamoDB
        try {
            List<DailyLevelResult> persisted = dailyLevelResultDao.getAll();
            log.info("Loaded {} persisted results from DynamoDB", persisted.size());
            for (DailyLevelResult pr : persisted) {
                LocalDate date = null;
                try {
                    date = LocalDate.parse(pr.getDateId());
                } catch (Exception e) {
                    log.warn("Failed to parse dateId '{}' as LocalDate", pr.getDateId());
                }
                merged.put(pr.getDateId(), new DailyLevelReportEntry(
                        pr.getDateId(), date,
                        pr.getTotalPlayed(), pr.getTotalSucceeded(),
                        pr.getTotalFailed(), pr.getSuccessPercentage()));
            }
        } catch (Exception e) {
            log.warn("Failed to load persisted results from DynamoDB: {}", e.getMessage());
        }

        // 2. Overlay in-memory results (current/active data takes precedence)
        for (LevelResult lr : levelResultCache.values()) {
            int succeeded = lr.getSuccessCounter().get();
            int failed = lr.getFailCounter().get();
            int totalPlayed = succeeded + failed;
            merged.put(lr.getId(), new DailyLevelReportEntry(
                    lr.getId(), lr.getDate(),
                    totalPlayed, succeeded, failed,
                    lr.getSuccessPercentage()));
        }

        return merged.values().stream()
                .sorted(Comparator.comparing(DailyLevelReportEntry::getDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
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

    /**
     * Automatically generates a daily level for tomorrow.
     * Runs every day at 10:00 PM (22:00) server time.
     */
    @Scheduled(cron = "0 0 22 * * ?")
    public void autoGenerateTomorrowLevel() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("Cron Job: Automatically generating daily level for tomorrow: {}", tomorrow);
        try {
            createDailyLevel(tomorrow);
            log.info("Cron Job: Successfully generated level for {}", tomorrow);
        } catch (Exception e) {
            log.error("Cron Job: Failed to generate level for tomorrow: {}", e.getMessage(), e);
        }
    }

    /**
     * Persists yesterday's daily level results to DynamoDB.
     * Runs every day at 6:00 AM server time.
     * After persisting, the entry is removed from the in-memory cache.
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void persistYesterdayResults() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String yesterdayId = yesterday.toString();
        log.info("Cron Job: Persisting results for yesterday: {}", yesterdayId);

        LevelResult levelResult = levelResultCache.get(yesterdayId);
        if (levelResult == null) {
            log.info("Cron Job: No in-memory results found for {}, nothing to persist", yesterdayId);
            return;
        }

        int succeeded = levelResult.getSuccessCounter().get();
        int failed = levelResult.getFailCounter().get();
        int totalPlayed = succeeded + failed;
        float successPercentage = levelResult.getSuccessPercentage();

        DailyLevelResult dbResult = new DailyLevelResult(
                yesterdayId, totalPlayed, succeeded, failed, successPercentage);

        try {
            dailyLevelResultDao.save(dbResult);
            log.info("Cron Job: Persisted results for {} — played: {}, succeeded: {}, failed: {}, success%: {}",
                    yesterdayId, totalPlayed, succeeded, failed, successPercentage);

            // Remove from in-memory cache after successful persistence
            levelResultCache.remove(yesterdayId);
            log.info("Cron Job: Removed {} from in-memory cache", yesterdayId);
        } catch (Exception e) {
            log.error("Cron Job: Failed to persist results for {}: {}", yesterdayId, e.getMessage(), e);
        }
    }
}
