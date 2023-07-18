package ru.secured;

import java.util.function.Function;

public abstract class MaskingHandler implements Function<Object, String> {
    @Override
    public String apply(Object argument) {
        return mask(argument);
    }

    abstract String mask(Object argument);
}
