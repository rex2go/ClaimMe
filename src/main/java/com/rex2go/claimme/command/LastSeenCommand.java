package com.rex2go.claimme.command;

import com.rex2go.claimme.ClaimMe;
import com.rex2go.claimme.command.exception.CommandErrorException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.UUID;

public class LastSeenCommand extends WrappedCommandExecutor {

    private final ClaimMe plugin = ClaimMe.getInstance();

    public LastSeenCommand() {
        super("lastseen", null);
    }

    @Override
    protected void execute(CommandSender sender, String label, String... args) throws Exception {
        checkPermission(sender, "claimme.lastseen");
    }
}
