package de.netbeacon.tools.jda.internal.command.arg.parsers;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parsable;

import java.nio.ByteBuffer;

@Discoverable
public class FloatParser implements Parsable<Float> {

    @Override
    public Class<Float> type() {
        return Float.class;
    }

    @Override
    public Float parse(String data) {
        return Float.parseFloat(data);
    }

    @Override
    public Float parse(byte[] data) {
        return ByteBuffer.allocate(Float.BYTES).put(data).flip().getFloat();
    }
}
