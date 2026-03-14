package com.example.passwordle.converter;

import com.example.passwordle.model.Level;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class LevelConverter implements AttributeConverter<Level> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public AttributeValue transformFrom(Level level) {
        try {
            return AttributeValue.builder().s(mapper.writeValueAsString(level)).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize Level", e);
        }
    }

    @Override
    public Level transformTo(AttributeValue attributeValue) {
        try {
            return mapper.readValue(attributeValue.s(), Level.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize Level", e);
        }
    }

    @Override
    public EnhancedType<Level> type() {
        return EnhancedType.of(Level.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}
