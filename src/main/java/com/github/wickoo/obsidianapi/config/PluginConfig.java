package com.github.wickoo.obsidianapi.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class PluginConfig {

    private File file;
    private FileConfiguration config;
    private JavaPlugin plugin;
    private String filename;

    public PluginConfig(JavaPlugin plugin, String filename) {
        this.plugin = plugin;
        this.filename = filename;
    }

    public void saveConfig() {
        try {
            this.config.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createCustomConfig() {
        this.file = new File(this.plugin.getDataFolder(), this.filename);

        if (!this.file.exists()) {

            this.file.getParentFile().mkdirs();
            this.plugin.saveResource(this.filename, false);
        }


        this.config = new YamlConfiguration();

        try {
            this.config.load(this.file);
        } catch (IOException|org.bukkit.configuration.InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return this.config;
    }
}



