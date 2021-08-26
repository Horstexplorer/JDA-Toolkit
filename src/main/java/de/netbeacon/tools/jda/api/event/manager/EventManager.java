package de.netbeacon.tools.jda.api.event.manager;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.IEventManager;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface EventManager extends IEventManager {

    void suspendBuffer(boolean state);

    boolean bufferIsSuspended();

    void suspendHandle(boolean state);

    boolean handleIsSuspended();

    static List<EventListener> discover(){
        try(var result = new ClassGraph().enableAllInfo().scan()){
            return result.getAllClasses().stream()
                    .filter(classInfo -> classInfo.implementsInterface(EventListener.class))
                    .filter(classInfo -> classInfo.hasAnnotation(Discoverable.class))
                    .map(ClassInfo::loadClass)
                    .filter(clazz -> Arrays.stream(clazz.getDeclaredConstructors()).anyMatch(constructor -> constructor.getParameterTypes().length == 0))
                    .map(clazz -> {
                        try {
                            return (EventListener) clazz.getDeclaredConstructor().newInstance();
                        }catch (Exception e){
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

}
