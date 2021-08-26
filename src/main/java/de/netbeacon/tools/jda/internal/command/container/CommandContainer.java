package de.netbeacon.tools.jda.internal.command.container;

import de.netbeacon.tools.jda.api.annotations.Command;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandContainer {


    private final Method method;
    private final Command annotation;
    private final List<ArgumentContainer> arguments;
    private final Object instance;

    public CommandContainer(Object instance, Method method){
        this.instance = instance;
        this.method = method;
        this.annotation = method.getAnnotation(Command.class);
        this.arguments = Arrays.stream(method.getParameters()).map(ArgumentContainer::new).collect(Collectors.toList());
    }

    public Object getInstance() {
        return instance;
    }

    public Method getMethod() {
        return method;
    }

    public Command getAnnotation() {
        return annotation;
    }

    public List<ArgumentContainer> getArguments() {
        return arguments;
    }
}
