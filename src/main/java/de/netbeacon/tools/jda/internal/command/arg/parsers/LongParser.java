package de.netbeacon.tools.jda.internal.command.arg.parsers;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parser;
import de.netbeacon.tools.jda.internal.exception.ParserException;

import java.nio.ByteBuffer;

@Discoverable
public class LongParser implements Parser<Long> {

    @Override
    public Class<Long> type() {
        return Long.class;
    }

    @Override
    public Long parse(String data) {
        try {
            return Long.parseLong(data);
        } catch (Exception e) {
            throw new ParserException("Failed to parse data as " + type().getName(), e);
        }
    }

    @Override
    public Long parse(byte[] data) {
        try {
            return ByteBuffer.allocate(Long.BYTES).put(data).flip().getLong();
        } catch (Exception e) {
            throw new ParserException("Failed to parse data as " + type().getName(), e);
        }
    }
}
