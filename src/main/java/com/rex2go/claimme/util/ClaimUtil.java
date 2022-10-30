package com.rex2go.claimme.util;

import com.rex2go.claimme.ClaimMe;
import com.rex2go.claimme.command.exception.CommandErrorException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimUtil {

    private static final ClaimMe plugin = ClaimMe.getInstance();

    public static List<BlockVector3> getChunkVertices3D(Chunk chunk, double y) {
        var p1 = BlockVector3.at(chunk.getX() * 16, y, chunk.getZ() * 16);
        var p2 = BlockVector3.at(chunk.getX() * 16 + 15, y, chunk.getZ() * 16);
        var p3 = BlockVector3.at(chunk.getX() * 16, y, chunk.getZ() * 16 + 15);
        var p4 = BlockVector3.at(chunk.getX() * 16 + 15, y, chunk.getZ() * 16 + 15);

        return List.of(p1, p2, p3, p4);
    }

    public static void displayPlot(List<BlockVector3> blockVectors, Player player) {
        var cp = plugin.getClaimPlayerManager().get(player);
        if(System.currentTimeMillis() - cp.getLastPlotDisplay() < 1000) {
            player.sendMessage("§7Partikel Cooldown (1s)");
            return;
        }

        cp.setLastPlotDisplay(System.currentTimeMillis());

        new BukkitRunnable() {

            private int iterations = 0;

            @Override
            public void run() {
                if (iterations < 20) {
                    iterations++;
                } else {
                    this.cancel();
                    return;
                }

                for (int x = blockVectors.get(0).getX(); x <= blockVectors.get(1).getX(); x++) {
                    for (int z = blockVectors.get(0).getZ(); z <= blockVectors.get(2).getZ(); z++) {
                        if (blockVectors.get(0).getX() == x
                                || blockVectors.get(3).getX() == x
                                || blockVectors.get(0).getZ() == z
                                || blockVectors.get(3).getZ() == z
                        ) {
                            for (int y = player.getLocation().getBlockY(); y <= player.getLocation().getBlockY() + 3; y++) {
                                player.spawnParticle(Particle.FLAME, x + 0.5, y, z + 0.5, 1, 0, 0, 0, 0.001);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 5);
    }

    public static List<ProtectedRegion> getRegions(Player player, boolean playerNeedsToBeOwner, boolean playerNeedsToBeMember) throws CommandErrorException {
        var location = player.getLocation();

        if (plugin.getConfigManager().getWorldNames().stream().noneMatch(n -> n.equalsIgnoreCase(location.getWorld().getName()))) {
            throw new CommandErrorException("Plots sind in dieser Welt nicht verfügbar");
        }

        var chunk = location.getChunk();

        var playerVector = BlockVector3.at(location.getX(), location.getY(), location.getZ());

        var blockVectors = new ArrayList<>(ClaimUtil.getChunkVertices3D(chunk, location.getY()));
        blockVectors.add(playerVector);

        var regionManager = ClaimMe.getInstance().getRegionManager();
        var regions = new ArrayList<ProtectedRegion>();

        for (BlockVector3 vector3 : blockVectors) {
            var protectedRegions = regionManager.getApplicableRegions(vector3);

            for (var protectedRegion : protectedRegions) {
                if (regions.contains(protectedRegion)) continue;
                regions.add(protectedRegion);
            }
        }

        if (playerNeedsToBeMember || playerNeedsToBeOwner) {
            var filtered = new ArrayList<ProtectedRegion>();

            if (playerNeedsToBeOwner)
                filtered.addAll(
                        regions.stream().filter(r -> r.getOwners().contains(player.getUniqueId())).toList()
                );

            if (playerNeedsToBeMember)
                filtered.addAll(
                        regions.stream().filter(r -> r.getMembers().contains(player.getUniqueId())).toList()
                );

            regions = filtered;
        }

        return regions;
    }

    public static void claim(Player player, Location location) throws Exception {
        var currentRegions = ClaimUtil.getRegions(player, false, false);

        if (currentRegions.size() > 0) {
            throw new Exception("Dieses Gebiet ist bereits vergeben");
        }

        double balance = plugin.getEconomy().getBalance(player);
        double price = plugin.getConfigManager().getChunkPrice();

        String ecoName = price != 1 ? plugin.getEconomy().currencyNamePlural() : plugin.getEconomy().currencyNameSingular();
        String priceString = new DecimalFormat("##.##").format(price);

        if (price > balance) {
            throw new Exception("§7Du hast nicht genügend Geld, um dieses Gebiet zu kaufen. §c" + priceString + " " + ecoName + ".");
        }

        var chunk = location.getChunk();
        var blockVectors = ClaimUtil.getChunkVertices3D(chunk, location.getY());

        var regionManager = plugin.getRegionManager();

        String regionId = "claimme_" + UUID.randomUUID().toString().substring(0, 6);

        var p1 = BlockVector3.at(blockVectors.get(0).getX(), -64, blockVectors.get(0).getZ());
        var p2 = BlockVector3.at(blockVectors.get(3).getX(), 320, blockVectors.get(3).getZ());

        ProtectedRegion region = new ProtectedCuboidRegion(regionId, p1, p2);
        region.setPriority(ClaimMe.getInstance().getConfigManager().getRegionPriority());
        region.getOwners().addPlayer(player.getUniqueId());

        if (plugin.getEconomy().withdrawPlayer(player, plugin.getConfigManager().getChunkPrice()).type == EconomyResponse.ResponseType.SUCCESS) {
            regionManager.addRegion(region);
            player.sendMessage("§aGebietskauf erfolgreich. §7Du hast ein Gebiet für §f" + priceString + " " + ecoName + " §7erworben.");
            ClaimUtil.displayPlot(blockVectors, player);
        } else {
            player.sendMessage("§cEs gab einen Fehler");
        }
    }
}
