package com.example.passwordle.service;

import com.example.passwordle.dto.DailyLevelRequest;
import com.example.passwordle.dto.DailyLevelResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class LevelServiceTest {

    private LevelService levelService;

    @BeforeEach
    public void setUp() {
        // Instantiate real dependencies for a complete unit/integration test!
        ObjectMapper objectMapper = new ObjectMapper();
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        levelService = new LevelService(objectMapper, resourceLoader);
    }

    @Test
    public void testGetDailyLevel_CachingWorks() {
        LocalDate today = LocalDate.of(2026, 2, 22);
        DailyLevelRequest request1 = new DailyLevelRequest(today, "UTC");

        // Fetch the level for the first time
        DailyLevelResponse response1 = levelService.getDailyLevel(request1);

        assertNotNull(response1);
        assertNotNull(response1.getLevel());
        assertEquals("2026-02-22", response1.getId());
        assertNotNull(response1.getLevel().getPassword(), "Password mapping should have generated");

        // Call it a second time for the exact same date
        DailyLevelRequest request2 = new DailyLevelRequest(today, "+02:00"); // Different timezone shouldn't matter
        DailyLevelResponse response2 = levelService.getDailyLevel(request2);

        // It MUST be the exact same object configuration in memory due to the map
        // cache!
        assertSame(response1, response2, "The cache should return the exact identical reference for the same date");
        assertEquals(response1.getLevel().getPassword(), response2.getLevel().getPassword());
    }

    @Test
    public void testGetDailyLevel_DifferentDatesReturnDifferentResponses() {
        LocalDate today = LocalDate.of(2026, 5, 10);
        LocalDate tomorrow = LocalDate.of(2026, 5, 11);

        DailyLevelRequest requestToday = new DailyLevelRequest(today, "UTC");
        DailyLevelRequest requestTomorrow = new DailyLevelRequest(tomorrow, "UTC");

        DailyLevelResponse responseToday = levelService.getDailyLevel(requestToday);
        DailyLevelResponse responseTomorrow = levelService.getDailyLevel(requestTomorrow);

        assertNotNull(responseToday);
        assertNotNull(responseTomorrow);

        // They should be completely separate objects and configurations
        assertNotSame(responseToday, responseTomorrow);
        assertEquals("2026-05-10", responseToday.getId());
        assertEquals("2026-05-11", responseTomorrow.getId());
    }
}
