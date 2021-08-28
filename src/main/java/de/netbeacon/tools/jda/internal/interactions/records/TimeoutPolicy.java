package de.netbeacon.tools.jda.internal.interactions.records;

public record TimeoutPolicy(Long now, Long timeoutInMS) {

    public static final TimeoutPolicy NONE = new TimeoutPolicy(Long.MIN_VALUE, Long.MAX_VALUE);

    public static TimeoutPolicy CUSTOM(Long ms) {
        return new TimeoutPolicy(System.currentTimeMillis(), ms);
    }

    public boolean isInTime() {
        if (this.equals(NONE)) {
            return true;
        }
        return System.currentTimeMillis() <= now + timeoutInMS;
    }

}
