package de.netbeacon.tools.jda.internal.command.arg.parsers;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parser;
import de.netbeacon.tools.jda.internal.command.arg.ctypes.HumanTime;
import de.netbeacon.tools.jda.internal.exception.ParserException;

@Discoverable
public class HumanTimeParser implements Parser<HumanTime> {
    @Override
    public Class<HumanTime> type() {
        return HumanTime.class;
    }

    @Override
    public HumanTime parse(byte[] data) {
        try {
            return HumanTime.parse(new String(data));
        }catch (Exception e){
            throw new ParserException("Failed to parse data as "+type().getName(), e);
        }
    }
}
