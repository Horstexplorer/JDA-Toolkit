package de.netbeacon.tools.jda.internal.exception;

public class DataException extends RuntimeException {

    public DataException(String message) {
        super(message);
    }

    public DataException(String message, Exception e) {
        super(message, e);
    }

}
