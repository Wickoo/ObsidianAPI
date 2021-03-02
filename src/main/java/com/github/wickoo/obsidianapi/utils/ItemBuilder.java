package com.github.wickoo.obsidianapi.utils;

import com.mojang.authlib.GameProfile;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemBuilder {

    private ItemStack itemStack;

    private ItemBuilder () { }

    public ItemStack buildItem (Material material, int data, int amount) {
        itemStack = new ItemStack(material, amount, (short) data);
        return itemStack;
    }

    public ItemStack buildItem (Material material) {
        itemStack = new ItemStack(material);
        return itemStack;
    }

    public ItemStack setName (String name) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(Utils.chat(name));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public ItemStack setGlow () {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addEnchant(Enchantment.DIG_SPEED, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public ItemStack setLore (String... lore) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> loreList = new ArrayList<>();
        for (String string : lore) {
            loreList.add(Utils.chat(string));
        }
        itemMeta.setLore(loreList);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    //needs to be SKULL_ITEM with data = 3

    public ItemStack setSkin (Player player) {

        if (!itemStack.getType().equals(Material.PLAYER_HEAD)) {
            return itemStack;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        GameProfile playerProfile = ((CraftPlayer) player).getProfile();
        profile.getProperties().replaceValues("textures", playerProfile.getProperties().get("textures"));
        Field profileField = null;
        try {
            profileField = itemMeta.getClass().getDeclaredField("profile");
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        profileField.setAccessible(true);
        try {
            profileField.set(itemMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
