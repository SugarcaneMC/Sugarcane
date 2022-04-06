package org.sugarcanemc.sugarcane.config;

import org.apache.commons.lang.BooleanUtils;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.server.level.ServerLevel;
import org.sugarcanemc.sugarcane.util.yaml.BaseYamlConfig;

import java.util.List;

@SuppressWarnings("unused")
public class SugarcaneWorldConfig extends BaseYamlConfig {

	private final ServerLevel level;
	private final String worldName;
	private final World.Environment environment;

	public SugarcaneWorldConfig(ServerLevel level, String worldName, World.Environment environment) {
		this.level = level;
		this.worldName = worldName;
		this.environment = environment;
		init();
	}

	public void init() {
		log("-------- World Settings For [" + worldName + "] --------");
		SugarcaneConfig.readConfig(SugarcaneWorldConfig.class, this);
	}

	private void set(String path, Object val) {
		SugarcaneConfig.config.addDefault("world-settings.default." + path, val);
		SugarcaneConfig.config.set("world-settings.default." + path, val);
		if (SugarcaneConfig.config.get("world-settings." + worldName + "." + path) != null) {
			SugarcaneConfig.config.addDefault("world-settings." + worldName + "." + path, val);
			SugarcaneConfig.config.set("world-settings." + worldName + "." + path, val);
		}
	}

	private ConfigurationSection getConfigurationSection(String path) {
		ConfigurationSection section = SugarcaneConfig.config.getConfigurationSection("world-settings." + worldName + "." + path);
		return section != null ? section : SugarcaneConfig.config.getConfigurationSection("world-settings.default." + path);
	}

	private String getString(String path, String def) {
		SugarcaneConfig.config.addDefault("world-settings.default." + path, def);
		return SugarcaneConfig.config.getString("world-settings." + worldName + "." + path, SugarcaneConfig.config.getString("world-settings.default." + path));
	}

	private boolean getBoolean(String path, boolean def) {
		SugarcaneConfig.config.addDefault("world-settings.default." + path, def);
		return SugarcaneConfig.config.getBoolean("world-settings." + worldName + "." + path, SugarcaneConfig.config.getBoolean("world-settings.default." + path));
	}

	private boolean getBoolean(String path, Predicate<Boolean> predicate) {
		String val = getString(path, "default").toLowerCase();
		Boolean bool = BooleanUtils.toBooleanObject(val, "true", "false", "default");
		return predicate.test(bool);
	}

	private double getDouble(String path, double def) {
		SugarcaneConfig.config.addDefault("world-settings.default." + path, def);
		return SugarcaneConfig.config.getDouble("world-settings." + worldName + "." + path, SugarcaneConfig.config.getDouble("world-settings.default." + path));
	}

	private int getInt(String path, int def) {
		SugarcaneConfig.config.addDefault("world-settings.default." + path, def);
		return SugarcaneConfig.config.getInt("world-settings." + worldName + "." + path, SugarcaneConfig.config.getInt("world-settings.default." + path));
	}

	private <T> List<?> getList(String path, T def) {
		SugarcaneConfig.config.addDefault("world-settings.default." + path, def);
		return SugarcaneConfig.config.getList("world-settings." + worldName + "." + path, SugarcaneConfig.config.getList("world-settings.default." + path));
	}
}