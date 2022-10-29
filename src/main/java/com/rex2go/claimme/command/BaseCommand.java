package com.rex2go.claimme.command;


import com.rex2go.claimme.ClaimMe;
import com.rex2go.claimme.command.exception.CommandErrorException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseCommand {

    private final ClaimMe plugin = ClaimMe.getInstance();

    protected abstract void execute(CommandSender sender, String label, String... args) throws Exception;

    public void checkPermission(CommandSender sender, String... permissions) throws CommandErrorException {
        if (Arrays.stream(permissions).noneMatch(sender::hasPermission))
            throw new CommandErrorException("Keine Berechtigung", permissions[0]);
    }

    public void sendMessage(CommandSender sender, String message, Object... objects) {
        sender.sendMessage(String.format(message, objects));
    }

    public List<BlockVector3> getChunkVertices3D(Chunk chunk, double y) {
        var p1 = BlockVector3.at(chunk.getX() * 16, y, chunk.getZ() * 16);
        var p2 = BlockVector3.at(chunk.getX() * 16 + 15, y, chunk.getZ() * 16);
        var p3 = BlockVector3.at(chunk.getX() * 16, y, chunk.getZ() * 16 + 15);
        var p4 = BlockVector3.at(chunk.getX() * 16 + 15, y, chunk.getZ() * 16 + 15);

        return List.of(p1, p2, p3, p4);
    }

    public List<ProtectedRegion> getRegions(Player player, boolean playerNeedsToBeOwner, boolean playerNeedsToBeMember) throws CommandErrorException {
        var location = player.getLocation();

        if (plugin.getConfigManager().getWorldNames().stream().noneMatch(n -> n.equalsIgnoreCase(location.getWorld().getName()))) {
            throw new CommandErrorException("Plots sind in dieser Welt nicht verfügbar");
        }

        var chunk = location.getChunk();

        var playerVector = BlockVector3.at(location.getX(), location.getY(), location.getZ());

        var blockVectors = new ArrayList<>(getChunkVertices3D(chunk, location.getY()));
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
}