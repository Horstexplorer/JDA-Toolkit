package de.netbeacon.tools.jda.internal.command.arg.parsers;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parsable;

import java.nio.ByteBuffer;

@Discoverable
public class DoubleParser implements Parsable<Double> {

    @Override
    public Class<Double> type() {
        return Double.class;
    }

    @Override
    public Double parse(String data) {
        return Double.parseDouble(data);
    }

    @Override
    public Double parse(byte[] data) {
        return ByteBuffer.allocate(Double.BYTES).put(data).flip().getDouble();
    }
}
