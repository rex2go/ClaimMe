package com.rex2go.claimme.config;

import com.rex2go.claimme.ClaimMe;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.logging.Level;

@Getter
public class ConfigManager {

    private final ClaimMe plugin = ClaimMe.getInstance();
    private final FileConfiguration config = plugin.getConfig();
    private double chunkPrice = 5000;
    private int regionPriority = 1;
    private boolean disableGlobalBuild = false;
    private List<String> worldNames = List.of("world");
    private int removeInactivePlotsCheckInterval = 30 * 60;
    private int removeInactivePlotsTime = -1;

    // mysql
    private boolean useMySQL = false;
    private String host = "";
    private int port = 3306;
    private String user = "";
    private String password = "";

    public ConfigManager() {
        loadConfig();
    }

    public void loadConfig() {
        try {
            chunkPrice = config.getDouble("chunkPrice");
            regionPriority = config.getInt("regionPriority");
            disableGlobalBuild = config.getBoolean("disableGlobalBuild");
            worldNames = (List<String>) plugin.getConfig().getList("worlds");
            removeInactivePlotsCheckInterval = config.getInt("removeInactivePlotsCheckInterval");
            removeInactivePlotsTime = config.getInt("removeInactivePlotsTime");
            useMySQL = config.getBoolean("useMySQL");
            host = config.getString("mySQL.host");
            port = config.getInt("mySQL.port");
            user = config.getString("mySQL.user");
            password = config.getString("mySQL.password");
        } catch (Exception exception) {
            plugin.getLogger().log(Level.WARNING, "Error loading config.yml");
        }
    }
}