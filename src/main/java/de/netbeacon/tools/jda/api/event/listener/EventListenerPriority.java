package de.netbeacon.tools.jda.api.event.listener;

/**
 * Helps to define the order in which an event should execute the event listeners
 * <p>
 * This is supported by the custom {@link de.netbeacon.tools.jda.api.event.manager.EventManager}
 */
public interface EventListenerPriority {

    /**
     * Defines the priority of the event listener
     * <p>
     * default is 0
     *
     * @return Integer
     */
    default int getPriority() {
        return 0;
    }

}
