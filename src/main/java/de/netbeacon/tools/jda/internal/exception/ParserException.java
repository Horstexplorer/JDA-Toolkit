package de.netbeacon.tools.jda.internal.exception;

public class ParserException extends RuntimeException {

    public ParserException(String message) {
        super(message);
    }

    public ParserException(String message, Exception e) {
        super(message, e);
    }

}
