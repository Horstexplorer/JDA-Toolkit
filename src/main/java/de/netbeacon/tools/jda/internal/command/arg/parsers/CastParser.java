package de.netbeacon.tools.jda.internal.command.arg.parsers;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parsable;

@Discoverable
public class CastParser<T> implements Parsable<T> {

    @Override
    public Class<T> type() {
        return null;
    }

    @Override
    public T parse(byte[] data) {
        return (T) data;
    }
}
