package com.github.wickoo.obsidianapi.npc;

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
import com.github.wickoo.obsidianapi.disguise.Skin;
import com.github.wickoo.obsidianapi.events.NPCInteractEvent;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NPCManager implements Listener {

    private JavaPlugin plugin;
    private ProtocolManager protocolManager;

    private List<NPC> npcs;

    public NPCManager(JavaPlugin plugin) {

        this.plugin = plugin;
        this.npcs = new ArrayList<>();
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        addProtocolListener();

    }

    public WrappedGameProfile getWrappedProfile(String name, UUID uuid, Skin skin) {

        WrappedGameProfile newGameProfile = new WrappedGameProfile(uuid, name).withName(name);
        Multimap<String, WrappedSignedProperty> newPropertiesMap = newGameProfile.getProperties();
        newPropertiesMap.removeAll("textures");
        WrappedSignedProperty textures = new WrappedSignedProperty("textures", skin.getTexture(), skin.getSignature());
        newPropertiesMap.put("textures", textures);
        return newGameProfile;

    }

    public List<NPC> getNPCs() {
        return npcs;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        npcs.forEach(n -> n.spawnNPC(player));
    }

    public void addProtocolListener() {

        protocolManager.addPacketListener(new PacketAdapter(plugin,
                ListenerPriority.NORMAL,
                PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {

                if (event.getPacketType() == PacketType.Play.Server.PLAYER_INFO) {

                    //player recieving packet
                    Player player = event.getPlayer();

                    PacketContainer packet = event.getPacket();
                    List<PlayerInfoData> playerInfoDataList = packet.getPlayerInfoDataLists().read(0);

                    //player trying to be added with packet
                    PlayerInfoData oldData = playerInfoDataList.get(0);

                    for (NPC npc : npcs) {

                        //check if attempted add is the npc
                        if (!oldData.getProfile().getUUID().equals(npc.getUuid())) {
                            continue;
                        }

                        if (npc.getSkin() != null) {

                            // get game profile with skin
                            WrappedGameProfile newGameProfile = getWrappedProfile(npc.getName(), npc.getUuid(), npc.getSkin());
                            PlayerInfoData newData = new PlayerInfoData(newGameProfile, oldData.getLatency(), oldData.getGameMode(), oldData.getDisplayName());

                            //clear list and add new profile
                            playerInfoDataList.clear();
                            playerInfoDataList.add(newData);

                            //write back to packet
                            packet.getPlayerInfoDataLists().write(0, playerInfoDataList);
                            continue;

                        }

                        //apply skin based on viewer
                        WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);
                        Multimap<String, WrappedSignedProperty> propertiesMap = gameProfile.getProperties();

                        String sig = propertiesMap.get("textures").iterator().next().getSignature();
                        String texture = propertiesMap.get("textures").iterator().next().getValue();

                        WrappedGameProfile newGameProfile = oldData.getProfile().withName(npc.getName());
                        Multimap<String, WrappedSignedProperty> newPropertiesMap = newGameProfile.getProperties();
                        newPropertiesMap.removeAll("textures");
                        WrappedSignedProperty textures = new WrappedSignedProperty("textures", texture, sig);
                        newPropertiesMap.put("textures", textures);

                        PlayerInfoData newData = new PlayerInfoData(newGameProfile, oldData.getLatency(), oldData.getGameMode(), oldData.getDisplayName());

                        //clear list and add new profile
                        playerInfoDataList.clear();
                        playerInfoDataList.add(newData);

                        //write back to packet
                        packet.getPlayerInfoDataLists().write(0, playerInfoDataList);

                    }

                }

            }

        });

        protocolManager.addPacketListener(new PacketAdapter(plugin,
                ListenerPriority.NORMAL,
                PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {

                if (event.getPacketType() == PacketType.Play.Client.USE_ENTITY) {

                    //player recieving packet
                    Player player = event.getPlayer();

                    PacketContainer packet = event.getPacket();
                    Integer entityID = packet.getIntegers().read(0);

                    for (NPC npc : npcs) {

                        //is it an npc?
                        if (!(npc.getEntityID() == entityID)) {
                            return;
                        }

                        if (packet.getEntityUseActions().read(0) == EnumWrappers.EntityUseAction.INTERACT_AT) {
                            return;
                        }

                        new BukkitRunnable() {
                            @Override
                            public void run() {

                                NPCInteractEvent npcInteractEvent = new NPCInteractEvent(npc, player);
                                Bukkit.getPluginManager().callEvent(npcInteractEvent);

                            }

                        }.runTask(plugin);

                    }

                }

            }

        });

    }

}
