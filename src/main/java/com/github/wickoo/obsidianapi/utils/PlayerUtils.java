package com.github.wickoo.obsidianapi.utils;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PlayerUtils {

    public static int getPing (Player player) {

        Class<?> craftPlayerClass = player.getClass();

        try {
            Method getHandle = craftPlayerClass.getDeclaredMethod("getHandle");
            getHandle.setAccessible(true);
            Object entityPlayer = getHandle.invoke(player);
            Field ping = entityPlayer.getClass().getDeclaredField("ping");
            return ping.getInt(entityPlayer);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
            return 5;
        }

    }

    public static void sendTitle (Player player, String message, net.md_5.bungee.api.ChatColor chatColor) {

        IChatBaseComponent chatTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\",color:" + chatColor.name().toLowerCase() + "}");

        PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, chatTitle);
        PacketPlayOutTitle length = new PacketPlayOutTitle(10, 10, 10);
        //fade in, duration, fade out


        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(length);

    }

    public static void heal(Player player) {

        player.setFireTicks(0);
        player.setExp(0);
        player.setHealth(20);
        player.setFoodLevel(20);
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }

    }

}
