package com.example.passwordle.dao;

import com.example.passwordle.model.DailyLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

@Repository
public class DailyLevelDao {

    private final DynamoDbTable<DailyLevel> dailyLevelTable;

    public DailyLevelDao(DynamoDbEnhancedClient enhancedClient,
                         @Value("${dynamodb.table.daily-levels:password-quest-prod-daily-levels}") String tableName) {
        this.dailyLevelTable = enhancedClient.table(tableName,
                TableSchema.fromBean(DailyLevel.class));
    }

    public DailyLevel save(DailyLevel dailyLevel) {
        dailyLevelTable.putItem(dailyLevel);
        return dailyLevel;
    }

    public Optional<DailyLevel> getById(String levelId) {
        Key key = Key.builder().partitionValue(levelId).build();
        return Optional.ofNullable(dailyLevelTable.getItem(key));
    }
}
