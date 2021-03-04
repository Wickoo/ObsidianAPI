package com.github.wickoo.obsidianapi.disguise;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.github.wickoo.obsidianapi.packets.WrapperPlayServerPlayerInfo;
import com.github.wickoo.obsidianapi.packets.WrapperPlayServerRespawn;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class DisguiseManager implements Listener {

    private JavaPlugin plugin;
    private ProtocolManager protocolManager;
    private Map<UUID, Disguise> disguiseMap;

    public DisguiseManager(JavaPlugin plugin) {

        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.disguiseMap = new HashMap<>();

        addPacketListener();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

    }

    public boolean disguisePlayer (Player player, String disguiseName) {

        if (disguiseMap.containsKey(player.getUniqueId())) { undisguisePlayer(player); }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            Disguise disguise = Disguise.buildDisguise(player, disguiseName);

            if (disguise == null) {
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {

                disguiseMap.put(player.getUniqueId(), disguise);
                disguise.setActualSkin(getSkin(player));
                setPlayerName(player, disguiseName);
                setSkin(player, disguise.getDisguisedSkin());
                updatePlayer(player);

            });

        });

        return true;

    }

    public boolean undisguisePlayer (Player player) {

        if (!disguiseMap.containsKey(player.getUniqueId())) {
            return false;
        }

        Disguise disguise = disguiseMap.get(player.getUniqueId()).clone();

        disguiseMap.remove(player.getUniqueId());
        setPlayerName(player, disguise.getActualName());
        setSkin(player, disguise.getActualSkin());
        updatePlayer(player);
        return true;

    }

    public Skin getSkin(Player player) {

        WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);
        Multimap<String, WrappedSignedProperty> propertiesMap = gameProfile.getProperties();

        Skin skin = new Skin();
        skin.setTexture(propertiesMap.get("textures").iterator().next().getValue());
        skin.setSignature(propertiesMap.get("textures").iterator().next().getSignature());

        return skin;

    }

    public void setSkin(Player player, Skin skin) {

        WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);
        Multimap<String, WrappedSignedProperty> propertiesMap = gameProfile.getProperties();

        propertiesMap.removeAll("textures");
        String localTexture = skin.getTexture();
        String localSignature = skin.getSignature();

        WrappedSignedProperty textures = new WrappedSignedProperty("textures", localTexture, localSignature);
        propertiesMap.put("textures", textures);

    }

    public WrappedGameProfile buildProfile (UUID uuid, String name, Skin skin) {

        WrappedGameProfile gameProfile = new WrappedGameProfile(uuid, name);
        Multimap<String, WrappedSignedProperty> propertiesMap = gameProfile.getProperties();

        propertiesMap.removeAll("textures");
        WrappedSignedProperty textures = new WrappedSignedProperty("textures", skin.getTexture(), skin.getSignature());
        propertiesMap.put("textures", textures);

        return gameProfile;

    }

    public void setPlayerName(Player player, String name) {

        Class<?> craftPlayerClass = player.getClass();

        try {
            Method getProfileMethod = craftPlayerClass.getDeclaredMethod("getProfile");
            getProfileMethod.setAccessible(true);
            Object gameProfile = getProfileMethod.invoke(player);
            Field field = gameProfile.getClass().getDeclaredField("name");
            field.setAccessible(true);
            field.set(gameProfile, ChatColor.stripColor(name));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }

    }

    private void addPacketListener() {

        protocolManager.addPacketListener(new PacketAdapter(plugin,
                ListenerPriority.NORMAL,
                PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {

                if (event.getPacketType() == PacketType.Play.Server.PLAYER_INFO) {

                    //check type
                    EnumWrappers.PlayerInfoAction playerInfoAction = event.getPacket().getPlayerInfoAction().read(0);
                    if (playerInfoAction != EnumWrappers.PlayerInfoAction.ADD_PLAYER) { return; }

                    PacketContainer packet = event.getPacket();
                    List<PlayerInfoData> playerInfoDataList = packet.getPlayerInfoDataLists().read(0);
                    List<PlayerInfoData> newPlayerInfoDataList = new ArrayList<>();

                    //player trying to be added with packet
                    for (PlayerInfoData playerInfoData : playerInfoDataList) {

                        UUID loopUUID = playerInfoData.getProfile().getUUID();

                        if (!getDisguisedPlayers().containsKey(loopUUID)) {
                            newPlayerInfoDataList.add(playerInfoData);
                            continue;
                        }

                        Disguise disguise = getDisguisedPlayer(loopUUID);

                        // apply new skin
                        WrappedGameProfile gameProfile = playerInfoData.getProfile();
                        Multimap<String, WrappedSignedProperty> propertiesMap = gameProfile.getProperties();

                        String texture = disguise.getDisguisedSkin().getTexture();
                        String signature = disguise.getDisguisedSkin().getSignature();

                        propertiesMap.removeAll("textures");
                        WrappedSignedProperty textures = new WrappedSignedProperty("textures", texture, signature);
                        propertiesMap.put("textures", textures);

                        PlayerInfoData newData = new PlayerInfoData(gameProfile, playerInfoData.getLatency(), playerInfoData.getGameMode(), playerInfoData.getDisplayName());
                        newPlayerInfoDataList.add(newData);

                    }

                    //write back to packet
                    packet.getPlayerInfoDataLists().write(0, newPlayerInfoDataList);

                }

            }

        });

    }

    public void updatePlayer (Player player) {

        Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(player));
        Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(player));

        WrapperPlayServerRespawn respawn = new WrapperPlayServerRespawn();
        respawn.setDefaults(player);
        WrapperPlayServerPlayerInfo remove = new WrapperPlayServerPlayerInfo();
        remove.setDefaults(player, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
        WrapperPlayServerPlayerInfo add = new WrapperPlayServerPlayerInfo();
        add.setDefaults(player, EnumWrappers.PlayerInfoAction.ADD_PLAYER);


        try {
            protocolManager.sendServerPacket(player, remove.getHandle());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        final boolean flying = player.isFlying();
        final Location location = player.getLocation();
        final int level = player.getLevel();
        final float xp = player.getExp();
        final double maxHealth = player.getMaxHealth();
        final double health = player.getHealth();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            try {

                protocolManager.sendServerPacket(player, respawn.getHandle());

                player.setFlying(flying);
                player.teleport(location);
                player.updateInventory();
                player.setLevel(level);
                player.setExp(xp);
                player.setMaxHealth(maxHealth);
                player.setHealth(health);

                protocolManager.sendServerPacket(player, add.getHandle());

            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }, 1);

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        if (!isDisguised(player)) { return; }

        Disguise disguise = disguiseMap.get(player.getUniqueId());
        setPlayerName(player, disguise.getDisguisedName());
        setSkin(player, disguise.getDisguisedSkin());

    }

    public boolean isDisguised(Player player) {
        return disguiseMap.containsKey(player.getUniqueId());
    }

    public Map<UUID, Disguise> getDisguisedPlayers () { return disguiseMap; }

    public Disguise getDisguisedPlayer (UUID uuid) { return disguiseMap.get(uuid); }

}

