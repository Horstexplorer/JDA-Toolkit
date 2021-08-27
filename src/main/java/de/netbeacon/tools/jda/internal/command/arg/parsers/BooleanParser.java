package de.netbeacon.tools.jda.internal.command.arg.parsers;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parser;
import de.netbeacon.tools.jda.internal.exception.ParserException;

@Discoverable
public class BooleanParser implements Parser<Boolean> {

    @Override
    public Class<Boolean> type() {
        return Boolean.class;
    }

    @Override
    public Boolean parse(String data) {
        try {
            return Boolean.parseBoolean(data);
        }catch (Exception e){
            throw new ParserException("Failed to parse data as "+type().getName(), e);
        }
    }

    @Override
    public Boolean parse(byte[] data) {
        try {
            return data[0] != 0;
        }catch (Exception e){
            throw new ParserException("Failed to parse data as "+type().getName(), e);
        }
    }
}
