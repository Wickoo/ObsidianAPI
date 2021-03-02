package com.github.wickoo.obsidianapi.disguise;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Disguise {

    private Skin actualSkin, disguisedSkin;
    private String actualName, disguisedName;
    private UUID actualUUID, disguisedUUID;

    public Disguise(UUID disguisedUUID, String disguisedName, String actualName, UUID actualUUID ) {

        this.disguisedUUID = disguisedUUID;
        this.disguisedName = disguisedName;
        this.actualName = actualName;
        this.actualUUID = actualUUID;

    }

    /**
     *
     * Must be called async as to not stall server.
     *
     *
     * @param player
     * @param disguiseName
     * @return
     */

    public static Disguise buildDisguise (Player player, String disguiseName) {

        UUID disguisedUUID = Bukkit.getOfflinePlayer(disguiseName).getUniqueId();
        Skin skin = Skin.getSkin(disguiseName);

        if (skin == null) return null;

        Disguise disguise = new Disguise(disguisedUUID, disguiseName, player.getName(), player.getUniqueId());
        disguise.setDisguisedSkin(skin);
        return disguise;

    }

    public Disguise clone() {

        Disguise disguise = new Disguise(disguisedUUID, disguisedName, actualName, actualUUID);
        disguise.setActualSkin(actualSkin.clone());
        disguise.setDisguisedSkin(disguisedSkin.clone());
        return disguise;
    }

    public UUID getDisguisedUUID() {
        return disguisedUUID;
    }

    public String getDisguisedName() {
        return disguisedName;
    }

    public String getActualName() {
        return actualName;
    }

    public UUID getActualUUID() {
        return actualUUID;
    }

    public Skin getActualSkin() {
        return actualSkin;
    }

    public void setActualSkin(Skin actualSkin) {
        this.actualSkin = actualSkin;
    }

    public Skin getDisguisedSkin() {
        return disguisedSkin;
    }

    public void setDisguisedSkin(Skin disguisedSkin) {
        this.disguisedSkin = disguisedSkin;
    }

}
