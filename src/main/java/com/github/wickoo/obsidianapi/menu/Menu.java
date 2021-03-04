package com.github.wickoo.obsidianapi.menu;

import com.github.wickoo.obsidianapi.ObsidianAPI;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public abstract class Menu {

    public abstract Player getPlayer();

    public abstract Inventory getInventory();

    public void onClose() { ObsidianAPI.getInstance().getMenuManager().getPlayersInMenu().remove(getPlayer().getUniqueId()); }

    public void initInventory() { ObsidianAPI.getInstance().getMenuManager().getPlayersInMenu().put(getPlayer().getUniqueId(), this); }

    public void processClick(InventoryClickEvent event) {

        if (event.getCurrentItem() == null) {
            return;
        }

        event.setCancelled(true);
        getPlayer().playSound(getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

    }

}
