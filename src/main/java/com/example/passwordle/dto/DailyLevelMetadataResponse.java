package com.example.passwordle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyLevelMetadataResponse {
    private String id;
    private LocalDate date;

    @JsonProperty("success_percentage")
    private float successPercentage;
}
