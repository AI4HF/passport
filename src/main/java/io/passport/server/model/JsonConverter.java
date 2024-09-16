package io.passport.server.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Converter class which handles the transformation between String-JSON type entries.
 * Used in the conversion of data type for passport details.
 */
public class JsonConverter implements AttributeConverter<Map<String, Object>, String> {

    /**
     * Object Mapper class instance to be used in conversion.
     * Configured to include JavaTimeModule in order to handle Java Time entities properly.
     */
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    /**
     * Method to transform key-value map entries into string format.
     * @param attribute Input in the form of a key-value format.
     * @return
     */
    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute); // Convert to proper JSON
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting to JSON", e);
        }
    }

    /**
     * Method to transform string entry into a map of key-value entries.
     * @param content Input in the form of a string.
     * @return
     */
    @Override
    public Map<String, Object> convertToEntityAttribute(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Error converting JSON to Map", e);
        }
    }

}
