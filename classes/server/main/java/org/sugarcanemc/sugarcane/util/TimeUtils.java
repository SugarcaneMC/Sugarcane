package org.sugarcanemc.sugarcane.util;

import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static String getFriendlyName(TimeUnit unit) {
        switch (unit) {
            case NANOSECONDS:
                return "ns";
            case MILLISECONDS:
                return "ms";
            case MICROSECONDS:
                return "micros";
            case SECONDS:
                return "s";
            case MINUTES:
                return "m";
            case DAYS:
                return "d";
            case HOURS:
                return "h";
            default:
                throw new AssertionError();
        }
    }
}