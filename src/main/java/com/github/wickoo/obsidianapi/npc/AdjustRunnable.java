package com.github.wickoo.obsidianapi.npc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AdjustRunnable extends BukkitRunnable {

    private JavaPlugin plugin;
    private NPC npc;

    public AdjustRunnable(JavaPlugin plugin, NPC npc) {

        this.plugin = plugin;
        this.npc = npc;

    }

    @Override
    public void run() {

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (!player.getWorld().equals(npc.getLocation().getWorld())) {
                return;
            }

            npc.adjustLookDirection(player);

        }

    }
}
