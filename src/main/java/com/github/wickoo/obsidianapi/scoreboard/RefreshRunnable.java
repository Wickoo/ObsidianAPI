package com.github.wickoo.obsidianapi.scoreboard;

import org.bukkit.scheduler.BukkitRunnable;

public class RefreshRunnable extends BukkitRunnable {

    private Flatboard flatBoard;

    public RefreshRunnable(Flatboard flatBoard) {
        this.flatBoard = flatBoard;
    }

    @Override
    public void run() {
        flatBoard.update();
    }

}
