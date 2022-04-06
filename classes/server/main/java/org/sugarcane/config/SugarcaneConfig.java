package org.sugarcanemc.sugarcane.config;

import com.google.common.base.Throwables;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.SharedConstants;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.purpurmc.purpur.PurpurConfig;
import org.sugarcanemc.sugarcane.util.ClassGenerators;
import org.sugarcanemc.sugarcane.util.yaml.BaseYamlConfig;
import org.sugarcanemc.sugarcane.util.yaml.YamlCommenter;

public class SugarcaneConfig extends BaseYamlConfig {
	public static File CONFIG_FILE;
	private static final String HEADER = "This is the main configuration file for Sugarcane.\n"
			+ "Sugarcane contains many breaking changes and settings, so know what you are doing!\n"
			+ "You have been warned!\n"
            + "Join our Discord to receive support & optimization help: https://sugarcanemc.org/discord\n";
	/*========================================================================*/
	public static YamlConfiguration config;
	private static final YamlCommenter c = new YamlCommenter();
	public static int version; // since we're remapping sidestreams' configs we need this public
	public static boolean verbose; // since we're remapping sidestreams' configs we need this public
	public static boolean alreadyLoaded = false;
	/*========================================================================*/

	public static void init(File configFile) {
		if(alreadyLoaded) return;
		CONFIG_FILE = configFile;
		config = new YamlConfiguration();
		try {
			config.load(CONFIG_FILE);
		} catch (IOException ex) {
		} catch (InvalidConfigurationException ex) {
			Bukkit.getLogger().log(Level.SEVERE, "Could not load sugarcane.yml, please correct your syntax errors", ex);
			throw Throwables.propagate(ex);
		}
		config.options().copyDefaults(true);
		verbose = getBoolean("dev.verbose", false);
		version = getInt("config-version", 2);
		set("config-version", 2);
		removeLeftovers();
		readConfig(SugarcaneConfig.class, null);
		addComments();
		alreadyLoaded = true;
	}
	private static void addComments() {
		//add header
		c.setHeader("""
				This is the main configuration file for Sugarcane.
				Sugarcane contains many breaking changes and settings, so know what you are doing!
				You have been warned!
				Join our Discord to receive support & optimization help: https://sugarcanemc.org/discord""");
		//add comments
		// section: developer toggles
		c.addComment("dev", "These settings are only useful for Sugarcane developers. You shouldn't ever need to touch these!");
		c.addComment("dev.debug", "Enable debug output for Sugarcane code!\nWarning: this WILL increase log size a lot!\nKeep in mind this is for output considered too spammy for verbose!");
		c.addComment("dev.verbose", "Enable verbose mode for Sugarcane code!\nWarning: may increase log size and/or hurt performance!");
		c.addComment("dev.is-running-in-ide", "Enables behavior changes related to running in IDEs.\nWe override this to true during startup, but will be set to this value (default: false) when this file is loaded.");
		c.addComment("dev.enable-shared-constant-overrides", "Enables overriding vanilla's built in developer toggles. Most of these do not work!");
		c.addComment("dev.enable-class-generators", "Enables generation of class files, useful when updating/patching them!");
		// section: user toggles
		c.addComment("brand-name", "Set the software name the server reports to be using.");
		c.addComment("ItemMergeBehavior",
				"""
						Changes how item merging works
						Values:
						- -1: Normal (use the vanilla item stacking)
						-  0: Infinite (stack infinitely based on item type and metadata)
						-  1: No stacking (every separate item is its own entity, only for fun!)""");
		c.addComment("config-version", "Config version, do NOT modify this!");
		c.addComment("settings.checks.flight", "Toggles flight checks for players");
		c.addComment("settings.checks.vehicle-flight", "Toggles flight checks for players in vehicles");
		c.addComment("performance.disable-mob-spawners-below-tps", "Disable mob spawners spawning mobs when TPS drops below this value.");
		c.addComment("performance.disable-mob-spawning-below-tps", "Disable spawning mobs when TPS drops below this value.");
		c.addComment("performance.tps-treshold", "Disable these features when TPS drops below this value");
		c.addComment("performance.uncap-tps", "Disable the 20TPS limit. Warning: this will make your server run way faster and make it consume a lot of CPU!");
	}

	private static void removeLeftovers() {
		// this method is only to remove non-used values in the config

		// leftover from rainforest
		if (config.get("world-settings") != null) {
			set("world-settings", null);
		}
		if (config.get("allow-player-item-duplication") != null) {
			set("allow-player-item-duplication", null);
		}
		if (config.get("allow-ridable-chestable-duping") != null) {
			set("allow-ridable-chestable-duping", null);
		}
		if (config.get("allow-sand-duping") != null) {
			set("allow-sand-duping", null);
		}
		if (config.get("timings-url") != null) {
			set("timings-url", null);
		}
	}

