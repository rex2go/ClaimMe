package com.rex2go.claimme.command;

import com.rex2go.claimme.ClaimMe;
import org.bukkit.command.CommandSender;

public class LastSeenCommand extends WrappedCommandExecutor {

    private final ClaimMe plugin = ClaimMe.getInstance();

    public LastSeenCommand() {
        super("lastseen", null);
    }

    @Override
    protected void execute(CommandSender sender, String label, String... args) throws Exception {
        checkPermission(sender, "claimme.lastseen");

        // TODO
    }
}
