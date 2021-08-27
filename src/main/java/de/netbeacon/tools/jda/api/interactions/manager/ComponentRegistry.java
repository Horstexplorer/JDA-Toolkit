package de.netbeacon.tools.jda.api.interactions.manager;

import de.netbeacon.tools.jda.api.interactions.type.ComponentEntry;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;

/**
 * Represents a registry for Components
 */
public interface ComponentRegistry {

    /**
     * Returns an ComponentEntry by its id
     *
     * @param id of the component
     * @param <T> of the represented jda interaction component
     * @return ComponentEntry or null if none has been found
     */
    <T extends ComponentInteraction> ComponentEntry<T> get(String id);

    /**
     * Register a component with this registry to activate it
     *
     * @param entry ComponentEntry
     */
    void register(ComponentEntry<?> entry);

}