	static void readConfig(Class<?> clazz, Object instance) {
		if(alreadyLoaded) return;
		for (Method method : clazz.getDeclaredMethods()) {
			if (Modifier.isPrivate(method.getModifiers())) {
				if (method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE) {
					try {
						method.setAccessible(true);
						method.invoke(instance);
					} catch (InvocationTargetException ex) {
						throw Throwables.propagate(ex.getCause());
					} catch (Exception ex) {
						Bukkit.getLogger().log(Level.SEVERE, "Error invoking " + method, ex);
					}
				}
			}
		}
		new File("config/").mkdirs();
		MobTPSThresholds.loadMobThresholds();
		try {
			config.save(CONFIG_FILE);
			c.saveComments(CONFIG_FILE);
		} catch (IOException ex) {
			Bukkit.getLogger().log(Level.SEVERE, "Could not save " + CONFIG_FILE, ex);
		}
		alreadyLoaded = true;
	}

	private static void set(String path, Object val) {
		config.set(path, val);
}

	private static boolean getBoolean(String path, boolean def) {
		config.addDefault(path, def);
		return config.getBoolean(path, config.getBoolean(path));
	}

	private static double getDouble(String path, double def) {
	    config.addDefault(path, def);
		return config.getDouble(path, config.getDouble(path));
	}

	private static float getFloat(String path, float def) {
	 	// TODO: Figure out why getFloat() always returns the default value.
		return (float) getDouble(path, (double) def);
	}

	static int getInt(String path, int def) {
		config.addDefault(path, def);
		return config.getInt(path, config.getInt(path));
	}

	private static <T> List<T> getList(String path, List<T> def) {
		config.addDefault(path, def);
		return (List<T>) config.getList(path, config.getList(path));
	}

	private static String getString(String path, String def) {
		config.addDefault(path, def);
		return config.getString(path, config.getString(path));
	}

	//define settings
	private static void classGenerators(){ if(getBoolean("dev.enable-class-generators", false)) ClassGenerators.Generate(); }

	public static boolean debug = false;
	private static void debug() {
		debug = getBoolean("dev.debug", false);
	}
	public static boolean isRunningInIDE = false;
	private static void setIsRunningInIDE() {
		isRunningInIDE = getBoolean("dev.is-running-in-ide", false);
		SharedConstants.IS_RUNNING_IN_IDE = isRunningInIDE;
	}
	/*public static boolean modSharedConstants = false;
	private static void setModSharedConstants() {
		modSharedConstants = getBoolean("dev.enable-shared-constant-overrides", false);
		if(modSharedConstants) SharedConstantOverridesConfig.Load();
	}*/

	public static boolean logPlayerLoginLoc = true;
	private static void general() {
		logPlayerLoginLoc = getBoolean("settings.log-player-login-location", logPlayerLoginLoc);
	}

	public static boolean fixProtocolLib = true;
	private static void protocolLib() {
		fixProtocolLib = getBoolean("settings.fix-protocollib", fixProtocolLib);
	}

	public static boolean disableEntityStuckChecks = false;
	private static void disableEntityStuckChecks() {
		disableEntityStuckChecks = getBoolean("settings.disableEntityStuckChecks", false);
	}

	public static String brandName = "Sugarcane";
		private static void brandName() {
		brandName = getString("brand-name", brandName);
        PurpurConfig.serverModName = brandName;
	}

	public static boolean checkFlying = true;
	public static boolean checkVehicleFlying = true;
	private static void flightChecks() {
		checkFlying = getBoolean("settings.checks.flight", checkFlying);
		checkVehicleFlying = getBoolean("settings.checks.vehicle-flight", checkVehicleFlying);
	}

	public static int itemStuckSleepTicks = 1;
	private static void itemStuckSleepTicks() {
		itemStuckSleepTicks = getInt("settings.itemStuckSleepTicks", 1);
	}

	public static int ItemMergeBehavior = -1;
    public static boolean SplitItems = false;
    private static void shouldItemsMerge() { ItemMergeBehavior = getInt("ItemMergeBehavior", -1); SplitItems = ItemMergeBehavior == 1; }

	public static int DisableMobSpawnerBelowTPS = 0;
	private static void disableMobSpawnerBelowTPS(){ DisableMobSpawnerBelowTPS = getInt("performance.tps-treshold.mob-spawners", 0); }
	public static boolean UncapTPS = true;
	private static void uncapTPS(){ UncapTPS = getBoolean("performance.uncap-tps", false); }

	public static boolean TickAllEntities = true;
	public static void setCheckEntityTickingRange(){ TickAllEntities = getBoolean("performance.tick-all-entities", TickAllEntities); }

	public static boolean DisableEntityTicking = false;
	public static void setDisableEntityTicking(){ DisableEntityTicking = getBoolean("performance.disable-entity-ticking", DisableEntityTicking); }
}