package com.github.wickoo.obsidianapi.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {

    public static String chat (String s) {
        return ChatColor.translateAlternateColorCodes('&',s);
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;

    }

    public static String getMinecraftVersion () { return Bukkit.getVersion().substring(Bukkit.getVersion().lastIndexOf(':') + 1).replace(')', ' '); }

    public static List<String> getColourCodes() {
        return new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e", "f", "k", "l", "m", "n", "o", "r", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
    }

}

