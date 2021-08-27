package de.netbeacon.tools.jda.internal.command.arg.parsers;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parser;

@Discoverable
public class BooleanParser implements Parser<Boolean> {

    @Override
    public Class<Boolean> type() {
        return Boolean.class;
    }

    @Override
    public Boolean parse(String data) {
        return Boolean.parseBoolean(data);
    }

    @Override
    public Boolean parse(byte[] data) {
        return data[0] != 0;
    }
}
