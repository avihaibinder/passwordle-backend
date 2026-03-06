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
        float ratio;
        if (fail == 0) {
            if (success == 0) {
                return 0.0f;
            }
            ratio = 100.0f;
        } else {
            ratio = (float) success / fail * 100;
        }
        return (float) Math.round(ratio * 10) / 10;
    }
}
