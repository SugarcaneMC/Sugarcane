package org.sugarcanemc.sugarcane.config;

import com.google.common.base.Throwables;
import io.netty.util.ResourceLeakDetector;
import net.minecraft.SharedConstants;
import net.minecraft.world.level.storage.DataVersion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.sugarcanemc.sugarcane.util.yaml.BaseYamlConfig;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Level;

public class SharedConstantOverridesConfig extends BaseYamlConfig {
    public static File CONFIG_FILE;
    public static YamlConfiguration config;

    private static final String HEADER = "This is the overrides configuration for Minecraft's Shared Constants file.\n"
            + "Many of these may break your server or simply not work, so know what you are doing!\n"
            + "You have been warned!\n"
            + "We are not responsible for any effects caused by modifying these!\n"
            + "Regards, the Sugarcane team.\n";

    private static void init(File configFile) {
        CONFIG_FILE = configFile;
        config = new YamlConfiguration();
    }

    public static void Load() {
        System.out.println("Loading shared constant overrides - these may break your server!");
        init(new File("shared-constant-overrides.yml"));
        config = new YamlConfiguration();
        try {
            config.load(CONFIG_FILE);
        } catch (IOException ex) {
        } catch (InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not load shared-constant-overrides.yml, please correct your syntax errors", ex);
            throw Throwables.propagate(ex);
        }


        System.out.println("Loaded shared constant overrides!");
        Save();
    }

    public static void Save() {
        System.out.println("Saving shared constants to override config...");
        var constants = SharedConstants.class;
        var inst = new SharedConstants();
        for (var _const : constants.getDeclaredFields()) {
            if (_const.canAccess(Modifier.isStatic(_const.getModifiers()) ? null : inst))
                try {
                    var val = _const.get(inst);
                    if(!(
                        val instanceof ResourceLeakDetector.Level ||
                        val instanceof char[] ||
                        val instanceof DataVersion
                    ))
                        config.set(fieldToKey(_const), val);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
        }

        try {
            config.save(CONFIG_FILE);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not save " + CONFIG_FILE, ex);
        }
        System.out.println("Saved shared constants to override config!");
    }

    private static String fieldToKey(Field field) {
        if (Modifier.isFinal(field.getModifiers())) throw new IllegalStateException("Field is final!");

        return String.format("%s%s.%s", Modifier.isStatic(field.getModifiers()) ? "static." : "", field.getType().getSimpleName(), field.getName());
    }
}
