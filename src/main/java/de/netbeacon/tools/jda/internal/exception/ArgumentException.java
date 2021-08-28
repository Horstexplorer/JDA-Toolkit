package de.netbeacon.tools.jda.internal.exception;

public class ArgumentException extends RuntimeException {

    private final Type type;

    public ArgumentException(Type type, String message) {
        super(message);
        this.type = type;
    }

    public ArgumentException(Type type, String message, Exception e) {
        super(message, e);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        NOT_SUPPLIED,
        NO_AVAILABLE_PARSER,
        PARSING_FAILED;
    }
}
