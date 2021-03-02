package com.github.wickoo.obsidianapi.scoreboard;

import com.github.wickoo.obsidianapi.ObsidianMain;
import com.github.wickoo.obsidianapi.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class Flatboard {

    private static Map<UUID, Flatboard> flatBoardMap = new HashMap<>();

    private ObsidianMain plugin;
    private Scoreboard scoreboard;
    private Objective objective;
    private List<Entry> entries;

    private UUID uuid;
    private int updateDelay;

    private RefreshRunnable refreshRunnable;

    public Flatboard(String name, UUID uuid, int updateDelay) {

        this.plugin = JavaPlugin.getPlugin(ObsidianMain.class);

        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("ServerName", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(Utils.chat(name));

        this.updateDelay = updateDelay;
        this.uuid = uuid;
        this.entries = new ArrayList<>();

        flatBoardMap.put(uuid, this);

    }

    public void display () {

        for (Entry entry : getEntries()) {

            if (entry instanceof FlatEntry) {
                FlatEntry flatEntry = (FlatEntry) entry;
                flatEntry.apply(objective);
                continue;
            }

            DynamicEntry dynamicEntry = (DynamicEntry) entry;
            dynamicEntry.apply(objective);

        }

        refreshRunnable = new RefreshRunnable(this);
        refreshRunnable.runTaskTimer(plugin, updateDelay, updateDelay);
        Bukkit.getPlayer(uuid).setScoreboard(scoreboard);

    }

    protected void update() {

        for (Entry entry : getEntries()) {

            if (entry instanceof FlatEntry) {
                continue;
            }

            DynamicEntry dynamicEntry = (DynamicEntry) entry;
            dynamicEntry.updateLine();

        }

    }

    public void remove() {
        refreshRunnable.cancel();
        Player player = Bukkit.getPlayer(uuid);
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        Flatboard.getFlatBoardMap().remove(uuid);
    }

    public void addFlatEntry (String line) {
        getEntries().add(new FlatEntry(this, line, getNextInt()));
    }

    public void addDynamicEntry (DynamicEntry dynamicEntry) {
        dynamicEntry.setPosition(getNextInt());
        getEntries().add(dynamicEntry);
    }

    public void addBlank () {
        getEntries().add(new FlatEntry(this, "", getNextInt()));
    }

    private int getNextInt () {
        return 16 - getEntries().size();
    }

    protected List<Entry> getEntries() { return entries; }

    protected Scoreboard getScoreboard () { return scoreboard; }

    protected static Map<UUID, Flatboard> getFlatBoardMap() {
        return flatBoardMap;
    }

    public static Flatboard fromPlayer(Player player) {
        return flatBoardMap.getOrDefault(player.getUniqueId(), null);
    }
}
