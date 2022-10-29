package com.rex2go.claimme.command.exception;

import org.bukkit.ChatColor;

public class CommandErrorException extends Exception {

    public CommandErrorException(String message, String ... args) {
        super(ChatColor.RED + message); // TODO: args?
    }
}
