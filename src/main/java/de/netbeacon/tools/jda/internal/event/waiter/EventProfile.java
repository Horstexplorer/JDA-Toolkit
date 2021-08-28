package de.netbeacon.tools.jda.internal.event.waiter;

import net.dv8tion.jda.api.events.GenericEvent;

import java.util.function.Predicate;

public class EventProfile<T extends GenericEvent> {

    private final Class<T> eventClassToWait;
    private final Predicate<T> condition;
    private T event;

    protected EventProfile(Class<T> eventClassToWait, Predicate<T> condition) {
        this.eventClassToWait = eventClassToWait;
        this.condition = condition;
    }

    @SuppressWarnings({"unchecked"})
    public synchronized void tryFinish(GenericEvent event) {
        if (eventClassToWait == event.getClass() && this.event == null) {
            if (condition.test((T) event)) {
                this.event = (T) event;
                this.notify();
            }
        }
    }

    public synchronized T getEvent() {
        return this.event;
    }

}
