package com.github.wickoo.obsidianapi.disguise;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class Skin {

    private String texture, signature;

    public Skin(String texture, String signature) {

        this.texture = texture;
        this.signature = signature;

    }

    public Skin() {

    }

    public static Skin getSkin(UUID targetUUID) {

        String uuidFixed = targetUUID.toString().replace("-", "");

        try {

            URL url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuidFixed + "?unsigned=false");
            InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());
            JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            String texture = textureProperty.get("value").getAsString();
            String signature = textureProperty.get("signature").getAsString();

            return new Skin(texture, signature);

        } catch (IOException e) {
            System.err.println("Could not get skin data from session servers!");
            return null;
        } catch (IllegalStateException e) {
            System.err.println("Player does not exist!");
            return null;
        }

    }

    public static Skin getSkin(String playerName) {

        UUID targetUUID = Bukkit.getOfflinePlayer(playerName).getUniqueId();
        return getSkin(targetUUID);

    }

    public String getTexture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Skin clone() {
        return new Skin(texture, signature);
    }

}
