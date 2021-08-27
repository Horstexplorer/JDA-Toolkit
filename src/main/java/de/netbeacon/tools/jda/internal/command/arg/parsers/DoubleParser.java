package de.netbeacon.tools.jda.internal.command.arg.parsers;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parser;
import de.netbeacon.tools.jda.internal.exception.ParserException;

import java.nio.ByteBuffer;

@Discoverable
public class DoubleParser implements Parser<Double> {

    @Override
    public Class<Double> type() {
        return Double.class;
    }

    @Override
    public Double parse(String data) {
        try {
            return Double.parseDouble(data);
        }catch (Exception e){
            throw new ParserException("Failed to parse data as "+type().getName(), e);
        }
    }

    @Override
    public Double parse(byte[] data) {
        try {
            return ByteBuffer.allocate(Double.BYTES).put(data).flip().getDouble();
        }catch (Exception e){
            throw new ParserException("Failed to parse data as "+type().getName(), e);
        }
    }
}
