package com.rex2go.claimme.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.util.UUID;

@Getter
public class ClaimOfflinePlayer {

    private final String name;

    private final UUID uniqueId;

    @Setter
    private long lastSeen;

    public ClaimOfflinePlayer(String name, UUID uniqueId, long lastSeen) {
        this.name = name;
        this.uniqueId = uniqueId;
        this.lastSeen = lastSeen;
    }

    public boolean isOnline() {
        return Bukkit.getOnlinePlayers().stream().anyMatch(p -> p.getUniqueId().equals(uniqueId));
    }
}
