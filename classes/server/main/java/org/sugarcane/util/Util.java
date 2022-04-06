package org.sugarcanemc.sugarcane.util;

import org.sugarcanemc.sugarcane.config.SugarcaneConfig;

public class Util {
    private final static int ConsoleBarWidth = 78; //80 column display
    public static int getIndentation(String s){
        if(!s.startsWith(" ")) return 0;
        int i = 0;
        while((s = s.replaceFirst(" ", "")).startsWith(" ")) i++;
        return i+1;
    }
    public static void logDebug(String s){
        if(SugarcaneConfig.debug) System.out.println(s);
    }
    public static String getTextProgressBar(double value) {
        int progress = (int) (value*ConsoleBarWidth);
        return String.format("[%s%s]", "=".repeat(progress), "_".repeat(ConsoleBarWidth - progress));
    }
}