package com.github.wickoo.obsidianapi.scoreboard;

import com.github.wickoo.obsidianapi.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import java.util.function.Function;

public abstract class DynamicEntry extends Entry {

    private Team team;
    private String identifier;
    private int position;

    private Function<?, String> func;

    public DynamicEntry (Flatboard flatBoard, String name) {

        this.team = flatBoard.getScoreboard().registerNewTeam(name);

    }

    public void apply (Objective objective) {

        this.identifier = "&f&" + Utils.getColourCodes().get(position) + "&r";
        team.addEntry(Utils.chat(identifier));
        updateLine();
        objective.getScore(Utils.chat(identifier)).setScore(position);

    }

    public void updateLine () {

        String line = Utils.chat(getLine());

        if (line.length() <= 16) {

            team.setPrefix(line);

        } else {

            line = line.substring(0, Math.min(30, line.length()));
            team.setPrefix(line.substring(0, 16));
            team.setSuffix(ChatColor.getLastColors(line.substring(0, 16)) + line.substring(16));

        }

    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public abstract String getLine();

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public Team getTeam() {
        return team;
    }
}
