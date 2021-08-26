package de.netbeacon.tools.jda.internal.command.arg.parsers;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parsable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Discoverable
public class LocalDateTimeParser implements Parsable<LocalDateTime> {
    @Override
    public Class<LocalDateTime> type() {
        return LocalDateTime.class;
    }

    @Override
    public LocalDateTime parse(byte[] data) {
        return LocalDateTime.parse(new String(data), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
