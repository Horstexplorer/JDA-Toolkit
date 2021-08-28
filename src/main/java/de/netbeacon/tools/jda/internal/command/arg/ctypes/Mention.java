package de.netbeacon.tools.jda.internal.command.arg.ctypes;

import net.dv8tion.jda.api.entities.Message;

import java.util.regex.Matcher;

public class Mention {

    private final long id;
    private final Message.MentionType mentionType;

    private Mention(long id, Message.MentionType mentionType) {
        this.id = id;
        this.mentionType = mentionType;
    }

    public static Mention parse(String string) {
        Matcher m = Message.MentionType.CHANNEL.getPattern().matcher(string);
        if (m.matches()) {
            return new Mention(Long.parseLong(m.group(1)), Message.MentionType.CHANNEL);
        }
        m = Message.MentionType.ROLE.getPattern().matcher(string);
        if (m.matches()) {
            return new Mention(Long.parseLong(m.group(1)), Message.MentionType.ROLE);
        }
        m = Message.MentionType.USER.getPattern().matcher(string);
        if (m.matches()) {
            return new Mention(Long.parseLong(m.group(1)), Message.MentionType.USER);
        }
        m = Message.MentionType.EMOTE.getPattern().matcher(string);
        if (m.matches()) {
            return new Mention(Long.parseLong(m.group(2)), Message.MentionType.EMOTE);
        }
        throw new RuntimeException("Failed to parse mention from string");
    }

    public long getMentionedId() {
        return id;
    }

    public Message.MentionType getMentionType() {
        return mentionType;
    }
}
