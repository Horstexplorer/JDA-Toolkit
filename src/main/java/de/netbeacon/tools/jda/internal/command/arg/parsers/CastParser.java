package de.netbeacon.tools.jda.internal.command.arg.parsers;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parser;
import de.netbeacon.tools.jda.internal.exception.ParserException;

@Discoverable
public class CastParser<T> implements Parser<T> {

    @Override
    public Class<T> type() {
        return null;
    }

    @Override
    public T parse(byte[] data) {
        try {
            return (T) data;
        }catch (Exception e){
            throw new ParserException("Failed to parse data as "+type().getName(), e);
        }
    }
}
