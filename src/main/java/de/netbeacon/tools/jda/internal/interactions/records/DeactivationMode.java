package de.netbeacon.tools.jda.internal.interactions.records;


import de.netbeacon.tools.jda.api.interactions.type.ComponentEntry;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public record DeactivationMode(Supplier<ComponentEntry<?>[]> entrySupplier) {

    public static final DeactivationMode NONE = new DeactivationMode(() -> new ComponentEntry[0]);

    public static final DeactivationMode SELF = new DeactivationMode(() -> new ComponentEntry[0]);

    public static final DeactivationMode ALL = new DeactivationMode(() -> new ComponentEntry[0]);

    public static DeactivationMode CUSTOM(ComponentEntry<?>... entries) {
        return CUSTOM(() -> entries);
    }

    public static DeactivationMode CUSTOM(Supplier<ComponentEntry<?>[]> entrySupplier) {
        return new DeactivationMode(entrySupplier);
    }

    public Set<String> getIds() {
        return Arrays.stream(entrySupplier.get()).map(ComponentEntry::getId).collect(Collectors.toSet());
    }

}
