package com.github.wickoo.obsidianapi;

import com.github.wickoo.obsidianapi.commands.MainCMD;
import com.github.wickoo.obsidianapi.scoreboard.Flatboard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;

public final class ObsidianMain extends JavaPlugin implements Listener {

    private ObsidianMain obsidianMain;
    private ObsidianAPI obsidianAPI;


    @Override
    public void onEnable() {

        this.obsidianMain = this;
        this.obsidianAPI = new ObsidianAPI(this);

        this.saveDefaultConfig();

        new MainCMD(obsidianMain);
        getServer().getPluginManager().registerEvents(this, this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player =  event.getPlayer();
        Flatboard flatboard = Flatboard.fromPlayer(player);
        if (!(flatboard == null)) {
            flatboard.remove();
        }

    }

    @EventHandler
    public void onJoin(BlockBreakEvent event) {

        Player player =  event.getPlayer();

    }

    @EventHandler
    public void onJoin(BlockPlaceEvent event) {

        Player player =  event.getPlayer();

    }

}
