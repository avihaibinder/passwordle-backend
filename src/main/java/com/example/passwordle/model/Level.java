package com.example.passwordle.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Level {

    @JsonProperty("data")
    private List<LevelRow> levelRows;

    @JsonProperty("numbers_each_row")
    private int numbersEachRow;

    @JsonProperty("password")
    private String password;

    public static Level ofSkeletonLevel(SkeletonLevel sl) {
        Map<String, String> mapping = new HashMap<>();
        List<String> digits = new ArrayList<>(
                Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
        Collections.shuffle(digits);
        int digitIndex = 0;

        // Map characters in rows to random digits
        for (SkeletonLevelRow row : sl.getSkeletonLevelRows()) {
            for (String letter : row.getNumbers()) {
                if (mapping.containsKey(letter)) {
                    continue;
                }
                mapping.put(letter, digits.get(digitIndex++));
            }
        }

        // Map any leftover characters in the password
        for (String letter : sl.getPassword()) {
            if (mapping.containsKey(letter)) {
                continue;
            }
            mapping.put(letter, digits.get(digitIndex++));
        }

        // Reconstruct the password string
        StringBuilder newPassword = new StringBuilder();
        for (String letter : sl.getPassword()) {
            newPassword.append(mapping.get(letter));
        }

        // Reconstruct the level rows
        List<LevelRow> levelRows = new ArrayList<>();
        for (SkeletonLevelRow row : sl.getSkeletonLevelRows()) {
            StringBuilder rowNumbers = new StringBuilder();
            for (String letter : row.getNumbers()) {
                rowNumbers.append(mapping.get(letter));
            }
            levelRows.add(new LevelRow(rowNumbers.toString(), row.getIndicator()));
        }

        return new Level(levelRows, sl.getNumbersEachRow(), newPassword.toString());
    }
}
