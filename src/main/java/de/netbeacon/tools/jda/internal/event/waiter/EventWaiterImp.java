package de.netbeacon.tools.jda.internal.event.waiter;

import de.netbeacon.tools.jda.api.event.listener.EventListenerPriority;
import de.netbeacon.tools.jda.api.event.waiter.EventWaiter;
import de.netbeacon.utils.concurrency.action.ExecutionAction;
import de.netbeacon.utils.concurrency.action.ExecutionException;
import de.netbeacon.utils.concurrency.action.imp.SupplierExecutionAction;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class EventWaiterImp implements EventWaiter, EventListener, EventListenerPriority {

    private final LinkedList<EventProfile<?>> eventDescriptionList = new LinkedList<>();
    private final ExecutorService asyncExecutorService;
    private final int priority;

    public EventWaiterImp() {
        this.asyncExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.priority = 0;
    }

    public EventWaiterImp(ExecutorService asyncExecutorService) {
        this.asyncExecutorService = asyncExecutorService;
        this.priority = 0;
    }

    public EventWaiterImp(ExecutorService asyncExecutorService, int priority) {
        this.asyncExecutorService = asyncExecutorService;
        this.priority = priority;
    }

    @Override
    public <T extends GenericEvent> ExecutionAction<T> waitFor(Class<T> eventClassToWait, Predicate<T> condition, long timeout) {
        Supplier<T> fun = () -> {
            EventProfile<T> desc = new EventProfile<T>(eventClassToWait, condition);
            try {
                eventDescriptionList.add(desc);
                synchronized (desc) {
                    desc.wait(timeout);
                }
                if (desc.getEvent() == null) {
                    throw new TimeoutException("Waiting for event timed out after " + timeout + " ms");
                }
                return desc.getEvent();
            } catch (Exception e) {
                eventDescriptionList.remove(desc);
                throw new ExecutionException(e);
            }
        };
        return new SupplierExecutionAction<>(asyncExecutorService, fun);
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        for (var entry : eventDescriptionList) {
            entry.tryFinish(event);
        }
    }

}
