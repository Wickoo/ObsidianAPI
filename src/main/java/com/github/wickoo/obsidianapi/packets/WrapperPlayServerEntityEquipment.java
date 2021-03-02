package com.github.wickoo.obsidianapi.packets;

/**
 * PacketWrapper - ProtocolLib wrappers for Minecraft packets
 * Copyright (C) dmulloy2 <http://dmulloy2.net>
 * Copyright (C) Kristian S. Strangeland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.Pair;
import com.github.wickoo.obsidianapi.utils.Utils;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WrapperPlayServerEntityEquipment extends AbstractPacket {
    public static final PacketType TYPE =
            PacketType.Play.Server.ENTITY_EQUIPMENT;

    public WrapperPlayServerEntityEquipment() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperPlayServerEntityEquipment(PacketContainer packet) {
        super(packet, TYPE);
    }

    private WrapperPlayServerEntityEquipment setDefaults(Player player, ItemSlot slot, ItemStack item) {

        setEntityID(player.getEntityId());
        setItem(item);

        if (Utils.getMinecraftVersion().contains("1.8.8")) {
            setSlotLegacy(slot);
            return this;
        }

        setSlot(slot);
        return this;
    }

    public static List<WrapperPlayServerEntityEquipment> getPackets (Player player) {

        List<WrapperPlayServerEntityEquipment> packets = new ArrayList<>();

        if (Utils.getMinecraftVersion().contains("1.16")) {

            WrapperPlayServerEntityEquipment entityEquipment = new WrapperPlayServerEntityEquipment();
            List<Pair<ItemSlot, ItemStack>> list = new ArrayList<>();
            list.add(new Pair<>(EnumWrappers.ItemSlot.HEAD, player.getInventory().getHelmet()));
            list.add(new Pair<>(EnumWrappers.ItemSlot.CHEST, player.getInventory().getChestplate()));
            list.add(new Pair<>(EnumWrappers.ItemSlot.LEGS, player.getInventory().getLeggings()));
            list.add(new Pair<>(EnumWrappers.ItemSlot.FEET, player.getInventory().getBoots()));
            list.add(new Pair<>(EnumWrappers.ItemSlot.MAINHAND, player.getInventory().getItemInMainHand()));
            list.add(new Pair<>(EnumWrappers.ItemSlot.OFFHAND, player.getInventory().getItemInOffHand()));
            entityEquipment.setItems(list);
            entityEquipment.setEntityID(player.getEntityId());
            packets.add(entityEquipment);
            return packets;
        }

        if (!Utils.getMinecraftVersion().contains("1.8.8")) {
            packets.add(new WrapperPlayServerEntityEquipment().setDefaults(player, EnumWrappers.ItemSlot.OFFHAND, player.getInventory().getItemInOffHand()));
        }

        packets.add(new WrapperPlayServerEntityEquipment().setDefaults(player, EnumWrappers.ItemSlot.HEAD, player.getInventory().getHelmet()));
        packets.add(new WrapperPlayServerEntityEquipment().setDefaults(player, EnumWrappers.ItemSlot.CHEST, player.getInventory().getChestplate()));
        packets.add(new WrapperPlayServerEntityEquipment().setDefaults(player, EnumWrappers.ItemSlot.LEGS, player.getInventory().getLeggings()));
        packets.add(new WrapperPlayServerEntityEquipment().setDefaults(player, EnumWrappers.ItemSlot.FEET, player.getInventory().getBoots()));
        packets.add(new WrapperPlayServerEntityEquipment().setDefaults(player, EnumWrappers.ItemSlot.MAINHAND, player.getInventory().getItemInHand()));

        return packets;

    }

    /**
     * Retrieve Entity ID.
     * <p>
     * Notes: entity's ID
     *
     * @return The current Entity ID
     */
    public int getEntityID() {
        return handle.getIntegers().read(0);
    }

    /**
     * Set Entity ID.
     *
     * @param value - new value.
     */
    public void setEntityID(int value) {
        handle.getIntegers().write(0, value);
    }

    /**
     * Retrieve the entity of the painting that will be spawned.
     *
     * @param world - the current world of the entity.
     * @return The spawned entity.
     */
    public Entity getEntity(World world) {
        return handle.getEntityModifier(world).read(0);
    }

    /**
     * Retrieve the entity of the painting that will be spawned.
     *
     * @param event - the packet event.
     * @return The spawned entity.
     */
    public Entity getEntity(PacketEvent event) {
        return getEntity(event.getPlayer().getWorld());
    }

    /**
     * Retrieve Item.
     * <p>
     * Notes: item in slot format
     *
     * @return The current Item
     */
    public List<Pair<ItemSlot, ItemStack>> getItems() {

        return handle.getSlotStackPairLists().read(0);
    }

    /**
     * Set Item.
     *
     * @param value - new value.
     */
    public void setItems(List<Pair<ItemSlot, ItemStack>> value) {
        handle.getSlotStackPairLists().write(0, value);
    }

    public ItemSlot getSlot() {
        return handle.getItemSlots().read(0);
    }

    public void setSlot(ItemSlot value) {
        handle.getItemSlots().write(0, value);
    }

    public void setSlotLegacy (ItemSlot value) {
        handle.getIntegers().write(1, fromEnum(value));
    }

    /**
     * Retrieve Item.
     * <p>
     * Notes: item in slot format
     *
     * @return The current Item
     */
    public ItemStack getItem() {
        return handle.getItemModifier().read(0);
    }

    /**
     * Set Item.
     *
     * @param value - new value.
     */
    public void setItem(ItemStack value) {
        handle.getItemModifier().write(0, value);
    }

    private int fromEnum (ItemSlot slot) {
        switch (slot) {
            case HEAD:
                return 4;
            case CHEST:
                return 3;
            case LEGS:
                return 2;
            case FEET:
                return 1;
            default:
                return 0;

        }
    }

}
