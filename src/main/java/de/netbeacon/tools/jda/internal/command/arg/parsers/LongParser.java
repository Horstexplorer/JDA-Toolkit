package de.netbeacon.tools.jda.internal.command.arg.parsers;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parsable;

import java.nio.ByteBuffer;

@Discoverable
public class LongParser implements Parsable<Long> {

    @Override
    public Class<Long> type() {
        return Long.class;
    }

    @Override
    public Long parse(String data) {
        return Long.parseLong(data);
    }

    @Override
    public Long parse(byte[] data) {
        return ByteBuffer.allocate(Long.BYTES).put(data).flip().getLong();
    }
}
