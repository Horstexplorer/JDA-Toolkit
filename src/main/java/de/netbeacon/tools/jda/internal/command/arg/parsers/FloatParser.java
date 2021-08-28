package de.netbeacon.tools.jda.internal.command.arg.parsers;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parser;
import de.netbeacon.tools.jda.internal.exception.ParserException;

import java.nio.ByteBuffer;

@Discoverable
public class FloatParser implements Parser<Float> {

    @Override
    public Class<Float> type() {
        return Float.class;
    }

    @Override
    public Float parse(String data) {
        try {
            return Float.parseFloat(data);
        } catch (Exception e) {
            throw new ParserException("Failed to parse data as " + type().getName(), e);
        }
    }

    @Override
    public Float parse(byte[] data) {
        try {
            return ByteBuffer.allocate(Float.BYTES).put(data).flip().getFloat();
        } catch (Exception e) {
            throw new ParserException("Failed to parse data as " + type().getName(), e);
        }
    }
}
