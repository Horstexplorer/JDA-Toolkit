package de.netbeacon.tools.jda.internal.interactions.type;

import de.netbeacon.tools.jda.api.interactions.manager.ComponentRegistry;
import de.netbeacon.tools.jda.api.interactions.type.ComponentEntry;
import de.netbeacon.tools.jda.internal.interactions.records.Accessors;
import de.netbeacon.tools.jda.internal.interactions.records.Activations;
import de.netbeacon.tools.jda.internal.interactions.records.DeactivationMode;
import de.netbeacon.tools.jda.internal.interactions.records.TimeoutPolicy;
import de.netbeacon.tools.jda.internal.interactions.utils.IDGenerator;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ComponentEntryImp<T extends ComponentInteraction> implements ComponentEntry<T> {

    private final String id = IDGenerator.printableString(100);
    private final Accessors accessors;
    private final Activations activations;
    private final DeactivationMode deactivationMode;
    private final TimeoutPolicy timeoutPolicy;
    private final AtomicInteger remainingActivations = new AtomicInteger(0);
    private final AtomicBoolean deactivated = new AtomicBoolean(false);
    private ComponentRegistry registry;
    private final Consumer<T> successConsumer;
    private final Consumer<Exception> exceptionConsumer;


    public ComponentEntryImp(Accessors accessors, Activations activations, DeactivationMode deactivationMode, TimeoutPolicy timeoutPolicy, Consumer<T> successConsumer, Consumer<Exception> exceptionConsumer){
        this.accessors = accessors;
        this.activations = activations;
        this.deactivationMode = deactivationMode;
        this.timeoutPolicy = timeoutPolicy;
        this.remainingActivations.set(activations.activations());
        this.successConsumer = successConsumer;
        this.exceptionConsumer = exceptionConsumer;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Accessors getAccessor() {
        return accessors;
    }

    @Override
    public Activations getActivations() {
        return activations;
    }

    @Override
    public int getRemainingActivations() {
        return remainingActivations.get();
    }

    @Override
    public DeactivationMode getDeactivationMode() {
        return deactivationMode;
    }

    @Override
    public TimeoutPolicy getTimeoutPolicy() {
        return timeoutPolicy;
    }

    @Override
    public ComponentEntry<T> setRegistry(ComponentRegistry registry) {
        this.registry = registry;
        return this;
    }

    @Override
    public ComponentRegistry getRegistry() {
        return registry;
    }

    @Override
    public boolean isValid() {
        return !deactivated.get()
                && timeoutPolicy.isInTime()
                && (remainingActivations.get() > 0 || activations.equals(Activations.UNLIMITED));
    }

    @Override
    public Consumer<T> successConsumer() {
        return successConsumer;
    }

    @Override
    public Consumer<Exception> exceptionConsumer() {
        return exceptionConsumer;
    }

    public synchronized boolean performActivation(){
        if(activations.equals(Activations.UNLIMITED))
            return true;
        if(remainingActivations.get() <= 0)
            return false;
        remainingActivations.decrementAndGet();
        return true;
    }

    public synchronized boolean markDeactivated(){
        return deactivated.compareAndSet(false, true);
    }
}
