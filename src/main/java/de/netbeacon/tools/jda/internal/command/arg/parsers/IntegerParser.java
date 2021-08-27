package de.netbeacon.tools.jda.internal.command.arg.parsers;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parser;

import java.nio.ByteBuffer;

@Discoverable
public class IntegerParser implements Parser<Integer> {

    @Override
    public Class<Integer> type() {
        return Integer.class;
    }

    @Override
    public Integer parse(String data) {
        return Integer.parseInt(data);
    }

    @Override
    public Integer parse(byte[] data) {
        return ByteBuffer.allocate(Integer.BYTES).put(data).flip().getInt();
    }
}
