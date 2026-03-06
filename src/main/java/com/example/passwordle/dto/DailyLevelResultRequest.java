package com.example.passwordle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyLevelResultRequest {
    private String id;
    private LocalDate date;
    private String result;
}
