package com.github.wickoo.obsidianapi.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuManager implements Listener {

    private JavaPlugin plugin;
    private Map<UUID, Menu> playersInMenu;

    public MenuManager (JavaPlugin plugin) {

        this.plugin = plugin;
        this.playersInMenu = new HashMap<>();
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);

    }

    @EventHandler
    public void onLeave(InventoryCloseEvent event) {

        Player player = (Player) event.getPlayer();

        Menu menu = getFromPlayer(player);

        if (menu == null) { return; }

        menu.onClose(this);

    }

    public Map<UUID, Menu> getPlayersInMenu() {
        return playersInMenu;
    }

    public Menu getFromPlayer(Player player) { return playersInMenu.getOrDefault(player.getUniqueId(), null); }

}
