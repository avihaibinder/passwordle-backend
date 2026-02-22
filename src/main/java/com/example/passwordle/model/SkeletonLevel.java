package com.example.passwordle.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SkeletonLevel {

    @JsonProperty("data")
    private List<SkeletonLevelRow> skeletonLevelRows;

    @JsonProperty("numbers_each_row")
    private int numbersEachRow;

    @JsonProperty("password")
    private List<String> password;
}
