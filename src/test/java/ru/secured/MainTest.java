package ru.secured;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MainTest {
    public static Logger log = LoggerFactory.getLogger(MainTest.class);

    @Test
    public void main() {
        Map<String, String> map = new HashMap<>();
        map.put("name", "MainTest");
        log.info("Message: {}", map);
    }
}
