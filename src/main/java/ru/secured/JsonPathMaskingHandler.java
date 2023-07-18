package ru.secured;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import java.nio.CharBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JsonPathMaskingHandler extends MaskingHandler {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(MapperFeature.USE_STD_BEAN_NAMING, false);
    private static final Configuration JSON_PATH_CONFIG = Configuration.builder()
            .jsonProvider(new JacksonJsonProvider())
            .mappingProvider(new JacksonMappingProvider())
            .options(Option.SUPPRESS_EXCEPTIONS)
            .build();
    private final List<String> sensitivePaths;

    public JsonPathMaskingHandler(List<String> sensitivePaths) {
        this.sensitivePaths = sensitivePaths;
    }

    @Override
    String mask(Object argument) {
        return Optional.ofNullable(argument)
                .map(this::processMask)
                .orElse("");
    }

    private String processMask(Object argument) {
        Map<?, ?> argumentMap = objectToMap(argument);
        sensitivePaths.iterator()
                .forEachRemaining(path -> JsonPath.compile(path).set(argumentMap, maskData(argument, path), JSON_PATH_CONFIG));
        return argumentMap.toString();
    }

    private Map<?, ?> objectToMap(Object argument) {
        return OBJECT_MAPPER.convertValue(argument, Map.class);
    }

    private String maskData(Object argument, String path) {
        Object value = JsonPath.compile(path).read(argument, JSON_PATH_CONFIG);
        if (value == null) {
            return null;
        }
        String str = value.toString();
        return CharBuffer.allocate(str.length())
                .toString()
                .replace('\0', '*');
    }
}
