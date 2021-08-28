package de.netbeacon.tools.jda.internal.command.container;

import java.lang.reflect.Parameter;

public class CommandArgument {

    private final Parameter parameter;
    private final boolean isExposedArgument;
    private final Class<?> aClass;
    private final de.netbeacon.tools.jda.api.annotations.Argument annotation;

    public CommandArgument(Parameter parameter){
        this.parameter = parameter;
        var annotation = parameter.getAnnotation(de.netbeacon.tools.jda.api.annotations.Argument.class);
        this.isExposedArgument = annotation != null;
        this.aClass = parameter.getType();
        this.annotation = annotation;
    }

    public String getDisplayName(){
        return annotation == null ? parameter.getName() : annotation.name();
    }

    public Parameter getParameter(){
        return parameter;
    }

    public boolean isExposedArgument() {
        return isExposedArgument;
    }

    public Class<?> getAClass() {
        return aClass;
    }

    public de.netbeacon.tools.jda.api.annotations.Argument getAnnotation() {
        return annotation;
    }
}
