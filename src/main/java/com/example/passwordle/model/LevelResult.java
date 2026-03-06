package com.example.passwordle.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LevelResult {
    private String id;
    private LocalDate date;
    private int successCounter;
    private int failCounter;

    public float getSuccessPercentage() {
        float ratio;
        if (failCounter == 0) {
            if (successCounter == 0) {
                return 0.0f;
            }
            ratio = 100.0f;
        } else {
            ratio = (float) successCounter / failCounter * 100;
        }
        return (float) Math.round(ratio * 10) / 10;
    }
}
