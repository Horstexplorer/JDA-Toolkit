package de.netbeacon.tools.jda.internal.command.arg.parsers;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parsable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Discoverable
public class LocalDateParser implements Parsable<LocalDate> {
    @Override
    public Class<LocalDate> type() {
        return LocalDate.class;
    }

    @Override
    public LocalDate parse(byte[] data) {
        return LocalDate.parse(new String(data), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
