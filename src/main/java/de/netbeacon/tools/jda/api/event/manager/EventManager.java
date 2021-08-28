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

/**
 * Represents an extended event manager
 */
public interface EventManager extends IEventManager {

    /**
     * Runs the discovery for all EventListeners which have been marked as @Discoverable
     * <p>
     * Will create and return a new instance of each one of them where this is possible to do so
     *
     * @return List of found event listeners
     */
    static List<EventListener> discover() {
        try (var result = new ClassGraph().enableAllInfo().scan()) {
            return result.getAllClasses().stream()
                    .filter(classInfo -> classInfo.implementsInterface(EventListener.class))
                    .filter(classInfo -> classInfo.hasAnnotation(Discoverable.class))
                    .map(ClassInfo::loadClass)
                    .filter(clazz -> Arrays.stream(clazz.getDeclaredConstructors()).anyMatch(constructor -> constructor.getParameterTypes().length == 0))
                    .map(clazz -> {
                        try {
                            return (EventListener) clazz.getDeclaredConstructor().newInstance();
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Suspends the input to the buffer.
     * <p>
     * Activating will reject events from getting added to the buffer and therefore from further processing.
     *
     * @param state true if suspended
     */
    void suspendBuffer(boolean state);

    /**
     * Whether the buffer input is suspended
     *
     * @return boolean
     */
    boolean bufferIsSuspended();

    /**
     * Suspends the event handling from the buffer
     * <p>
     * Activating will stop further processing of events leading for them to pile up in the buffer
     *
     * @param state true if suspended
     */
    void suspendHandle(boolean state);

    /**
     * Whether the event handling from the buffer is suspended
     *
     * @return boolean
     */
    boolean handleIsSuspended();

}
