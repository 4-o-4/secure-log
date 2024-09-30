# secure-log

#### Template for logback.xml
```xml
<conversionRule conversionWord="mask_msg"
    converterClass="ru.secured.SecuredLogbackConverter"/>

<appender name="FILE" class="FileAppender">
    <file>${HOME_LOG}</file>
    <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
        <layout class="ru.secured.HidingLayout">
            <hiddenKeys>
                $.[*].name
            </hiddenKeys>
            <pattern>${CUSTOM_LOG_PATTERN}</pattern>
        </layout>
    </encoder>
</appender>
```

#### Example
```
2024-09-30 INFO [main] ru.secured.MainTest - Message: [{"name":"*****"},{"name":"*****"}]
```
