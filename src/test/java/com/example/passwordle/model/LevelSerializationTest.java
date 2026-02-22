package com.example.passwordle.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class LevelSerializationTest {

    @Test
    public void testSerializationDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        String json = "{\n" +
                "  \"data\": [\n" +
                "    {\n" +
                "      \"numbers\": [\"F\", \"H\"],\n" +
                "      \"indicator\": {\n" +
                "        \"correct_in_place\": 1,\n" +
                "        \"correct_not_in_place\": 0\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"numbers_each_row\": 2,\n" +
                "  \"password\": [\"F\", \"G\"]\n" +
                "}";

        SkeletonLevel skeleton = mapper.readValue(json, SkeletonLevel.class);

        assertNotNull(skeleton);
        assertEquals(2, skeleton.getNumbersEachRow());
        assertEquals(1, skeleton.getSkeletonLevelRows().size());
        assertEquals(1, skeleton.getSkeletonLevelRows().get(0).getIndicator().getCorrectInPlace());

        String serialized = mapper.writeValueAsString(skeleton);
        assertTrue(serialized.contains("numbers_each_row"));
    }
}
