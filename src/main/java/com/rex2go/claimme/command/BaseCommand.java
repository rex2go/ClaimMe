package com.rex2go.claimme.command;


import com.rex2go.claimme.ClaimMe;
import com.rex2go.claimme.command.exception.CommandErrorException;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public abstract class BaseCommand {

    private final ClaimMe plugin = ClaimMe.getInstance();

    protected abstract void execute(CommandSender sender, String label, String... args) throws Exception;

    public void checkPermission(CommandSender sender, String... permissions) throws CommandErrorException {
        if (Arrays.stream(permissions).noneMatch(sender::hasPermission))
            throw new CommandErrorException("Keine Berechtigung", permissions[0]);
    }

    public void sendMessage(CommandSender sender, String message, Object... objects) {
        sender.sendMessage(String.format(message, objects));
    }
}