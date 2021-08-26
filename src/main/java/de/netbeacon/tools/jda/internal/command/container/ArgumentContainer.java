package de.netbeacon.tools.jda.internal.command.container;

import de.netbeacon.tools.jda.api.annotations.Argument;

import java.lang.reflect.Parameter;

public class ArgumentContainer {

    private final Parameter parameter;
    private final boolean isExposedArgument;
    private final Class<?> aClass;
    private final Argument annotation;

    public ArgumentContainer(Parameter parameter){
        this.parameter = parameter;
        var annotation = parameter.getAnnotation(Argument.class);
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

    public Argument getAnnotation() {
        return annotation;
    }
}
