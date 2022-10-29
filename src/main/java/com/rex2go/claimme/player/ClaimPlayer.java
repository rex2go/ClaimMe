package com.rex2go.claimme.player;

import com.rex2go.claimme.plot.ClaimGroup;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

@Getter
public class ClaimPlayer extends ClaimOfflinePlayer {

    private final ArrayList<ProtectedRegion> cachedRegions;

    private final ArrayList<ClaimGroup> cachedGroups;

    private final Player player;

    public ClaimPlayer(String name, UUID uuid, long lastSeen, ArrayList<ProtectedRegion> cachedRegions, ArrayList<ClaimGroup> cachedGroups, Player player) {
        super(name, uuid, lastSeen);
        this.cachedRegions = cachedRegions;
        this.cachedGroups = cachedGroups;
        this.player = player;
    }

    public void sendMessage(String message) {
        player.sendMessage(message);
    }

    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }
}
