package de.netbeacon.tools.jda.internal.exception;

public class UnsuitableEnvironmentException extends RuntimeException {

    private final Type type;

    public UnsuitableEnvironmentException(Type type, String message) {
        super(message);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        UNKNOWN,
        NSFW,
        CONTEXT
    }
}
