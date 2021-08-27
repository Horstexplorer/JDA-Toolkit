package de.netbeacon.tools.jda.internal.command.arg.parsers;

import de.netbeacon.tools.jda.api.command.arg.Parser;
import de.netbeacon.tools.jda.internal.exception.ParserException;

public class StringParser implements Parser<String> {

    @Override
    public Class<String> type() {
        return String.class;
    }

    @Override
    public String parse(String data) throws ParserException {
        return data;
    }

    @Override
    public String parse(byte[] data) throws ParserException {
        return new String(data);
    }
}
