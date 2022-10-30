package com.rex2go.claimme.task;

import com.rex2go.claimme.ClaimMe;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class InactivityChecker extends BukkitRunnable {

    private final ClaimMe plugin = ClaimMe.getInstance();

    @Override
    public void run() {
        if (plugin.getConfigManager().getRemoveInactivePlotsTime() == -1) return;

        try {
            var connection = plugin.getDatabaseManager().getDataSource().getConnection();
            var offlineTime = System.currentTimeMillis() - (plugin.getConfigManager().getRemoveInactivePlotsTime() * 1000L);
            var ps = connection.prepareStatement("SELECT * FROM `claim_player` WHERE `lastSeen` <= ?");
            ps.setLong(1, offlineTime);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String username = rs.getString("username");

                var allRegions = plugin.getRegionManager().getRegions();

                for (var region : allRegions.values()) {
                    if (region.getOwners().contains(uuid)) {
                        plugin.getRegionManager().removeRegion(region.getId());
                        plugin.getLogger().log(Level.INFO, String.format("Removed region %s belonging to %s", region.getId(), username));
                    }
                }
            }

            rs.close();
            ps.close();

            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
