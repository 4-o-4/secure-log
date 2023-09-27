package ru.secured;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.ConverterUtil;
import ch.qos.logback.core.pattern.parser.Node;
import ch.qos.logback.core.pattern.parser.Parser;
import ch.qos.logback.core.spi.ScanException;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.StatusManager;

import java.util.Arrays;
import java.util.List;

public class HidingLayout extends PatternLayout {
    private List<String> sensitivePaths;
    private Converter<ILoggingEvent> head;

    public void setHiddenKeys(final String keys) {
        this.sensitivePaths = Arrays.stream(keys.split(","))
                .map(String::trim)
                .toList();
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        return super.doLayout(event);
    }

    @Override
    public void start() {
        if (getPattern() == null || getPattern().isEmpty()) {
            addError("Empty or null pattern.");
            return;
        }
        try {
            Parser<ILoggingEvent> p = new Parser<ILoggingEvent>(getPattern());
            if (getContext() != null) {
                p.setContext(getContext());
            }
            Node t = p.parse();
            this.head = p.compile(t, getEffectiveConverterMap());
            if (postCompileProcessor != null) {
                postCompileProcessor.process(context, head);
            }
            ConverterUtil.setContextForConverters(getContext(), head);
            ConverterUtil.startConverters(this.head);
            super.start();
        } catch (ScanException sce) {
            StatusManager sm = getContext().getStatusManager();
            sm.add(new ErrorStatus("Failed to parse pattern \"" + getPattern() + "\".", this, sce));
        }
    }

    @Override
    protected String writeLoopOnConverters(ILoggingEvent event) {
        StringBuilder strBuilder = new StringBuilder(256);
        Converter<ILoggingEvent> c = head;
        while (c != null) {
            if (c instanceof SecuredLogbackConverter) {
                ((SecuredLogbackConverter) c).setInitialHandler(this.sensitivePaths);
            }
            c.write(strBuilder, event);
            c = c.getNext();
        }
        return strBuilder.toString();
    }
}
