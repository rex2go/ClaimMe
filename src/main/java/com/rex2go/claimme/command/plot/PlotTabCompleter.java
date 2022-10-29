package com.rex2go.claimme.command.plot;

import com.rex2go.claimme.ClaimMe;
import com.rex2go.claimme.player.ClaimPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlotTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("plot"))
            return new ArrayList<>();

        if (!(sender instanceof Player player))
            return new ArrayList<>();

        String lastArg = args[args.length - 1];
        boolean isAdmin = sender.hasPermission("claimme.plot.admin");

        if (args.length == 1) {
            ArrayList<String> list = new ArrayList<>();

            if (sender.hasPermission("claimme.plot.list") || isAdmin)
                list.add("list");
            if (sender.hasPermission("claimme.plot.info") || isAdmin)
                list.add("info");
            if (sender.hasPermission("claimme.plot.addmember") || isAdmin)
                list.add("addmember");
            if (sender.hasPermission("claimme.plot.removemember") || isAdmin)
                list.add("removemember");
            if (sender.hasPermission("claimme.plot.transfer") || isAdmin)
                list.add("transfer");
            if (sender.hasPermission("claimme.plot.show") || isAdmin)
                list.add("show");

            if (lastArg.isEmpty()) return list;

            return list
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        String subCommand = args[0];

        if (subCommand.equalsIgnoreCase("info")
                || subCommand.equalsIgnoreCase("addmember")
                || subCommand.equalsIgnoreCase("removemember")
                || subCommand.equalsIgnoreCase("transfer")
                || subCommand.equalsIgnoreCase("show")) {
            if (args.length == 2) {
                ClaimPlayer claimPlayer = ClaimMe.getInstance().getClaimPlayerManager().get(player);
                ArrayList<String> list = new ArrayList<>();

                for (var region : claimPlayer.getCachedRegions()) {
                    list.add(region.getId().substring(8));
                }

                if (lastArg.isEmpty()) return list;

                return list
                        .stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (args.length == 3) {
                ArrayList<String> list = new ArrayList<>();

                for (var all : Bukkit.getOnlinePlayers()) {
                    list.add(all.getName());
                }

                if (lastArg.isEmpty()) return list;

                return list
                        .stream()
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}
