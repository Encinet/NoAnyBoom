package org.encinet.noAnyBoom;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.encinet.noAnyBoom.utils.ViolationTracker;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class Command implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /nab <list|info <player>|reset <player> [violations|bans|all]>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> handleList(sender);
            case "info" -> {
                if (args.length < 2) { sender.sendMessage("Usage: /nab info <player>"); return true; }
                handleInfo(sender, args[1]);
            }
            case "reset" -> {
                if (args.length < 2) { sender.sendMessage("Usage: /nab reset <player> [violations|bans|all]"); return true; }
                handleReset(sender, args[1], args.length >= 3 ? args[2] : "all");
            }
            default -> sender.sendMessage("Unknown subcommand. Use: list | info | reset");
        }
        return true;
    }

    private void handleList(CommandSender sender) {
        Map<UUID, Integer> violations = ViolationTracker.getAllViolations();
        Map<UUID, Integer> banCounts = ViolationTracker.getAllBanCounts();

        // Merge all known UUIDs
        java.util.Set<UUID> all = new java.util.HashSet<>(violations.keySet());
        all.addAll(banCounts.keySet());

        if (all.isEmpty()) {
            sender.sendMessage("[NoAnyBoom] No records.");
            return;
        }

        sender.sendMessage("[NoAnyBoom] Records (" + all.size() + "):");
        for (UUID uuid : all) {
            String name = resolvePlayerName(uuid);
            int v = violations.getOrDefault(uuid, 0);
            int b = banCounts.getOrDefault(uuid, 0);
            sender.sendMessage("  " + name + " - violations: " + v + ", bans: " + b);
        }
    }

    private void handleInfo(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        UUID uuid = target != null ? target.getUniqueId() : null;
        if (uuid == null) {
            sender.sendMessage("Player not found or offline: " + playerName);
            return;
        }
        int v = ViolationTracker.getViolations(uuid);
        int b = ViolationTracker.getBanCount(uuid);
        sender.sendMessage("[NoAnyBoom] " + playerName + " - violations: " + v + "/" + ViolationTracker.THRESHOLD + ", bans: " + b);
    }

    private void handleReset(CommandSender sender, String playerName, String mode) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("Player not found or offline: " + playerName);
            return;
        }
        UUID uuid = target.getUniqueId();
        switch (mode.toLowerCase()) {
            case "violations" -> { ViolationTracker.resetViolations(uuid); sender.sendMessage("Reset violations for " + playerName); }
            case "bans"       -> { ViolationTracker.resetBanCount(uuid);   sender.sendMessage("Reset ban count for " + playerName); }
            default           -> {
                ViolationTracker.resetViolations(uuid);
                ViolationTracker.resetBanCount(uuid);
                sender.sendMessage("Reset all counts for " + playerName);
            }
        }
    }

    private String resolvePlayerName(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) return p.getName();
        return uuid.toString().substring(0, 8) + "...";
    }
}