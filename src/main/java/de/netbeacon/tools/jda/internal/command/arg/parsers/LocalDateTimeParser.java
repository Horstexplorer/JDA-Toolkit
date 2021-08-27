package de.netbeacon.tools.jda.internal.command.arg.parsers;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parser;
import de.netbeacon.tools.jda.internal.exception.ParserException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Discoverable
public class LocalDateTimeParser implements Parser<LocalDateTime> {
    @Override
    public Class<LocalDateTime> type() {
        return LocalDateTime.class;
    }

    @Override
    public LocalDateTime parse(byte[] data) {
        try {
            return LocalDateTime.parse(new String(data), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }catch (Exception e){
            throw new ParserException("Failed to parse data as "+type().getName(), e);
        }
    }
}
