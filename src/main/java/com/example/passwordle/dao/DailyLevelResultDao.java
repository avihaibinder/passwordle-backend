package com.example.passwordle.dao;

import com.example.passwordle.model.DailyLevelResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class DailyLevelResultDao {

    private final DynamoDbTable<DailyLevelResult> resultTable;

    public DailyLevelResultDao(DynamoDbEnhancedClient enhancedClient,
                               @Value("${dynamodb.table.daily-level-results:password-quest-prod-daily-level-results}") String tableName) {
        this.resultTable = enhancedClient.table(tableName,
                TableSchema.fromBean(DailyLevelResult.class));
    }

    public DailyLevelResult save(DailyLevelResult result) {
        resultTable.putItem(result);
        return result;
    }

    public Optional<DailyLevelResult> getByDateId(String dateId) {
        Key key = Key.builder().partitionValue(dateId).build();
        return Optional.ofNullable(resultTable.getItem(key));
    }

    public List<DailyLevelResult> getAll() {
        List<DailyLevelResult> results = new ArrayList<>();
        resultTable.scan().items().forEach(results::add);
        return results;
    }
}
