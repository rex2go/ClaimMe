package com.rex2go.claimme.command.claim;

import com.rex2go.claimme.ClaimMe;
import com.rex2go.claimme.Util;
import com.rex2go.claimme.command.WrappedCommandExecutor;
import com.rex2go.claimme.command.exception.CommandErrorException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.UUID;

public class ClaimCommand extends WrappedCommandExecutor {

    private final ClaimMe plugin = ClaimMe.getInstance();

    public ClaimCommand() {
        super("claim", null);
    }

    @Override
    protected void execute(CommandSender sender, String label, String... args) throws Exception {
        checkPermission(sender, "claimme.claim");

        if (!(sender instanceof Player player))
            throw new CommandErrorException("Dieser Befehl kann nur als Spieler ausgeführt werden");

        var currentRegions = getRegions(player, false, false);

        if (currentRegions.size() > 0) {
            throw new CommandErrorException("Dieses Gebiet ist bereits vergeben");
        }

        double balance = plugin.getEconomy().getBalance(player);
        double price = plugin.getConfigManager().getChunkPrice();

        String ecoName = price != 1 ? plugin.getEconomy().currencyNamePlural() : plugin.getEconomy().currencyNameSingular();
        String priceString = new DecimalFormat("##.##").format(price);

        if (price > balance) {
            throw new CommandErrorException("§7Du hast nicht genügend Geld, um dieses Gebiet zu kaufen. §c" + priceString + " " + ecoName + ".");
        }

        var location = player.getLocation();
        var chunk = location.getChunk();
        var blockVectors = Util.getChunkVertices3D(chunk, location.getY());

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
            Util.displayPlot(blockVectors, player);
        } else {
            player.sendMessage("§cEs gab einen Fehler");
        }
    }
}
