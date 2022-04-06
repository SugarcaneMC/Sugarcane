package org.sugarcanemc.sugarcane.feature;

public class GlobalEula {
    public static boolean Accepted() {
        java.io.File globalEula = new java.io.File(System.getProperty("user.home"), ".mc-eula.accept");
        if (globalEula.exists()) {
            System.out.println("Global eula file found at " + globalEula.getAbsolutePath());
            return true;
        } else {
            System.out.println("No global eula file found at " + globalEula.getAbsolutePath());
        }
        return false;
    }
}
