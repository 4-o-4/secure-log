package ru.secured;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class JsonMaskingHandler implements Function<Object, String> {
    private static final Configuration JSON_PATH_CONFIG = Configuration.builder()
            .jsonProvider(new JacksonJsonProvider())
            .mappingProvider(new JacksonMappingProvider())
            .options(Option.SUPPRESS_EXCEPTIONS)
            .build();
    private final List<String> sensitivePaths;

    public JsonMaskingHandler(List<String> sensitivePaths) {
        this.sensitivePaths = sensitivePaths;
    }

    @Override
    public String apply(Object argument) {
        return Optional.ofNullable(argument)
                .map(this::processMask)
                .orElse("");
    }

    private String processMask(Object argument) {
        try {
            DocumentContext context = objectToContext(argument);
            sensitivePaths.iterator()
                    .forEachRemaining(path -> context.set(JsonPath.compile(path), maskData(context, path)));
            return context.jsonString();
        } catch (Exception e) {
            return argument.toString();
        }
    }

    private DocumentContext objectToContext(Object argument) throws IOException, ClassNotFoundException {
        ParseContext context = JsonPath.using(JSON_PATH_CONFIG);
        return argument instanceof String
                ? context.parse(argument.toString())
                : context.parse(deepClone(argument));
    }

    private Object deepClone(Object argument) throws IOException, ClassNotFoundException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(argument);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        }
    }

    private String maskData(DocumentContext context, String path) {
        Object value = context.read(path);
        return value == null ? null : "*****";
    }
}
