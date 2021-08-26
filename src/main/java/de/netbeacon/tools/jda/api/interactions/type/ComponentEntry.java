package de.netbeacon.tools.jda.api.interactions.type;

import de.netbeacon.tools.jda.api.interactions.manager.ComponentRegistry;
import de.netbeacon.tools.jda.internal.interactions.records.Accessor;
import de.netbeacon.tools.jda.internal.interactions.records.Activations;
import de.netbeacon.tools.jda.internal.interactions.records.DeactivationMode;
import de.netbeacon.tools.jda.internal.interactions.records.TimeoutPolicy;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;

import java.util.function.Consumer;

public interface ComponentEntry<T extends ComponentInteraction> {

    String getId();

    Accessor getAccessor();

    Activations getActivations();

    int getRemainingActivations();

    DeactivationMode getDeactivationMode();

    TimeoutPolicy getTimeoutPolicy();

    ComponentEntry<T> setRegistry(ComponentRegistry registry);

    ComponentRegistry getRegistry();

    boolean isValid();

    Consumer<T> successConsumer();

    Consumer<Exception> exceptionConsumer();

}
