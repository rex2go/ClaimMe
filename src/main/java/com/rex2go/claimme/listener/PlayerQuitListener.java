package com.rex2go.claimme.listener;

import com.rex2go.claimme.player.ClaimPlayer;
import com.rex2go.claimme.player.ClaimPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener extends AbstractListener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ClaimPlayerManager claimPlayerManager = plugin.getClaimPlayerManager();
        ClaimPlayer claimPlayer = claimPlayerManager.get(player);
        claimPlayerManager.save(claimPlayer);
        claimPlayerManager.getCachedClaimPlayers().remove(claimPlayer);
    }
}
