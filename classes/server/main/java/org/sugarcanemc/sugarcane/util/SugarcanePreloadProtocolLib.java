package org.sugarcanemc.sugarcane.util;

import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Method;

public class SugarcanePreloadProtocolLib {

    public synchronized static void run() {
        try {
            final SimplePluginManager pluginManager = (SimplePluginManager) Bukkit.getPluginManager();
            final Plugin protocolLib = pluginManager.getPlugin("ProtocolLib");
            if(protocolLib != null && protocolLib.isEnabled()) {
                MinecraftServer.LOGGER.info("Sugarcane: Attempting to preload ProtocolLib's EnumWrappers");
                final Method initialize = Class.forName("com.comphenix.protocol.wrappers.EnumWrappers", true, protocolLib.getClass().getClassLoader()).getDeclaredMethod("initialize");
                initialize.setAccessible(true);
                initialize.invoke(null);
                synchronized (SugarcanePreloadProtocolLib.class) {
                }
            }
        } catch (Throwable t) {
            MinecraftServer.LOGGER.warn("Sugarcane: Failed to preload ProtocolLib's EnumWrappers", t);
        }
    }
}