package com.github.wickoo.obsidianapi.commands;

import com.github.wickoo.obsidianapi.ObsidianAPI;
import com.github.wickoo.obsidianapi.ObsidianMain;
import com.github.wickoo.obsidianapi.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MainCMD implements CommandExecutor {

    private ObsidianMain plugin;

    public MainCMD(ObsidianMain plugin) {

        this.plugin = plugin;
        plugin.getCommand("obsidianapi").setExecutor(this);

    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String version = Bukkit.getVersion().substring(Bukkit.getVersion().lastIndexOf(':') + 1).replace(')', ' ');
        sender.sendMessage(Utils.chat("&5&lObsidianAPI &7by author &5Wick_\n&5Plugin Version: &7" + plugin.getDescription().getVersion() + "\n&5Command: &7/obsidianapi" + "\n&5Minecraft Version:&7" + version));

        ObsidianAPI.getInstance().spawnNPC("&e&lNPC", "Apple", ((Player) sender).getLocation());

        return true;

    }

}
