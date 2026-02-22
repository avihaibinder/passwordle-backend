package com.example.passwordle.service;

import com.example.passwordle.dto.DailyLevelRequest;
import com.example.passwordle.dto.DailyLevelResponse;
import com.example.passwordle.exception.LevelNotFoundException;
import com.example.passwordle.model.Level;
import com.example.passwordle.model.SkeletonLevel;
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
        return dailyLevelCache.computeIfAbsent(request.getDate(), this::generateNewDailyLevel);
    }

    private DailyLevelResponse generateNewDailyLevel(LocalDate date) {
        // Generate a random number from 0 to 49 (since we have 0.json up to 50.json
        // currently)
        int randomSkeletonId = ThreadLocalRandom.current().nextInt(50);

        SkeletonLevel skeletonLevel = getSkeletonLevel(randomSkeletonId);
        Level level = Level.ofSkeletonLevel(skeletonLevel);

        // Use the date itself as the ID for the daily level response
        String dailyId = date != null ? date.toString() : "unknown-date";

        return new DailyLevelResponse(dailyId, level);
    }
}
