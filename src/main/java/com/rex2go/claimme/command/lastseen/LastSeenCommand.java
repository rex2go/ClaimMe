package com.rex2go.claimme.command.lastseen;

import com.rex2go.claimme.ClaimMe;
import com.rex2go.claimme.command.WrappedCommandExecutor;
import com.rex2go.claimme.command.exception.CommandErrorException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.concurrent.TimeUnit;

public class LastSeenCommand extends WrappedCommandExecutor {

    private final ClaimMe plugin = ClaimMe.getInstance();

    public LastSeenCommand() {
        super("lastseen", null);
    }

    @Override
    protected void execute(CommandSender sender, String label, String... args) throws Exception {
        checkPermission(sender, "claimme.lastseen");

        if(args.length < 1)
            throw new CommandErrorException("§7/lastseen <Spieler>");

        var targetName = args[0];

        if(Bukkit.getOnlinePlayers().stream().anyMatch(p -> p.getName().equalsIgnoreCase(targetName))) {
            sender.sendMessage(String.format("§e%s ist gerade online", targetName));
            return;
        }

        var target = plugin.getClaimPlayerManager().resolve(targetName);

        if(target == null)
            throw new CommandErrorException(targetName + " war noch nie auf diesem Server");

        var millis = System.currentTimeMillis() - target.getLastSeen();
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        var formatted = String.format(
                "%d Tage, %d Stunden, %d Minuten, %d Sekunden",
                days,
                hours,
                minutes,
                seconds
        );

        sender.sendMessage(String.format("§7%s ist offline seit: §e%s", target.getName(), formatted));
    }
}
