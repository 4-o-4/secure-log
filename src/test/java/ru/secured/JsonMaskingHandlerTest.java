package ru.secured;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class JsonMaskingHandlerTest {
    private static final ObjectMapper MAPPER = new ObjectMapper().configure(MapperFeature.USE_STD_BEAN_NAMING, false);
    private JsonMaskingHandler jsonMaskingHandler;

    @BeforeEach
    void init() {
        jsonMaskingHandler = new JsonMaskingHandler(Arrays.asList("$.value", "$.payload[?(@.name == 'test')].value"));
    }

    @Test
    public void noChangeObjectReferenceTest() throws JacksonException {
        Map<Object, Object> json = new HashMap<>();
        json.put("value", "Object");
        String jsonMask = jsonMaskingHandler.apply(json);
        String jsonNoMask = MAPPER.writeValueAsString(json);
        assertNotEquals(jsonNoMask, jsonMask);
    }

    @Test
    public void maskSrtingValueTest() throws JacksonException {
        Map<Object, Object> json = new HashMap<>();
        json.put("value", "Object");
        String jsonMask = jsonMaskingHandler.apply(json);
        json.put("value", "*****");
        assertEquals(MAPPER.writeValueAsString(json), jsonMask);
    }

    @Test
    public void maskNullValueTest() throws JacksonException {
        Map<Object, Object> json = new HashMap<>();
        json.put("value", null);
        String jsonMask = jsonMaskingHandler.apply(json);
        assertEquals(MAPPER.writeValueAsString(json), jsonMask);
    }

    @Test
    public void maskPayloadValueTest() throws JacksonException {
        Map<Object, Object> json = new HashMap<>();
        Map<Object, Object> payload = new HashMap<>();
        payload.put("name", "test");
        payload.put("value", "value");
        json.put("payload", payload);
        String jsonMask = jsonMaskingHandler.apply(json);
        payload.put("value", "*****");
        assertEquals(MAPPER.writeValueAsString(json), jsonMask);
    }
}