package com.rex2go.claimme.player;

import com.rex2go.claimme.ClaimMe;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class ClaimPlayerManager {

    @Getter
    private final ArrayList<ClaimPlayer> cachedClaimPlayers = new ArrayList<>();

    public ClaimPlayer get(Player player) {
        for (var cp : cachedClaimPlayers) {
            if (cp.getUniqueId().equals(player.getUniqueId())) return cp;
        }

        try {
            ClaimPlayer cp;
            var connection = ClaimMe.getInstance().getDatabaseManager().getDataSource().getConnection();
            var ps = connection.prepareStatement("SELECT * FROM `claim_player` WHERE `uuid` = ?");
            ps.setString(1, player.getUniqueId().toString());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // exists

                String username = rs.getString("username");
                long lastSeen = rs.getLong("lastSeen");

                // update username
                if (!username.equals(player.getName())) {
                    PreparedStatement ps1 = connection.prepareStatement("UPDATE `claim_player` SET `username` = `?` WHERE `uuid` = ?");
                    ps1.setString(1, player.getName());
                    ps1.setString(2, player.getUniqueId().toString());
                    ps1.execute();
                    ps1.close();
                }

                // load plots
                ArrayList<ProtectedRegion> plots = new ArrayList<>();
                var allRegions = ClaimMe.getInstance().getRegionManager().getRegions();

                for (var region : allRegions.values()) {
                    if (region.getOwners().contains(player.getUniqueId())) {
                        plots.add(region);
                    }
                }

                // TODO: load groups

                cp = new ClaimPlayer(username, player.getUniqueId(), lastSeen, plots, new ArrayList<>(), player);

                cachedClaimPlayers.add(cp);
            } else {
                // new user
                PreparedStatement ps1 = connection.prepareStatement("INSERT INTO `claim_player` (uuid, username, lastSeen) VALUES (?, ?, ?)");
                ps1.setString(1, player.getUniqueId().toString());
                ps1.setString(2, player.getName());
                ps1.setDouble(3, System.currentTimeMillis());
                ps1.execute();
                ps1.close();

                cp = new ClaimPlayer(player.getName(), player.getUniqueId(), System.currentTimeMillis(), new ArrayList<>(), new ArrayList<>(), player);

                cachedClaimPlayers.add(cp);
            }

            rs.close();
            ps.close();

            connection.close();

            return cp;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    public ClaimOfflinePlayer resolve(String name, boolean cacheOnly) {
        for (var cp : cachedClaimPlayers) {
            if (cp.getName().equalsIgnoreCase(name)) return cp;
        }

        if (cacheOnly) return null;

        try {
            ClaimOfflinePlayer cp = null;
            var connection = ClaimMe.getInstance().getDatabaseManager().getDataSource().getConnection();
            var ps = connection.prepareStatement("SELECT * FROM `claim_player` WHERE `name` = ?");
            ps.setString(1, name);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // exists

                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String username = rs.getString("username");
                long lastSeen = rs.getLong("lastSeen");

                cp = new ClaimOfflinePlayer(username, uuid, lastSeen);
            }

            rs.close();
            ps.close();

            connection.close();

            return cp;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    public ClaimOfflinePlayer resolve(String name) {
        return resolve(name, false);
    }

    public void save(ClaimPlayer claimPlayer) {
        try {
            var connection = ClaimMe.getInstance().getDatabaseManager().getDataSource().getConnection();
            var ps = connection.prepareStatement("UPDATE `claim_player` SET lastSeen = ? WHERE `uuid` = ?");
            ps.setLong(1, System.currentTimeMillis());
            ps.setString(2, claimPlayer.getUniqueId().toString());
            ps.execute();

            ps.close();

            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
