package ru.secured;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Stream;

public class SecuredLogbackConverter extends ClassicConverter {
    private Function<Object, String> initialHandler;

    public void setInitialHandler(List<String> sensitivePaths) {
        this.initialHandler = new JsonPathMaskingHandler(sensitivePaths);
    }

    @Override
    public String convert(ILoggingEvent event) {
        Object[] arguments = event.getArgumentArray();
        if (arguments == null || arguments.length < 1) {
            return event.getFormattedMessage();
        }
        return generateMessageWithHiddenSensitiveData(event);
    }

    private String generateMessageWithHiddenSensitiveData(ILoggingEvent event) {
        return Stream.of(event.getArgumentArray())
                .map(initialHandler)
                .reduce(event.getMessage(), (message, arg) -> message.replaceFirst("\\{}", Matcher.quoteReplacement(arg)));
    }
}
