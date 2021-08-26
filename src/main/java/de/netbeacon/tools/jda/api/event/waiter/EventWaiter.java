package de.netbeacon.tools.jda.api.event.waiter;

import de.netbeacon.utils.concurrency.action.ExecutionAction;
import net.dv8tion.jda.api.events.GenericEvent;

import java.util.function.Predicate;

public interface EventWaiter {

    <T extends GenericEvent> ExecutionAction<T> waitFor(Class<T> eventClassToWait, Predicate<T> condition, long timeout);

}
