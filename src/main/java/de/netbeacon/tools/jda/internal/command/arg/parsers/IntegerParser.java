package de.netbeacon.tools.jda.internal.command.arg.parsers;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parser;
import de.netbeacon.tools.jda.internal.exception.ParserException;

import java.nio.ByteBuffer;

@Discoverable
public class IntegerParser implements Parser<Integer> {

    @Override
    public Class<Integer> type() {
        return Integer.class;
    }

    @Override
    public Integer parse(String data) {
        try {
            return Integer.parseInt(data);
        }catch (Exception e){
            throw new ParserException("Failed to parse data as "+type().getName(), e);
        }
    }

    @Override
    public Integer parse(byte[] data) {
        try {
            return ByteBuffer.allocate(Integer.BYTES).put(data).flip().getInt();
        }catch (Exception e){
            throw new ParserException("Failed to parse data as "+type().getName(), e);
        }
    }
}
