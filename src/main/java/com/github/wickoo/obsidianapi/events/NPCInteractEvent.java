package com.github.wickoo.obsidianapi.events;

import com.github.wickoo.obsidianapi.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NPCInteractEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private NPC npc;
    private Player player;

    public NPCInteractEvent(NPC npc, Player player) {
        this.npc = npc;
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    public NPC getNPC() {
        return npc;
    }

    public Player getPlayer() {
        return player;
    }
}
