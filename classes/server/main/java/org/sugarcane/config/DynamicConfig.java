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
import java.util.List;
import java.util.logging.Level;

public class DynamicConfig extends BaseYamlConfig {
    public File CONFIG_FILE;
    public YamlConfiguration config = new YamlConfiguration();;

    private String HEADER = "This is the overrides configuration for Minecraft's Shared Constants file.\n"
            + "Many of these may break your server or simply not work, so know what you are doing!\n"
            + "You have been warned!\n"
            + "We are not responsible for any effects caused by modifying these!\n"
            + "Regards, the Sugarcane team.\n";

    public DynamicConfig(String file){
        CONFIG_FILE = new File(file);
    }

    public void Load() {
        var stime = System.nanoTime();
        System.out.printf("Loading %s...", CONFIG_FILE.getName());
        config = new YamlConfiguration();
        try {
            config.load(CONFIG_FILE);
        } catch (IOException ex) {
        } catch (InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, " Could not load file! Please correct your syntax errors", ex);
            throw Throwables.propagate(ex);
        }

        System.out.printf(" Done! (%.2f ms)\n", (System.nanoTime() - stime) / 1000000d);
        Save();
    }

    public void Save() {
        var stime = System.nanoTime();
        System.out.printf("Saving %s...", CONFIG_FILE.getName());

        try {
            config.save(CONFIG_FILE);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not save " + CONFIG_FILE, ex);
        }

        System.out.printf(" Done! (%.2f ms)\n", (System.nanoTime() - stime) / 1000000d);
    }

    public void set(String path, Object val) {
        config.set(path, val);
    }

    public boolean getBoolean(String path, boolean def) {
        config.addDefault(path, def);
        return config.getBoolean(path, config.getBoolean(path));
    }

    public double getDouble(String path, double def) {
        config.addDefault(path, def);
        return config.getDouble(path, config.getDouble(path));
    }

    public float getFloat(String path, float def) {
        // TODO: Figure out why getFloat() always returns the default value.
        return (float) getDouble(path, (double) def);
    }

    public int getInt(String path, int def) {
        config.addDefault(path, def);
        return config.getInt(path, config.getInt(path));
    }

    public <T> List<T> getList(String path, List<T> def) {
        config.addDefault(path, def);
        return (List<T>) config.getList(path, config.getList(path));
    }

    public String getString(String path, String def) {
        config.addDefault(path, def);
        return config.getString(path, config.getString(path));
    }
}
