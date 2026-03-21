package com.example.passwordle;

import com.example.passwordle.model.DailyLevel;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

public class TestSchema {
    public static void main(String[] args) {
        try {
            TableSchema<DailyLevel> schema = TableSchema.fromBean(DailyLevel.class);
            System.out.println("Schema created successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
