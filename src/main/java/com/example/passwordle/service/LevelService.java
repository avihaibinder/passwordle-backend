package com.example.passwordle.service;

import com.example.passwordle.dto.DailyLevelRequest;
import com.example.passwordle.dto.DailyLevelResponse;
import com.example.passwordle.exception.LevelNotFoundException;
import com.example.passwordle.model.Level;
import com.example.passwordle.model.LevelResult;
import com.example.passwordle.model.SkeletonLevel;
import com.example.passwordle.dto.DailyLevelResultRequest;
import com.example.passwordle.dto.DailyLevelMetadataResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class LevelService {

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final Map<Integer, SkeletonLevel> skeletonLevelCache = new ConcurrentHashMap<>();
    private final Map<LocalDate, DailyLevelResponse> dailyLevelCache = new ConcurrentHashMap<>();
    private final Map<LocalDate, LevelResult> levelResultCache = new ConcurrentHashMap<>();

    public LevelService(ObjectMapper objectMapper, ResourceLoader resourceLoader) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    private SkeletonLevel getSkeletonLevel(int levelNumber) {
        return skeletonLevelCache.computeIfAbsent(levelNumber, this::loadSkeletonLevelFromFile);
    }

    private SkeletonLevel loadSkeletonLevelFromFile(Integer levelNumber) {
        Resource resource = resourceLoader.getResource("classpath:skeleton_levels/" + levelNumber + ".json");

        if (!resource.exists()) {
            throw new LevelNotFoundException("Level " + levelNumber + " not found!");
        }

        try {
            return objectMapper.readValue(resource.getInputStream(), SkeletonLevel.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load SkeletonLevel from file for level: " + levelNumber, e);
        }
    }

    public DailyLevelResponse getDailyLevel(DailyLevelRequest request) {
        LocalDate date = resolveDate(request);
        DailyLevelResponse response = dailyLevelCache.get(date);
        if (response == null) {
            throw new LevelNotFoundException("Daily level for " + date + " not found!");
        }
        return response;
    }

    public DailyLevelResponse createDailyLevel(DailyLevelRequest request) {
        LocalDate date = resolveDate(request);
        return dailyLevelCache.computeIfAbsent(date, this::generateNewDailyLevel);
    }

    private DailyLevelResponse generateNewDailyLevel(LocalDate date) {
        int randomSkeletonId = ThreadLocalRandom.current().nextInt(50);

        SkeletonLevel skeletonLevel = getSkeletonLevel(randomSkeletonId);
        Level level = Level.ofSkeletonLevel(skeletonLevel);

        // Use the date itself as the ID for the daily level response
        String dailyId = date != null ? date.toString() : "unknown-date";

        levelResultCache.putIfAbsent(date, new LevelResult(dailyId, date, 0, 0));

        return new DailyLevelResponse(dailyId, level);
    }

    public void postDailyLevelResult(DailyLevelResultRequest request) {
        LevelResult levelResult = levelResultCache.get(request.getDate());
        if (levelResult != null) {
            if ("success".equalsIgnoreCase(request.getResult())) {
                levelResult.setSuccessCounter(levelResult.getSuccessCounter() + 1);
            } else if ("failure".equalsIgnoreCase(request.getResult())) {
                levelResult.setFailCounter(levelResult.getFailCounter() + 1);
            }
        } else {
            LevelResult newRes = new LevelResult(request.getId(), request.getDate(),
                    "success".equalsIgnoreCase(request.getResult()) ? 1 : 0,
                    "failure".equalsIgnoreCase(request.getResult()) ? 1 : 0);
            levelResultCache.put(request.getDate(), newRes);
        }
    }

    public DailyLevelMetadataResponse getDailyLevelMetadata(DailyLevelRequest request) {
        LocalDate date = resolveDate(request);
        LevelResult levelResult = levelResultCache.get(date);
        if (levelResult == null) {
            throw new LevelNotFoundException("Daily level metadata for " + date + " not found!");
        }
        return new DailyLevelMetadataResponse(levelResult.getId(), levelResult.getDate(),
                levelResult.getSuccessPercentage());
    }

    private LocalDate resolveDate(DailyLevelRequest request) {
        if (request.getDate() != null) {
            return request.getDate();
        }
        if (request.getId() != null) {
            try {
                return LocalDate.parse(request.getId());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid id format. Expected YYYY-MM-DD", e);
            }
        }
        // If neither is provided, default to today's date
        return LocalDate.now();
    }
}
