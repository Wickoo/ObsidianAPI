package com.github.wickoo.obsidianapi;

import com.github.wickoo.obsidianapi.disguise.DisguiseManager;
import com.github.wickoo.obsidianapi.menu.MenuManager;
import com.github.wickoo.obsidianapi.mysql.SQLConnection;
import com.github.wickoo.obsidianapi.npc.NPC;
import com.github.wickoo.obsidianapi.npc.NPCManager;
import com.github.wickoo.obsidianapi.scoreboard.Flatboard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class ObsidianAPI {

    private static ObsidianAPI obsidianAPI;

    private JavaPlugin plugin;
    private SQLConnection sqlConnection;
    private NPCManager npcManager;
    private MenuManager menuManager;
    private DisguiseManager disguiseManager;

    public ObsidianAPI (JavaPlugin plugin) {

        this.plugin = plugin;
        obsidianAPI = this;

        this.sqlConnection = new SQLConnection(plugin);
        this.npcManager = new NPCManager(plugin);
        this.menuManager = new MenuManager(plugin);
        this.disguiseManager = new DisguiseManager(plugin);

    }

    public Flatboard createFlatboard (String name, UUID uuid, int delay) {
        return new Flatboard(name, uuid, delay);
    }

    public NPC spawnNPC(String name, Location location) {
        NPC npc = new NPC(plugin, name, location);
        npcManager.getNPCs().add(npc);
        Bukkit.getOnlinePlayers().forEach(npc::spawnNPC);
        return npc;
    }

    public NPC spawnNPC(String name, String skinName, Location location) {
        NPC npc = new NPC(plugin, name, skinName, location);
        npcManager.getNPCs().add(npc);
        Bukkit.getOnlinePlayers().forEach(npc::spawnNPC);
        return npc;
    }

    public void deleteNPC(String name) {
        NPC npc = npcManager.getNPCs().stream().filter(n -> n.getName().equals(name)).findFirst().orElse(null);
        if (npc == null) { return; }
        npcManager.getNPCs().remove(npc);
        Bukkit.getOnlinePlayers().forEach(npc::removeNPC);
    }

    public void test() {

    }

    public static ObsidianAPI getInstance() { return obsidianAPI; }

    public SQLConnection getSQLConnection() { return sqlConnection; }

    public DisguiseManager getDisguiseManager() { return disguiseManager; }

}
