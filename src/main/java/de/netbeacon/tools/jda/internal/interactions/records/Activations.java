package de.netbeacon.tools.jda.internal.interactions.records;

public record Activations(Integer activations) {

    public static final Activations ONCE = new Activations(1);

    public static final Activations UNLIMITED = new Activations(-1);

    public static Activations CUSTOM(Integer count) {
        return new Activations(count);
    }

}
