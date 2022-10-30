package com.rex2go.claimme.command.plot;

import com.rex2go.claimme.ClaimMe;
import com.rex2go.claimme.util.ClaimUtil;
import com.rex2go.claimme.command.WrappedCommandExecutor;
import com.rex2go.claimme.command.exception.CommandErrorException;
import com.rex2go.claimme.player.ClaimOfflinePlayer;
import com.rex2go.claimme.player.ClaimPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.profile.cache.ProfileCache;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class PlotCommand extends WrappedCommandExecutor {

    private final ClaimMe plugin = ClaimMe.getInstance();

    public PlotCommand() {
        super("plot", new PlotTabCompleter());
    }

    @Override
    protected void execute(CommandSender sender, String label, String... args) throws Exception {
        checkPermission(sender, "claimme.plot");

        if (!(sender instanceof Player player))
            throw new CommandErrorException("Dieser Befehl kann nur als Spieler ausgeführt werden");

        ClaimPlayer claimPlayer = plugin.getClaimPlayerManager().get(player);

        if (args.length > 0) {
            var arg0 = args[0];

            if (arg0.equalsIgnoreCase("list")) {
                list(claimPlayer, args);
            } else if (arg0.equalsIgnoreCase("info")) {
                info(claimPlayer, args);
            } else if (arg0.equalsIgnoreCase("addmember")) {
                addMember(claimPlayer, args);
            } else if (arg0.equalsIgnoreCase("removemember")) {
                removeMember(claimPlayer, args);
            } else if (arg0.equalsIgnoreCase("transfer")) {
                transfer(claimPlayer, args);
            } else if (arg0.equalsIgnoreCase("show")) {
                show(claimPlayer, args);
            } else {
                player.sendMessage("§7/plot <list|info|addmember|removemember|transfer|show>");
            }
        } else {
            player.sendMessage("§7-§e-§7- §fPlot Help §7-§e-§7-");
            player.sendMessage("§fClaimMe created by rex2go");
            if (player.hasPermission("claimme.plot.list"))
                player.sendMessage("§7/plot list [<Spieler>]");
            if (player.hasPermission("claimme.plot.info"))
                player.sendMessage("§7/plot info [<ID>]");
            if (player.hasPermission("claimme.plot.addmember"))
                player.sendMessage("§7/plot addmember <ID> <Spieler>");
            if (player.hasPermission("claimme.plot.removemember"))
                player.sendMessage("§7/plot removemember <ID> <Spieler>");
            if (player.hasPermission("claimme.plot.transfer"))
                player.sendMessage("§7/plot transfer <ID> <Spieler>");
            if (player.hasPermission("claimme.plot.show"))
                player.sendMessage("§7/plot show [<ID>]");
        }
    }

    private void show(ClaimPlayer player, String[] args) throws CommandErrorException {
        checkPermission(player.getPlayer(), "claimme.plot.show");

        ProtectedRegion region;
        String id;

        if (args.length >= 2) {
            id = args[1];
            region = plugin.getRegionManager().getRegion("claimme_" + id);
        } else {
            var regions = ClaimUtil.getRegions(player.getPlayer(), true, true);

            if (regions.size() == 0)
                throw new CommandErrorException("§eNutze /plot show <ID>, um die Grenzen eines Plots anzuzeigen");

            region = regions.get(0);
            id = region.getId();
        }

        if (region == null)
            throw new CommandErrorException("§cDas Gebiet mit der ID \"" + id + "\" konnte nicht gefunden werden");

        if (!region.getOwners().contains(player.getUniqueId()))
            checkPermission(player.getPlayer(), "claimme.plot.admin");

        Location location = player.getPlayer().getLocation();
        Chunk chunk = location.getWorld().getChunkAt(
                new Location(location.getWorld(), region.getMinimumPoint().getBlockX(), location.getY(), region.getMinimumPoint().getBlockZ())
        );
        ClaimUtil.displayPlot(
                ClaimUtil.getChunkVertices3D(
                        chunk,
                        location.getY()
                ),
                player.getPlayer()
        );
    }

    private void addMember(ClaimPlayer player, String[] args) throws CommandErrorException {
        checkPermission(player.getPlayer(), "claimme.plot.addmember");

        if (args.length < 3)
            throw new CommandErrorException("§7/plot addmember <ID> <Spieler>");

        var id = args[1];
        var playerName = args[2];

        ProtectedRegion region = plugin.getRegionManager().getRegion("claimme_" + id);
        if (region == null)
            throw new CommandErrorException("Die Gebiet \"" + id + "\" konnte nicht gefunden");

        if (!region.getOwners().contains(player.getUniqueId()) && !player.hasPermission("claimme.plot.admin"))
            throw new CommandErrorException("Dir gehört dieses Gebiet nicht");

        if (playerName.equalsIgnoreCase(player.getName()))
            throw new CommandErrorException("Du kannst dich nicht selbst als Mitgelid hinzufügen");

        ClaimOfflinePlayer targetOffline = plugin.getClaimPlayerManager().resolve(playerName);

        if (targetOffline == null)
            throw new CommandErrorException(playerName + " war noch nie auf diesem Server");

        if (region.getMembers().contains(targetOffline.getUniqueId()))
            throw new CommandErrorException(targetOffline.getName() + " ist bereits Mitglied");

        region.getMembers().addPlayer(targetOffline.getUniqueId());

        player.sendMessage("§e" + targetOffline.getName() + " als Mitglied hinzugefügt");

        if (targetOffline.isOnline())
            ((ClaimPlayer) targetOffline).sendMessage("§eDu bist nun Mitglied der Region \"" + id + "\"");
    }

    private void removeMember(ClaimPlayer player, String[] args) throws CommandErrorException {
        checkPermission(player.getPlayer(), "claimme.plot.removemember");

        if (args.length < 3)
            throw new CommandErrorException("§7/plot removemember <ID> <Spieler>");

        var id = args[1];
        var playerName = args[2];

        ProtectedRegion region = plugin.getRegionManager().getRegion("claimme_" + id);
        if (region == null)
            throw new CommandErrorException("Die Gebiet \"" + id + "\" konnte nicht gefunden");

        if (!region.getOwners().contains(player.getUniqueId()) && !player.hasPermission("claimme.plot.admin"))
            throw new CommandErrorException("Dir gehört dieses Gebiet nicht");

        if (playerName.equalsIgnoreCase(player.getName()))
            throw new CommandErrorException("Du kannst dich nicht selbst als Mitgelid entfernen");

        ClaimOfflinePlayer targetOffline = plugin.getClaimPlayerManager().resolve(playerName);

        if (targetOffline == null)
            throw new CommandErrorException(playerName + " war noch nie auf diesem Server");

        if (!region.getMembers().contains(targetOffline.getUniqueId()))
            throw new CommandErrorException(targetOffline.getName() + " ist kein Mitglied");

        region.getMembers().removePlayer(targetOffline.getUniqueId());

        player.sendMessage("§e" + targetOffline.getName() + " als Mitglied entfernt");

        if (targetOffline.isOnline())
            ((ClaimPlayer) targetOffline).sendMessage("§eDu bist nun kein Mitglied der Region \"" + id + "\" mehr");
    }

    private void info(ClaimPlayer player, String[] args) throws CommandErrorException {
        checkPermission(player.getPlayer(), "claimme.plot.info");

        ProtectedRegion region;
        String id;

        if (args.length >= 2) {
            id = args[1];
            region = plugin.getRegionManager().getRegion("claimme_" + id);
        } else {
            var regions = ClaimUtil.getRegions(player.getPlayer(), true, true);

            if (regions.size() == 0)
                throw new CommandErrorException("§eNutze /plot info <ID>, um die Informationen eines Plots anzuzeigen");

            region = regions.get(0);
            id = region.getId();
        }

        if (region == null)
            throw new CommandErrorException("§cDas Gebiet mit der ID \"" + id + "\" konnte nicht gefunden werden");

        if (!region.getOwners().contains(player.getUniqueId()))
            checkPermission(player.getPlayer(), "claimme.plot.admin");

        ProfileCache profileCache = WorldGuard.getInstance().getProfileCache();

        player.sendMessage("§7-§e-§7- §fPlot Info §7-§e-§7-");
        player.sendMessage("§7ID: §e" + region.getId().substring(8));

        String owners = region.getOwners().toUserFriendlyString(profileCache);
        if (owners.length() > 0) {
            player.sendMessage("§7Besitzer: §e" + owners);
        }

        String members = region.getMembers().toUserFriendlyString(profileCache);
        if (members.length() > 0) {
            player.sendMessage("§7Mitglieder: §e" + members);
        }

        player.sendMessage("§7Priorität: §e" + region.getPriority());

        player.sendMessage("§7Koordinaten: §eX: " + region.getMinimumPoint().getX() + " Z: " + region.getMinimumPoint().getZ());

        Location location = player.getPlayer().getLocation();
        Chunk chunk = location.getWorld().getChunkAt(
                new Location(location.getWorld(), region.getMinimumPoint().getBlockX(), location.getY(), region.getMinimumPoint().getBlockZ())
        );
        ClaimUtil.displayPlot(
                ClaimUtil.getChunkVertices3D(
                        chunk,
                        location.getY()
                ),
                player.getPlayer()
        );
    }

    private void list(ClaimPlayer player, String[] args) throws CommandErrorException {
        checkPermission(player.getPlayer(), "claimme.plot.list");

        if (args.length > 1) {
            checkPermission(player.getPlayer(), "claimme.plot.admin");

            var targetName = args[1];
            var target = plugin.getClaimPlayerManager().resolve(targetName);
            ArrayList<ProtectedRegion> list;

            if (target == null)
                throw new CommandErrorException(String.format("%s war noch nie auf diesem Server", targetName));

            if (target.isOnline()) {
                list = ((ClaimPlayer) target).getCachedRegions();
            } else {
                list = new ArrayList<>();

                var allRegions = ClaimMe.getInstance().getRegionManager().getRegions();

                for (var region : allRegions.values()) {
                    if (region.getOwners().contains(target.getUniqueId())) {
                        list.add(region);
                    }
                }
            }

            player.sendMessage("§7" + targetName + " besitzt folgende Gebiete:");

            if (list.isEmpty()) {
                player.sendMessage("§7Keine");
                return;
            }

            for (var region : list) {
                var baseComponents = TextComponent.fromLegacyText(
                        String.format(
                                "§7- §e%s [X:%d Z:%d]",
                                region.getId().substring(8),
                                region.getMinimumPoint().getX(),
                                region.getMinimumPoint().getZ()
                        )
                );

                for (BaseComponent baseComponent : baseComponents) {
                    baseComponent.setClickEvent(new ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            "/plot info " + region.getId().substring(8))
                    );

                    baseComponent.setHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            TextComponent.fromLegacyText("Mehr Informationen anzeigen")
                    ));
                }

                player.getPlayer().spigot().sendMessage(baseComponents);
            }

            return;
        }

        player.sendMessage("§7Du besitzt folgende Gebiete:");

        if (player.getCachedRegions().isEmpty()) {
            player.sendMessage("§7Keine");
            return;
        }

        for (var region : player.getCachedRegions()) {
            var baseComponents = TextComponent.fromLegacyText(
                    String.format(
                            "§7- §e%s [X:%d Z:%d]",
                            region.getId().substring(8),
                            region.getMinimumPoint().getX(),
                            region.getMinimumPoint().getZ()
                    )
            );

            for (BaseComponent baseComponent : baseComponents) {
                baseComponent.setClickEvent(new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/plot info " + region.getId().substring(8))
                );

                baseComponent.setHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        TextComponent.fromLegacyText("Mehr Informationen anzeigen")
                ));
            }

            player.getPlayer().spigot().sendMessage(baseComponents);
        }
    }

    public void transfer(ClaimPlayer player, String[] args) throws CommandErrorException {
        checkPermission(player.getPlayer(), "claimme.plot.transfer");

        if (args.length < 3)
            throw new CommandErrorException("§7/plot transfer <ID> <Spieler>");

        String id = args[1];
        String playerName = args[2];

        if (playerName.equalsIgnoreCase(player.getName()) && !player.hasPermission("claimme.plot.admin"))
            throw new CommandErrorException("Du bist bereits Eigentümer des Gebiets");

        ProtectedRegion region = plugin.getRegionManager().getRegion("claimme_" + id);
        if (region == null)
            throw new CommandErrorException("Das Gebiet \"" + id + "\" existiert nicht");

        if (!region.getOwners().contains(player.getUniqueId()) && !player.hasPermission("claimme.plot.admin"))
            throw new CommandErrorException("Dir gehört das Gebiet nicht");

        ClaimOfflinePlayer claimOfflinePlayer = plugin.getClaimPlayerManager().resolve(playerName);
        if (claimOfflinePlayer == null)
            throw new CommandErrorException(playerName + " war noch nie auf dem Server");

        if (args.length < 4 || !args[3].equalsIgnoreCase("confirm")) {
            player.sendMessage(
                    String.format(
                            "§7Bist du sicher, dass du das Gebiet auf %s übertragen möchtest? §cDieser Schritt kann nicht rückgänig gemacht werden.",
                            claimOfflinePlayer.getName()
                    )
            );
            player.sendMessage(
                    String.format(
                            "§7Bestätige mit §e/plot transfer %s %s confirm",
                            id,
                            claimOfflinePlayer.getName()
                    )
            );
            return;
        }

        region.getOwners().clear();
        region.getMembers().clear();
        region.getOwners().addPlayer(claimOfflinePlayer.getUniqueId());

        player.getCachedRegions().remove(region);

        player.sendMessage(String.format("§eDu hast das Gebiet %s auf %s übertragen", id, claimOfflinePlayer.getName()));

        if (claimOfflinePlayer.isOnline()) {
            ClaimPlayer claimPlayer = (ClaimPlayer) claimOfflinePlayer;
            claimPlayer.getCachedRegions().add(region);
            claimPlayer.sendMessage(
                    String.format(
                            "§e%s hat dir das Gebiet %s übertragen",
                            player.getName(),
                            id
                    )
            );
        }
    }
}
