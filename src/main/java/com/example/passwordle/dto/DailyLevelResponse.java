package com.example.passwordle.dto;

import com.example.passwordle.model.Level;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyLevelResponse {

    private String id;
    private Level level;
}
