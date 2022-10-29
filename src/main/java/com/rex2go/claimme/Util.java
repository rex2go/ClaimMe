package com.rex2go.claimme;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Chunk;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Util {

    public static List<BlockVector3> getChunkVertices3D(Chunk chunk, double y) {
        var p1 = BlockVector3.at(chunk.getX() * 16, y, chunk.getZ() * 16);
        var p2 = BlockVector3.at(chunk.getX() * 16 + 15, y, chunk.getZ() * 16);
        var p3 = BlockVector3.at(chunk.getX() * 16, y, chunk.getZ() * 16 + 15);
        var p4 = BlockVector3.at(chunk.getX() * 16 + 15, y, chunk.getZ() * 16 + 15);

        return List.of(p1, p2, p3, p4);
    }

    public static void displayPlot(List<BlockVector3> blockVectors, Player player) {
        new BukkitRunnable() {

            private int iterations = 0;

            @Override
            public void run() {
                if (iterations < 50) {
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
        }.runTaskTimer(ClaimMe.getInstance(), 0, 5);
    }
}
