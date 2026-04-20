package org.encinet.noAnyBoom;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import org.encinet.noAnyBoom.utils.ViolationTracker;

public class NabCommand {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Component PREFIX = MM.deserialize("<dark_gray>[<red>NAB</red>]</dark_gray> ");

    private static Component msg(String mini) {
        return PREFIX.append(MM.deserialize(mini));
    }

    public static void register(LifecycleEventManager<Plugin> manager) {
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(
                    Commands.literal("nab")
                            .requires(src -> src.getSender().hasPermission("noanyboom.admin"))
                            .then(Commands.literal("list").executes(NabCommand::list))
                            .then(Commands.literal("info")
                                    .then(Commands.argument("player", word()).executes(NabCommand::info)))
                            .then(Commands.literal("reset")
                                    .then(Commands.argument("player", word())
                                            .then(Commands.argument("mode", word())
                                                    .executes(NabCommand::reset)))
                                    .executes(ctx -> resetDefault(ctx)))
                            .build(),
                    "NoAnyBoom admin command",
                    List.of()
            );
        });
    }

    private static int resetDefault(CommandContext<CommandSourceStack> ctx) {
        String name = ctx.getArgument("player", String.class);
        Player target = Bukkit.getPlayer(name);
        if (target == null) {
            ctx.getSource().getSender().sendMessage(msg("<red>Player not online"));
            return 0;
        }
        UUID uuid = target.getUniqueId();
        ViolationTracker.resetViolations(uuid);
        ViolationTracker.resetBanCount(uuid);
        ctx.getSource().getSender().sendMessage(msg("<green>Reset <white>all</white> for <white>" + name + "</white>."));
        return Command.SINGLE_SUCCESS;
    }

    private static int list(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        var violations = ViolationTracker.getAllViolations();
        var banCounts = ViolationTracker.getAllBanCounts();

        Set<UUID> all = new HashSet<>(violations.keySet());
        all.addAll(banCounts.keySet());

        if (all.isEmpty()) {
            sender.sendMessage(msg("<gray>No records."));
            return Command.SINGLE_SUCCESS;
        }

        sender.sendMessage(msg("<gray>Records <white>(" + all.size() + ")</white>:"));
        for (UUID uuid : all) {
            int v = violations.getOrDefault(uuid, 0);
            int b = banCounts.getOrDefault(uuid, 0);
            sender.sendMessage(MM.deserialize(
                    "  <white>" + resolveName(uuid) + "</white> <dark_gray>|</dark_gray> " +
                            "<yellow>violations</yellow> <red>" + v + "<dark_gray>/</dark_gray>" + ViolationTracker.THRESHOLD + "</red>  " +
                            "<yellow>bans</yellow> <red>" + b + "</red>"
            ));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int info(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        String name = ctx.getArgument("player", String.class);

        Player target = Bukkit.getPlayer(name);
        if (target == null) {
            sender.sendMessage(msg("<red>Player not online: <white>" + name));
            return 0;
        }

        UUID uuid = target.getUniqueId();
        sender.sendMessage(msg(
                "<white>" + name + "</white> <dark_gray>|</dark_gray> " +
                        "<yellow>violations</yellow> <red>" + ViolationTracker.getViolations(uuid) +
                        "<dark_gray>/</dark_gray>" + ViolationTracker.THRESHOLD + "</red>  " +
                        "<yellow>bans</yellow> <red>" + ViolationTracker.getBanCount(uuid) + "</red>"
        ));
        return Command.SINGLE_SUCCESS;
    }

    private static int reset(CommandContext<CommandSourceStack> ctx) {
        String name = ctx.getArgument("player", String.class);
        String mode = ctx.getArgument("mode", String.class).toLowerCase();
        Player target = Bukkit.getPlayer(name);

        if (target == null) {
            ctx.getSource().getSender().sendMessage(msg("<red>Player not online"));
            return 0;
        }

        UUID uuid = target.getUniqueId();
        switch (mode) {
            case "violations" -> ViolationTracker.resetViolations(uuid);
            case "bans" -> ViolationTracker.resetBanCount(uuid);
            default -> {
                ViolationTracker.resetViolations(uuid);
                ViolationTracker.resetBanCount(uuid);
            }
        }
        ctx.getSource().getSender().sendMessage(msg("<green>Reset <white>" + mode + "</white> for <white>" + name + "</white>."));
        return Command.SINGLE_SUCCESS;
    }

    private static String resolveName(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        return p != null ? p.getName() : uuid.toString().substring(0, 8) + "…";
    }
}