package com.rex2go.claimme;

import com.rex2go.claimme.command.claim.ClaimCommand;
import com.rex2go.claimme.command.lastseen.LastSeenCommand;
import com.rex2go.claimme.command.plot.PlotCommand;
import com.rex2go.claimme.config.ConfigManager;
import com.rex2go.claimme.database.DatabaseManager;
import com.rex2go.claimme.listener.PlayerInteractListener;
import com.rex2go.claimme.listener.PlayerJoinListener;
import com.rex2go.claimme.listener.PlayerQuitListener;
import com.rex2go.claimme.player.ClaimPlayer;
import com.rex2go.claimme.player.ClaimPlayerManager;
import com.rex2go.claimme.task.InactivityChecker;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class ClaimMe extends JavaPlugin {

    @Getter
    private static ClaimMe instance;

    @Getter
    private WorldGuard worldGuard;

    @Getter
    private DatabaseManager databaseManager;

    @Getter
    private RegionManager regionManager;

    @Getter
    private ConfigManager configManager;

    @Getter
    private ClaimPlayerManager claimPlayerManager;

    @Getter
    private Economy economy;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            getLogger().log(Level.SEVERE, "Missing economy plugin!");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        saveResource("config.yml", false);

        worldGuard = WorldGuard.getInstance();

        setupManagers();
        setupDatabase();
        setupTables();
        setupListeners();
        setupCommands();

        setGlobalFlag(
                Flags.BUILD,
                configManager.isDisableGlobalBuild() ? StateFlag.State.DENY : StateFlag.State.ALLOW
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            claimPlayerManager.get(player);
        }

        new InactivityChecker().runTaskTimerAsynchronously(this, 0, configManager.getRemoveInactivePlotsCheckInterval() * 20L);
    }

    @Override
    public void onDisable() {
        for (ClaimPlayer player : claimPlayerManager.getCachedClaimPlayers()) {
            claimPlayerManager.save(player);
        }
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider =
                getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    private void setupDatabase() {
        boolean useMySQL = configManager.isUseMySQL();

        if (useMySQL) {
            String host = configManager.getHost();
            int port = configManager.getPort();
            String database = "claimme";
            String user = configManager.getUser();
            String password = configManager.getPassword();

            databaseManager = new DatabaseManager(host, database, user, password, port);
        } else {
            File databaseFile = new File(getDataFolder(), "claimme.db");

            if (!databaseFile.exists()) {
                try {
                    databaseFile.createNewFile();
                } catch (IOException exception) {
                    getLogger().log(Level.SEVERE, "Failed to created SQLite database. Error: " + exception.getMessage());
                    getPluginLoader().disablePlugin(this);
                    return;
                }
            }

            databaseManager = new DatabaseManager(databaseFile);
        }
    }

    private void setupTables() {
        try {
            Connection connection = databaseManager.getDataSource().getConnection();
            PreparedStatement ps;

            if (databaseManager.isMySQL()) {
                ps = connection.prepareStatement("CREATE DATABASE IF NOT EXISTS ?");
                ps.setString(1, databaseManager.getDatabase());

                ps.execute();
                ps.close();
            }

            ps = connection.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS `claim_player` (
                    	`uuid` VARCHAR(36) NOT NULL,
                    	`username` VARCHAR(16) NOT NULL,
                    	`lastSeen` DOUBLE DEFAULT 0,
                    	PRIMARY KEY (`uuid`)
                    );
                    """);
            ps.execute();
            ps.close();

            // TODO: tables here

            connection.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void setupManagers() {
        configManager = new ConfigManager();
        regionManager = worldGuard.getPlatform().getRegionContainer().get(
                BukkitAdapter.adapt(
                        Bukkit.getWorld(configManager.getWorldNames().get(0)) // TODO: multiple worlds
                )
        );
        claimPlayerManager = new ClaimPlayerManager();
    }

    private void setupListeners() {
        new PlayerJoinListener();
        new PlayerQuitListener();
        new PlayerInteractListener();
    }

    private void setupCommands() {
        new ClaimCommand();
        new PlotCommand();
        new LastSeenCommand();
    }

    private void setGlobalFlag(Flag flag, StateFlag.State state) {
        ProtectedRegion region = regionManager.getRegion("__global__");

        if (region == null) {
            region = new GlobalProtectedRegion("__global__");
            regionManager.addRegion(region);
        }

        if (region.getFlag(flag) == null || region.getFlag(flag) != state) {
            region.setFlag(flag, state);
        }
    }
}
