package com.example.passwordle.model;

import com.example.passwordle.converter.LevelConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class DailyLevel {

    private String levelId;
    private Level level;

    public DailyLevel() {
    }

    public DailyLevel(String levelId, Level level) {
        this.levelId = levelId;
        this.level = level;
    }

    @DynamoDbPartitionKey
    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    @DynamoDbConvertedBy(LevelConverter.class)
    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
}
