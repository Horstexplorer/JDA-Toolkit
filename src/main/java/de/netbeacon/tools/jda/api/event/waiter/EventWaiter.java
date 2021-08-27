package de.netbeacon.tools.jda.api.event.waiter;

import de.netbeacon.utils.concurrency.action.ExecutionAction;
import net.dv8tion.jda.api.events.GenericEvent;

import java.util.function.Predicate;

/**
 * Represents an event waiter useful to wait for another event to occur
 */
public interface EventWaiter {

    /**
     * Returns an execution action waiting for the event with the provided details
     *
     * @param eventClassToWait on which event to wait
     * @param condition what the event needs to contain
     * @param timeout for how long it should wait
     * @param <T> generic event type
     * @return ExecutionAction returning the waited for event
     */
    <T extends GenericEvent> ExecutionAction<T> waitFor(Class<T> eventClassToWait, Predicate<T> condition, long timeout);

}
