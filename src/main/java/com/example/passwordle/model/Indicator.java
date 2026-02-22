package com.example.passwordle.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Indicator {

    @JsonProperty("correct_in_place")
    private int correctInPlace;

    @JsonProperty("correct_not_in_place")
    private int correctNotInPlace;
}
