package com.rex2go.claimme.command;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.*;

public abstract class WrappedCommandExecutor extends BaseCommand implements CommandExecutor {

    private final String command;
    @Getter
    protected PluginCommand pluginCommand;

    public WrappedCommandExecutor(String command, TabCompleter tabCompleter) {
        this.command = command;
        this.pluginCommand = Bukkit.getPluginCommand(command);

        assert pluginCommand != null;
        pluginCommand.setExecutor(this);

        if (tabCompleter != null) pluginCommand.setTabCompleter(tabCompleter);

        if (pluginCommand.getPermissionMessage() == null) {
            pluginCommand.setPermissionMessage("§cKeine Rechte");
        }
    }

    public WrappedCommandExecutor(String command) {
        this(command, null);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase(this.command)) return false;

        try {
            execute(sender, label, args);
            return true;
        } catch (Exception exception) {
            sender.sendMessage("§c" + exception.getLocalizedMessage());
        }

        return true;
    }
}