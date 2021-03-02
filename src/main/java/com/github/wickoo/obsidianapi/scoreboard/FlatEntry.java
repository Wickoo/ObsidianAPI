package com.github.wickoo.obsidianapi.scoreboard;

import com.github.wickoo.obsidianapi.utils.Utils;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

public class FlatEntry extends Entry {

    private Team team;
    private String identifier;
    private String line;
    private int position;

    public FlatEntry (Flatboard flatboard, String line, int position) {

        this.identifier = "&f&" + Utils.getColourCodes().get(position) + "&r";
        this.team = flatboard.getScoreboard().registerNewTeam(identifier);
        this.line = Utils.chat(line);
        this.position = position;

        handleString();

    }

    private void handleString () {

        if (!this.line.equals("")) {
            return;
        }

        StringBuilder newLine = new StringBuilder();
        for (int i = 0; i < position; i++) {
            newLine.append(" ");
        }
        this.line = newLine.toString();

    }

    public void apply (Objective objective) {

        if (line.length() <= 16) {

            team.addEntry(line);
            objective.getScore(line).setScore(position);

        } else if (line.length() <= 32) {

            line = line.substring(0, Math.min(32, line.length()));
            team.setPrefix(line.substring(0, 16));
            team.addEntry(line.substring(16));
            objective.getScore(line).setScore(position);

        } else {

            line = line.substring(0, Math.min(48, line.length()));
            team.setPrefix(line.substring(0, 16));
            team.addEntry(line.substring(16, 32));
            team.setSuffix(line.substring(32));
            objective.getScore(line.substring(16,32)).setScore(position);

        }

    }

    @Override
    public String getLine() {
        return line;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    Team getTeam() {
        return null;
    }
}
