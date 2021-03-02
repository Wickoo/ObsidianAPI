package com.github.wickoo.obsidianapi.scoreboard;

import org.bukkit.scoreboard.Team;

abstract class Entry {

    abstract String getLine();
    abstract int getPosition();
    abstract Team getTeam();

}
