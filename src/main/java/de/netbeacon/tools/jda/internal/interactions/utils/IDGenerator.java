package de.netbeacon.tools.jda.internal.interactions.utils;

import org.apache.commons.lang3.RandomStringUtils;

public class IDGenerator {

    private IDGenerator() {
    }

    public static synchronized String printableString(int length) {
        return RandomStringUtils.randomPrint(length);
    }

}
