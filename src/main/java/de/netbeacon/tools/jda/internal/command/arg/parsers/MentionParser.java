package de.netbeacon.tools.jda.internal.command.arg.parsers;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parser;
import de.netbeacon.tools.jda.internal.command.arg.ctypes.Mention;
import de.netbeacon.tools.jda.internal.exception.ParserException;

@Discoverable
public class MentionParser implements Parser<Mention> {

    @Override
    public Class<Mention> type() {
        return Mention.class;
    }

    @Override
    public Mention parse(String data) throws ParserException {
        try {
            return Mention.parse(data);
        }catch (Exception e){
            throw new ParserException("Failed to parse data as "+type().getName(), e);
        }
    }

    @Override
    public Mention parse(byte[] data) throws ParserException {
        try {
            return Mention.parse(new String(data));
        }catch (Exception e){
            throw new ParserException("Failed to parse data as "+type().getName(), e);
        }
    }
}
