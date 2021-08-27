package de.netbeacon.tools.jda.internal.command.arg.parsers;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parser;
import de.netbeacon.tools.jda.internal.exception.ParserException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Discoverable
public class LocalDateParser implements Parser<LocalDate> {
    @Override
    public Class<LocalDate> type() {
        return LocalDate.class;
    }

    @Override
    public LocalDate parse(byte[] data) {
        try {
            return LocalDate.parse(new String(data), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }catch (Exception e){
            throw new ParserException("Failed to parse data as "+type().getName(), e);
        }
    }
}
