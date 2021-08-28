package de.netbeacon.tools.jda.internal.event.manager;

import de.netbeacon.tools.jda.api.event.listener.EventListenerPriority;
import de.netbeacon.tools.jda.api.event.manager.EventManager;
import de.netbeacon.utils.concurrency.queue.SuspendableBlockingQueue;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.ResumedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventManagerImp implements EventManager {

    private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();
    private final SuspendableBlockingQueue<GenericEvent> eventBuffer = new SuspendableBlockingQueue<>();
    private final AtomicBoolean handleSuspended = new AtomicBoolean(false);
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Executor urgentExecutor = Executors.newSingleThreadExecutor();

    public EventManagerImp() {
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            Executor defaultExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            defaultExecutor.execute(this::handleDefaultTask);
        }
    }

    @Override
    public void suspendBuffer(boolean state) {
        this.eventBuffer.suspend(state);
    }

    @Override
    public boolean bufferIsSuspended() {
        return eventBuffer.isSuspended();
    }

    @Override
    public void suspendHandle(boolean state) {
        this.handleSuspended.set(state);
    }

    @Override
    public boolean handleIsSuspended() {
        return handleSuspended.get();
    }

    @Override
    public void register(@NotNull Object listener) {
        if (!(listener instanceof EventListener)) {
            throw new IllegalArgumentException("Listener must implement EventListener");
        }
        listeners.add((EventListener) listener);
        listeners.sort((a, b) -> {
            Integer aPos = 0;
            int bPos = 0;
            if (a instanceof EventListenerPriority)
                aPos = ((EventListenerPriority) a).getPriority();
            if (b instanceof EventListenerPriority)
                bPos = ((EventListenerPriority) b).getPriority();

            return aPos.compareTo(bPos);
        });
    }

    @Override
    public void unregister(@NotNull Object listener) {
        if (!(listener instanceof EventListener)) {
            throw new IllegalArgumentException("Listener must implement EventListener");
        }
        listeners.remove(listener);
    }

    @Override
    public void handle(@NotNull GenericEvent genericEvent) {
        if (
                (genericEvent instanceof DisconnectEvent)
                        || (genericEvent instanceof ResumedEvent)
                        || (genericEvent instanceof ReconnectedEvent)
        ) {
            handleUrgent(genericEvent);
            return;
        }
        if (handleSuspended.get())
            return;
        eventBuffer.put(genericEvent);
    }

    private void handleUrgent(GenericEvent genericEvent) {
        if (genericEvent instanceof DisconnectEvent) {
            suspendBuffer(true);
            logger.warn("Disconnect detected! Suspending event processing from buffer. Currently " + eventBuffer.size() + " events are buffered.");
        } else if (genericEvent instanceof ResumedEvent) {
            suspendBuffer(false);
            logger.warn("Resume detected! Resuming to process from buffer. Currently " + eventBuffer.size() + " events are buffered.");
        } else if (genericEvent instanceof ReconnectedEvent) {
            var current = eventBuffer.size();
            eventBuffer.clear();
            suspendBuffer(false);
            logger.warn("Reconnect detected! Resuming to process. " + current + "  events have been cleared from the buffer as their integrity cannot be ensured.");
        }
        urgentExecutor.execute(() -> handleEvent(genericEvent));
    }

    private void handleDefaultTask() {
        try {
            while (true) {
                try {
                    var event = eventBuffer.get();
                    if (event == null) return;
                    handleEvent(event);
                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    logger.error("Unknown exception handling event", e);
                }
            }
        } catch (InterruptedException e) {
            logger.warn("Received interrupt. Wont process any more events");
        }
    }

    private void handleEvent(GenericEvent genericEvent) {
        for (var listener : listeners) {
            try {
                listener.onEvent(genericEvent);
            } catch (Throwable t) {
                if (t instanceof Error)
                    throw t;
                logger.error("Unhandled exception on listener " + listener.getClass().getSimpleName(), t);
            }
        }
    }

    @NotNull
    @Override
    public List<Object> getRegisteredListeners() {
        return List.of(listeners);
    }

}
