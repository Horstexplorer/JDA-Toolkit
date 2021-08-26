package de.netbeacon.tools.jda.internal.tuples;

public class Pair<V1,V2>{

    private final V1 value1;
    private final V2 value2;

    public Pair(V1 v1, V2 v2){
        this.value1 = v1;
        this.value2 = v2;
    }

    public V1 getValue1() {
        return value1;
    }

    public V2 getValue2() {
        return value2;
    }
}
