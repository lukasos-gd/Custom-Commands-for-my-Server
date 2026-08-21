package com.lukasos.ccfms.commands;

import com.lukasos.ccfms.CcfmsMod;
import com.lukasos.ccfms.data.BanRecord;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class OffendCommand {
    private static final long INVALID_DURATION = Long.MIN_VALUE;
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("offend")
                .requires(OffendCommand::isOp)
                .then(argument("player", EntityArgument.player())
                        .then(argument("duration", StringArgumentType.word())
                                .executes(ctx -> offend(ctx.getSource(),
                                        EntityArgument.getPlayer(ctx, "player"),
                                        StringArgumentType.getString(ctx, "duration"),
                                        "No reason provided."))
                                .then(argument("reason", StringArgumentType.greedyString())
                                        .executes(ctx -> offend(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                StringArgumentType.getString(ctx, "duration"),
                                                StringArgumentType.getString(ctx, "reason"))))))
                .then(literal("config")
                        .then(literal("appeal")
                                .then(argument("text", StringArgumentType.greedyString())
                                        .executes(ctx -> setAppeal(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "text")))))));

        dispatcher.register(literal("unoffend")
                .requires(OffendCommand::isOp)
                .then(argument("name", StringArgumentType.word())
                        .executes(ctx -> unoffend(ctx.getSource(), StringArgumentType.getString(ctx, "name")))));
    }

    private static boolean isOp(CommandSourceStack src) {
        ServerPlayer player = src.getPlayer();
        return player == null || src.getServer().getPlayerList().isOp(player.nameAndId());
    }

    private static int offend(CommandSourceStack source, ServerPlayer target, String durationInput, String reason) {
        long parsed = parseDuration(durationInput);
        if (parsed == INVALID_DURATION) {
            source.sendFailure(Component.literal("Invalid duration. Use a number plus s/m/h/d/w (e.g. 30m, 7d), or 'perm' for permanent."));
            return 0;
        }
        Long expiresAt = parsed == 0L ? null : parsed;

        ServerPlayer executor = source.getPlayer();
        String bannedBy = executor != null ? executor.getName().getString() : "Console";

        BanRecord record = new BanRecord();
        record.targetName = target.getName().getString();
        record.reason = reason;
        record.expiresAt = expiresAt;
        record.bannedBy = bannedBy;
        record.timestamp = System.currentTimeMillis();

        CcfmsMod.banManager.ban(target.getUUID(), record);

        source.sendSuccess(() -> Component.literal(target.getName().getString() + " has been banned. Reason: " + reason), true);
        target.connection.disconnect(CcfmsMod.buildBanMessage(record, CcfmsMod.banManager.getAppealInfo()));
        return 1;
    }

    private static int unoffend(CommandSourceStack source, String name) {
        boolean removed = CcfmsMod.banManager.unoffendByName(name);
        if (removed) {
            source.sendSuccess(() -> Component.literal(name + " has been unbanned."), true);
            return 1;
        }
        source.sendFailure(Component.literal("No ban found for '" + name + "'."));
        return 0;
    }

    private static int setAppeal(CommandSourceStack source, String text) {
        CcfmsMod.banManager.setAppealInfo(text);
        source.sendSuccess(() -> Component.literal("Appeal info updated to: " + text), true);
        return 1;
    }

    private static long parseDuration(String input) {
        if (input.equalsIgnoreCase("perm") || input.equalsIgnoreCase("permanent")) {
            return 0L;
        }
        if (input.length() < 2) return INVALID_DURATION;
        char unit = Character.toLowerCase(input.charAt(input.length() - 1));
        long multiplier = switch (unit) {
            case 's' -> 1000L;
            case 'm' -> 60_000L;
            case 'h' -> 3_600_000L;
            case 'd' -> 86_400_000L;
            case 'w' -> 604_800_000L;
            default -> -1L;
        };
        if (multiplier < 0) return INVALID_DURATION;
        try {
            long amount = Long.parseLong(input.substring(0, input.length() - 1));
            if (amount <= 0) return INVALID_DURATION;
            return System.currentTimeMillis() + amount * multiplier;
        } catch (NumberFormatException e) {
            return INVALID_DURATION;
        }
    }

    public static String formatExpiry(Long expiresAt) {
        if (expiresAt == null) return "Permanent";
        return DISPLAY_FORMAT.format(Instant.ofEpochMilli(expiresAt));
    }
}
