package com.rex2go.claimme.listener;

import com.rex2go.claimme.util.ClaimUtil;
import com.rex2go.claimme.player.ClaimPlayer;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.util.profile.cache.ProfileCache;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener extends AbstractListener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("claimme.stick")) return;

        ItemStack itemStack = event.getItem();
        if (itemStack == null || itemStack.getType() != Material.STICK) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        ClaimPlayer claimPlayer = plugin.getClaimPlayerManager().get(player);

        var location = block.getLocation();
        var vector = BlockVector3.at(location.getX(), location.getY(), location.getZ());
        var regions = plugin.getRegionManager().getApplicableRegions(vector);

        if (regions.size() == 0) {
            if (claimPlayer.getLastClicked() == null || !claimPlayer.getLastClicked().equals(vector)) {
                player.sendMessage("§7Dieses Gebiet ist nicht vergeben");
                player.sendMessage("§eDrücke erneut um das Gebiet zu kaufen");
                claimPlayer.setLastClicked(vector);

                Chunk chunk = location.getWorld().getChunkAt(
                        new Location(location.getWorld(), location.getX(), location.getY(), location.getZ())
                );

                ClaimUtil.displayPlot(
                        ClaimUtil.getChunkVertices3D(
                                chunk,
                                location.getY()
                        ),
                        player.getPlayer()
                );
            } else if (claimPlayer.getLastClicked().equals(vector)) {
                claimPlayer.setLastClicked(null);

                try {
                    ClaimUtil.claim(player, location);
                } catch (Exception exception) {
                    player.sendMessage("§c" + exception.getMessage());
                    return;
                }
            }

            return;
        }

        for (var region : regions) {
            if (!region.getId().startsWith("claimme_")) continue;
            var id = region.getId().substring(8);

            if(region.getOwners().contains(player.getUniqueId())) {
                Bukkit.dispatchCommand(player, String.format("/plot info %s", id));
                return;
            }

            ProfileCache profileCache = WorldGuard.getInstance().getProfileCache();
            String owners = region.getOwners().toUserFriendlyString(profileCache);

            player.sendMessage(String.format("§eDieses Gebiet gehört %s (%s)", owners, id));

            Chunk chunk = location.getWorld().getChunkAt(
                    new Location(location.getWorld(), location.getX(), location.getY(), location.getZ())
            );

            ClaimUtil.displayPlot(
                    ClaimUtil.getChunkVertices3D(
                            chunk,
                            location.getY()
                    ),
                    player.getPlayer()
            );
        }
    }
}
