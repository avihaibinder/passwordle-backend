package com.example.passwordle.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

/**
 * DynamoDB-persisted record of daily level play results.
 * Partition key is the date string (e.g. "2026-01-13").
 */
@DynamoDbBean
public class DailyLevelResult {

    private String dateId;
    private int totalPlayed;
    private int totalSucceeded;
    private int totalFailed;
    private float successPercentage;

    public DailyLevelResult() {
    }

    public DailyLevelResult(String dateId, int totalPlayed, int totalSucceeded, int totalFailed, float successPercentage) {
        this.dateId = dateId;
        this.totalPlayed = totalPlayed;
        this.totalSucceeded = totalSucceeded;
        this.totalFailed = totalFailed;
        this.successPercentage = successPercentage;
    }

    @DynamoDbPartitionKey
    public String getDateId() {
        return dateId;
    }

    public void setDateId(String dateId) {
        this.dateId = dateId;
    }

    public int getTotalPlayed() {
        return totalPlayed;
    }

    public void setTotalPlayed(int totalPlayed) {
        this.totalPlayed = totalPlayed;
    }

    public int getTotalSucceeded() {
        return totalSucceeded;
    }

    public void setTotalSucceeded(int totalSucceeded) {
        this.totalSucceeded = totalSucceeded;
    }

    public int getTotalFailed() {
        return totalFailed;
    }

    public void setTotalFailed(int totalFailed) {
        this.totalFailed = totalFailed;
    }

    public float getSuccessPercentage() {
        return successPercentage;
    }

    public void setSuccessPercentage(float successPercentage) {
        this.successPercentage = successPercentage;
    }
}
