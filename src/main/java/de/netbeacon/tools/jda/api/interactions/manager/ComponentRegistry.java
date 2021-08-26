package de.netbeacon.tools.jda.api.interactions.manager;

import de.netbeacon.tools.jda.api.interactions.type.ComponentEntry;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;

public interface ComponentRegistry {

    <T extends ComponentInteraction> ComponentEntry<T> get(String id);

    void register(ComponentEntry<?> entry);



}
