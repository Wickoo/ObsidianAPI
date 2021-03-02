package com.github.wickoo.obsidianapi.npc;

import com.github.wickoo.obsidianapi.disguise.Skin;
import com.github.wickoo.obsidianapi.utils.Utils;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

public class NPC {

    private JavaPlugin plugin;

    private EntityPlayer entityPlayer;
    private AdjustRunnable adjustRunnable;

    private String name;
    private UUID uuid;
    private int entityID;
    private Location location;
    private Skin skin;

    public NPC(JavaPlugin plugin, String name, Location location) {

        this.plugin = plugin;
        this.location = location;
        this.name = Utils.chat(name);

        initNPC();
        initRunnable();

    }

    public NPC(JavaPlugin plugin, String name, String skinName, Location location) {

        this.plugin = plugin;
        this.location = location;
        this.name =  Utils.chat(name);
        this.skin = Skin.getSkin(skinName);

        initNPC();
        initRunnable();

    }

    private void initNPC() {

        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle(); // Change "world" to the world the NPC should be spawned in.
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), Utils.chat(name)); // Change "playername" to the name the NPC should have, max 16 characters.
        entityPlayer = new EntityPlayer(nmsServer, nmsWorld, gameProfile, new PlayerInteractManager(nmsWorld)); // This will be the EntityPlayer (NPC) we send with the sendNPCPacket method.
        entityPlayer.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        uuid = entityPlayer.getUniqueID();
        entityID = entityPlayer.getId();

    }

    private void initRunnable() {

        adjustRunnable = new AdjustRunnable(plugin, this);
        adjustRunnable.runTaskTimer(plugin, 0, 5);

    }

    public void spawnNPC(Player player) {

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer)); // "Adds the player data for the client to use when spawning a player" - https://wiki.vg/Protocol#Spawn_Player
        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(entityPlayer)); // Spawns the NPC for the player client.
        connection.sendPacket(new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) (entityPlayer.yaw * 256 / 360))); // Correct head rotation when spawned in player look direction.

        DataWatcher watcher = entityPlayer.getDataWatcher();
        watcher.watch(10, (byte) 127);
        connection.sendPacket(new PacketPlayOutEntityMetadata(entityPlayer.getId(), watcher, true));

        new BukkitRunnable() {
            @Override
            public void run() {

                connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer));

            }
        }.runTaskLater(plugin, 40);

    }

    public void removeNPC(Player player) {

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutEntityDestroy(entityPlayer.getId()));

    }

    public void adjustLookDirection(Player player) {

        if (player.getUniqueId().toString().equals(entityPlayer.getUniqueID().toString())) {
            return;
        }

        if (calculateDistance(player) > 10) {
            return;
        }

        // Calculate the Yaw for the NPC (credit to https://www.spigotmc.org/threads/help-with-npcs-head-rotation.427251/)
        Vector difference = player.getLocation().subtract(entityPlayer.getBukkitEntity().getLocation()).toVector().normalize();
        byte yaw = (byte) MathHelper.d((Math.toDegrees(Math.atan2(difference.getZ(), difference.getX()) - Math.PI / 2) * 256.0F) / 360.0F);

        // Calculate the Pitch for the NPC (credit to https://www.spigotmc.org/threads/help-with-npcs-head-rotation.427251/)
        Vector height = entityPlayer.getBukkitEntity().getLocation().subtract(player.getLocation()).toVector().normalize();
        byte pitch = (byte) MathHelper.d((Math.toDegrees(Math.atan(height.getY())) * 256.0F) / 360.0F);

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutEntityHeadRotation(entityPlayer, yaw));
        connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(entityPlayer.getId(), yaw, pitch, true));


    }

    //(credit to https://www.spigotmc.org/threads/help-with-npcs-head-rotation.427251/)
    private double calculateDistance(Player p) {

        double diffX = entityPlayer.locX - p.getLocation().getX(), diffZ = entityPlayer.locZ - p.getLocation().getZ();
        double x = diffX < 0 ? (diffX * -1) : diffX, z = diffZ < 0 ? (diffZ * -1) : diffZ;
        return Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));

    }

    public Skin getSkin() {
        return skin;
    }

    public Location getLocation() {
        return location;
    }

    public EntityPlayer getEntityPlayer() {
        return entityPlayer;
    }

    public String getName() {
        return name;
    }

    public EntityPlayer getNpc() {
        return entityPlayer;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getEntityID() {
        return entityID;
    }
}
