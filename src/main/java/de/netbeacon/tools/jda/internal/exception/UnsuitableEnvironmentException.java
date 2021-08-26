package de.netbeacon.tools.jda.internal.exception;

public class UnsuitableEnvironmentException extends RuntimeException{

    public enum Type{
        UNKNOWN,
        NSFW
    }

    private final Type type;

    public UnsuitableEnvironmentException(Type type, String message){
        super(message);
        this.type = type;
    }

    public Type getType(){
        return type;
    }
}
