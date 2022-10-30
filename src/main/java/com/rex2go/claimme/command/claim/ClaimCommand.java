package com.rex2go.claimme.command.claim;

import com.rex2go.claimme.ClaimMe;
import com.rex2go.claimme.util.ClaimUtil;
import com.rex2go.claimme.command.WrappedCommandExecutor;
import com.rex2go.claimme.command.exception.CommandErrorException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        try {
            ClaimUtil.claim(player, player.getLocation());
        } catch (Exception exception) {
            throw new CommandErrorException(exception.getMessage());
        }
    }
}
