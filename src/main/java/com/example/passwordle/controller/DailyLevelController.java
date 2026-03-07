package com.example.passwordle.controller;

import com.example.passwordle.dto.DailyLevelRequest;
import com.example.passwordle.dto.DailyLevelResultRequest;
import com.example.passwordle.service.LevelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/daily-level")
@RequiredArgsConstructor
public class DailyLevelController {

    private final LevelService levelService;

    @GetMapping
    public ResponseEntity<?> getDailyLevel(@RequestBody(required = false) DailyLevelRequest request) {
        log.info("=== GET /daily-level === request: {}", request);
        if (request == null) {
            request = new DailyLevelRequest();
            log.info("Request was null, created empty DailyLevelRequest");
        }
        try {
            Object response = levelService.getDailyLevel(request);
            log.info("GET /daily-level SUCCESS, response id: {}", response);
            return ResponseEntity.ok(response);
        } catch (Throwable t) {
            log.error("GET /daily-level ERROR: {}", t.getMessage(), t);
            return ResponseEntity.badRequest().body(t.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createDailyLevel(@RequestBody DailyLevelRequest request) {
        log.info("=== POST /daily-level === request: {}", request);
        try {
            Object response = levelService.createDailyLevel(request);
            log.info("POST /daily-level SUCCESS, response: {}", response);
            return ResponseEntity.ok(response);
        } catch (Throwable t) {
            log.error("POST /daily-level ERROR: {}", t.getMessage(), t);
            return ResponseEntity.badRequest().body(t.getMessage());
        }
    }

    @GetMapping("/metadata")
    public ResponseEntity<?> getDailyLevelMetadata(@RequestBody DailyLevelRequest request) {
        log.info("=== GET /daily-level/metadata === request: {}", request);
        try {
            Object response = levelService.getDailyLevelMetadata(request);
            log.info("GET /daily-level/metadata SUCCESS, response: {}", response);
            return ResponseEntity.ok(response);
        } catch (Throwable t) {
            log.error("GET /daily-level/metadata ERROR: {}", t.getMessage(), t);
            return ResponseEntity.badRequest().body(t.getMessage());
        }
    }

    @PostMapping("/result")
    public ResponseEntity<?> postDailyLevelResult(@RequestBody DailyLevelResultRequest request) {
        log.info("=== POST /daily-level/result === request: {}", request);
        try {
            levelService.postDailyLevelResult(request);
            log.info("POST /daily-level/result SUCCESS");
            return ResponseEntity.ok().build();
        } catch (Throwable t) {
            log.error("POST /daily-level/result ERROR: {}", t.getMessage(), t);
            return ResponseEntity.badRequest().body(t.getMessage());
        }
    }
}
