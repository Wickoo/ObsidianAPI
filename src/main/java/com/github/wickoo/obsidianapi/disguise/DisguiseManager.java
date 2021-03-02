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
import com.github.wickoo.obsidianapi.packets.*;
import com.github.wickoo.obsidianapi.utils.PlayerUtils;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

    public boolean disguisePlayer (Player player, String disguiseName) throws ExecutionException, InterruptedException {

        CompletableFuture<Disguise> completableFuture = CompletableFuture.supplyAsync(() -> Disguise.buildDisguise(player, disguiseName)).thenApply(disguise -> {

            if (disguise == null) {
                return null;
            }

            disguiseMap.put(player.getUniqueId(), disguise);
            setPlayerName(player, disguiseName);
            updateDisguise(player);
            return disguise;

        });

        return completableFuture.get() != null;

    }

    public boolean undisguisePlayer (Player player) {

        if (!disguiseMap.containsKey(player.getUniqueId())) {
            return false;
        }

        Disguise disguise = disguiseMap.get(player.getUniqueId()).clone();

        disguiseMap.remove(player.getUniqueId());
        setPlayerName(player, disguise.getActualName());
        clearDisguise(player, disguise);
        return true;

    }

    private void setDisguiseSkin(Player player) {

        WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);
        Multimap<String, WrappedSignedProperty> propertiesMap = gameProfile.getProperties();

        Disguise disguise = this.getDisguisedPlayer(player.getUniqueId());
        Skin skin = new Skin();
        skin.setTexture(propertiesMap.get("textures").iterator().next().getValue());
        skin.setSignature(propertiesMap.get("textures").iterator().next().getSignature());
        disguise.setActualSkin(skin);

        propertiesMap.removeAll("textures");
        String localTexture = disguise.getDisguisedSkin().getTexture();
        String localSignature = disguise.getDisguisedSkin().getSignature();

        WrappedSignedProperty textures = new WrappedSignedProperty("textures", localTexture, localSignature);
        propertiesMap.put("textures", textures);

    }

    private WrappedGameProfile buildProfile (Player player, String name, Skin skin) {

        final WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);
        Multimap<String, WrappedSignedProperty> propertiesMap = gameProfile.getProperties();

        propertiesMap.removeAll("textures");
        WrappedSignedProperty textures = new WrappedSignedProperty("textures", skin.getTexture(), skin.getSignature());
        propertiesMap.put("textures", textures);

        WrappedGameProfile newProfile = WrappedGameProfile.fromPlayer(player).withName(name);
        newProfile.getProperties().putAll(gameProfile.getProperties());
        return newProfile;

    }

    private void updateDisguise(Player disguisedPlayer) {

        Disguise disguise = getDisguisedPlayer(disguisedPlayer.getUniqueId());
        setDisguiseSkin(disguisedPlayer);
        setPlayerName(disguisedPlayer, disguise.getDisguisedName());
        disguisedPlayer.setDisplayName(getDisguisedPlayers().get(disguisedPlayer.getUniqueId()).getDisguisedName());

        // remove player packet
        WrapperPlayServerPlayerInfo serverInfoRemove = new WrapperPlayServerPlayerInfo();
        serverInfoRemove.setAction(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
        List<PlayerInfoData> playerInfoDataListOld = serverInfoRemove.getData();
        PlayerInfoData playerInfoDataOld = new PlayerInfoData(buildProfile(disguisedPlayer, disguise.getActualName(), disguise.getActualSkin()),
                PlayerUtils.getPing(disguisedPlayer), EnumWrappers.NativeGameMode.fromBukkit(disguisedPlayer.getGameMode()), null);
        playerInfoDataListOld.add(playerInfoDataOld);
        serverInfoRemove.setData(playerInfoDataListOld);

        // destroy entity packet
        WrapperPlayServerEntityDestroy packetDestroyEntity = new WrapperPlayServerEntityDestroy();
        packetDestroyEntity.setEntityIds(new int[]{disguisedPlayer.getEntityId()});

        // add player packet
        WrapperPlayServerPlayerInfo serverInfoAdd = new WrapperPlayServerPlayerInfo();
        serverInfoAdd.setAction(EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        List<PlayerInfoData> playerInfoDataListNew = serverInfoAdd.getData();
        PlayerInfoData playerInfoDataNew = new PlayerInfoData(buildProfile(disguisedPlayer, disguise.getDisguisedName(), disguise.getDisguisedSkin()),
                PlayerUtils.getPing(disguisedPlayer), EnumWrappers.NativeGameMode.fromBukkit(disguisedPlayer.getGameMode()), null);
        playerInfoDataListNew.add(playerInfoDataNew);
        serverInfoAdd.setData(playerInfoDataListNew);

        //send to everyone
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            if (otherPlayer.getUniqueId().equals(disguisedPlayer.getUniqueId())) {
                continue;
            }
            try {
                protocolManager.sendServerPacket(otherPlayer, serverInfoRemove.getHandle());
                protocolManager.sendServerPacket(otherPlayer, packetDestroyEntity.getHandle());
                protocolManager.sendServerPacket(otherPlayer, serverInfoAdd.getHandle());
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }

        //update equipment
        updatePlayer(disguisedPlayer);

    }

    private void clearDisguise(Player disguisedPlayer, Disguise disguise) {

        // remove disguised player
        WrapperPlayServerPlayerInfo serverInfoRemove = new WrapperPlayServerPlayerInfo();
        serverInfoRemove.setAction(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
        List<PlayerInfoData> playerInfoDataListOld = serverInfoRemove.getData();
        PlayerInfoData playerInfoDataOld = new PlayerInfoData(buildProfile(disguisedPlayer, disguise.getDisguisedName(), disguise.getDisguisedSkin()),
                PlayerUtils.getPing(disguisedPlayer), EnumWrappers.NativeGameMode.fromBukkit(disguisedPlayer.getGameMode()), null);
        playerInfoDataListOld.add(playerInfoDataOld);
        serverInfoRemove.setData(playerInfoDataListOld);

        // destory entity packet
        WrapperPlayServerEntityDestroy packetDestroyEntity = new WrapperPlayServerEntityDestroy();
        packetDestroyEntity.setEntityIds(new int[]{disguisedPlayer.getEntityId()});

        // add player packet
        WrapperPlayServerPlayerInfo serverInfoAdd = new WrapperPlayServerPlayerInfo();
        serverInfoAdd.setAction(EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        List<PlayerInfoData> playerInfoDataListNew = serverInfoAdd.getData();
        PlayerInfoData playerInfoDataNew = new PlayerInfoData(buildProfile(disguisedPlayer, disguise.getActualName(), disguise.getActualSkin()),
                PlayerUtils.getPing(disguisedPlayer), EnumWrappers.NativeGameMode.fromBukkit(disguisedPlayer.getGameMode()), null);
        playerInfoDataListNew.add(playerInfoDataNew);
        serverInfoAdd.setData(playerInfoDataListNew);

        //send to everyone
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            if (otherPlayer.getUniqueId().equals(disguisedPlayer.getUniqueId())) {
                continue;
            }
            try {
                protocolManager.sendServerPacket(otherPlayer, serverInfoRemove.getHandle());
                protocolManager.sendServerPacket(otherPlayer, packetDestroyEntity.getHandle());
                protocolManager.sendServerPacket(otherPlayer, serverInfoAdd.getHandle());
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }

        //update equipment
        updatePlayer(disguisedPlayer);

    }

    private void setPlayerName(Player player, String name) {

        player.setDisplayName(name);
        player.setPlayerListName(name);
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

    private void updatePlayer (Player player) {

        for (Player other : Bukkit.getOnlinePlayers()) {

            if (other.equals(player)) {
                continue;
            }

            try {
                protocolManager.sendServerPacket(other, new WrapperPlayServerNamedEntitySpawn().setDefaults(player).getHandle());
                protocolManager.sendServerPacket(other, new WrapperPlayServerEntityMetadata().setDefaults(player).getHandle());
                protocolManager.sendServerPacket(other, new WrapperPlayServerEntityHeadRotation().setDefaults(player).getHandle());
                protocolManager.sendServerPacket(other, new WrapperPlayServerEntityLook().setDefaults(player).getHandle());

                WrapperPlayServerEntityEquipment.getPackets(player).forEach(packet -> {
                    try {
                        protocolManager.sendServerPacket(other, packet.getHandle());
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });

            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        undisguisePlayer(player); //method checks if theyre disguised

    }

    public Map<UUID, Disguise> getDisguisedPlayers () { return disguiseMap; }

    public Disguise getDisguisedPlayer (UUID uuid) { return disguiseMap.get(uuid); }

}

