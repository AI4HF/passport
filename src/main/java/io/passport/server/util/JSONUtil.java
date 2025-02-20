package io.passport.server.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utility class for handling JSON serialization operations.
 */
public class JSONUtil {

    /**
     * ObjectMapper instance configured to handle Java Time serialization.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Converts any object to JSON string safely.
     *
     * @param obj Object to be serialized.
     * @return JSON representation of the object or an error message if serialization fails.
     */
    public static String objectToJsonSafely(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "Unable to serialize object: " + e.getMessage();
        }
    }
}