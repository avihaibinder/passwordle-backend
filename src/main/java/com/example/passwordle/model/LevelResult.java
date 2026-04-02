package com.example.passwordle.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LevelResult {
    private String id;
    private LocalDate date;
    private AtomicInteger successCounter = new AtomicInteger(0);
    private AtomicInteger failCounter = new AtomicInteger(0);

    public LevelResult(String id, LocalDate date) {
        this.id = id;
        this.date = date;
    }

    public void incrementSuccess() {
        successCounter.incrementAndGet();
    }

    public void incrementFailure() {
        failCounter.incrementAndGet();
    }

    public float getSuccessPercentage() {
        int success = successCounter.get();
        int fail = failCounter.get();
        int total = success + fail;
        if (total == 0) {
            return 0.0f;
        }
        float percentage = (float) success / total * 100;
        return (float) Math.round(percentage * 10) / 10.0f;
    }
}
