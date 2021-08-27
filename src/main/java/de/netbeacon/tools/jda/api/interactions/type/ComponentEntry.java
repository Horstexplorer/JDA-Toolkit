package de.netbeacon.tools.jda.api.interactions.type;

import de.netbeacon.tools.jda.api.interactions.manager.ComponentRegistry;
import de.netbeacon.tools.jda.internal.interactions.records.Accessors;
import de.netbeacon.tools.jda.internal.interactions.records.Activations;
import de.netbeacon.tools.jda.internal.interactions.records.DeactivationMode;
import de.netbeacon.tools.jda.internal.interactions.records.TimeoutPolicy;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;

import java.util.function.Consumer;

/**
 * Represents a component entry
 * @param <T> represented ComponentInteraction
 */
public interface ComponentEntry<T extends ComponentInteraction> {

    /**
     * Returns the id of this component
     *
     * @return String
     */
    String getId();

    /**
     * Returns a record of allowed accessors / users of this component
     *
     * @return Accessors
     */
    Accessors getAccessor();

    /**
     * Returns a record of allowed activations of this component
     *
     * @return Activations
     */
    Activations getActivations();

    /**
     * Returns the amount of remaining activations
     *
     * @return Integer
     */
    int getRemainingActivations();

    /**
     * Returns the mode of deactivation for this component
     *
     * @return DeactivationMode
     */
    DeactivationMode getDeactivationMode();

    /**
     * Returns the timeout policy for this component
     *
     * @return TimeoutPolicy
     */
    TimeoutPolicy getTimeoutPolicy();

    /**
     * Sets the registry for this component
     *
     * This should not be set manually
     * @param registry which takes care of this record
     * @return instance of this component for chaining
     */
    ComponentEntry<T> setRegistry(ComponentRegistry registry);

    /**
     * Returns the registry this component is assigned to
     *
     * @return ComponentRegistry
     */
    ComponentRegistry getRegistry();

    /**
     * Whether this component is still valid to keep
     *
     * @return boolean
     */
    boolean isValid();

    /**
     * Execution consumer on successful activation
     *
     * @return Consumer<T>
     */
    Consumer<T> successConsumer();

    /**
     * Execution of exceptional activation
     *
     * @return Consumer<Exception>
     */
    Consumer<Exception> exceptionConsumer();

}
