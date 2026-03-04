package com.example.passwordle.controller;

import com.example.passwordle.dto.DailyLevelRequest;
import com.example.passwordle.service.LevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/daily-level")
@RequiredArgsConstructor
public class DailyLevelController {

    private final LevelService levelService;

    @GetMapping
    public ResponseEntity<?> getDailyLevel(@RequestBody DailyLevelRequest request) {
        try {
            return ResponseEntity.ok(levelService.getDailyLevel(request));
        } catch (Throwable t) {
            return ResponseEntity.badRequest().body(t.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createDailyLevel(@RequestBody DailyLevelRequest request) {
        try {
            return ResponseEntity.ok(levelService.createDailyLevel(request));
        } catch (Throwable t) {
            return ResponseEntity.badRequest().body(t.getMessage());
        }
    }
}
