package com.rex2go.claimme.config;

import com.rex2go.claimme.ClaimMe;
import lombok.Getter;

import java.util.List;

@Getter
public class ConfigManager {

    private final ClaimMe plugin = ClaimMe.getInstance();

    private final double chunkPrice;
    private final int regionPriority;
    private final boolean disableGlobalBuild;
    private final List<String> worldNames;

    // TODO: add mysql

    public ConfigManager() {
        chunkPrice = plugin.getConfig().getDouble("chunkPrice");
        regionPriority = plugin.getConfig().getInt("regionPriority");
        disableGlobalBuild = plugin.getConfig().getBoolean("disableGlobalBuild");
        worldNames = (List<String>) plugin.getConfig().getList("worlds");
    }
}