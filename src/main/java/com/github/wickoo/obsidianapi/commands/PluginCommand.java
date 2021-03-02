package com.github.wickoo.obsidianapi.commands;

import org.bukkit.entity.Player;

public abstract class PluginCommand {

    public abstract String getName();
    public abstract String getPermission();
    public abstract String getUsage();
    public abstract String getDescription();
    public abstract boolean fromConsole();
    public abstract void executeCommand(Player player, String command, String[] args);

}
