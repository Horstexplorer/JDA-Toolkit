package de.netbeacon.tools.jda.internal.exception;

import java.lang.reflect.Parameter;

public class ParameterException extends RuntimeException{

    public ParameterException(String message){
        super(message);
    }

    public ParameterException(String message, Exception e){
        super(message, e);
    }

}
