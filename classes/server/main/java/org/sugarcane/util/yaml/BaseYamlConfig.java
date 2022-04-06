package org.sugarcanemc.sugarcane.util.yaml;

import org.bukkit.Bukkit;
import org.sugarcanemc.sugarcane.config.SugarcaneConfig;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class BaseYamlConfig {
    protected static final Pattern SPACE = Pattern.compile(" ");
    protected static final Pattern NOT_NUMERIC = Pattern.compile("[^-\\d.]");

    protected static void logError(String s) {
        Bukkit.getLogger().severe(s);
    }

    protected static void log(String s) {
        if (SugarcaneConfig.verbose) {
            Bukkit.getLogger().info(s);
        }
    }

    public static int getSeconds(String str) {
        str = SPACE.matcher(str).replaceAll("");
        final char unit = str.charAt(str.length() - 1);
        str = NOT_NUMERIC.matcher(str).replaceAll("");
        double num;
        try {
            num = Double.parseDouble(str);
        } catch (Exception e) {
            num = 0D;
        }
        switch (unit) {
            case 'd':  num *= (double) 60 * 60 * 24;
            case 'h':  num *= (double) 60 * 60;
            case 'm':  num *= 60;
            case 's':  break;
            default: break;
        }
        return (int) num;
    }

    protected static String timeSummary(int seconds) {
        String time = "";

        if (seconds > 60 * 60 * 24) {
            time += TimeUnit.SECONDS.toDays(seconds) + "d";
            seconds %= 60 * 60 * 24;
        }

        if (seconds > 60 * 60) {
            time += TimeUnit.SECONDS.toHours(seconds) + "h";
            seconds %= 60 * 60;
        }

        if (seconds > 0) {
            time += TimeUnit.SECONDS.toMinutes(seconds) + "m";
        }
        return time;
    }
}
