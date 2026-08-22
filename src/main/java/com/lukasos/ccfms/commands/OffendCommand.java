package com.lukasos.ccfms.commands;

import com.lukasos.ccfms.CcfmsMod;
import com.lukasos.ccfms.data.BanRecord;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
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
    private static final long INVALID_DURATION = -1L;

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private static final SuggestionProvider<CommandSourceStack> DURATION_SUGGESTIONS = (ctx, builder) -> {
        for (String s : new String[]{"1h", "1d", "7d", "1m", "1y", "5y", "Permanent"}) {
            builder.suggest(s);
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("offend")
                .requires(OffendCommand::isOp)
                .then(argument("player", EntityArgument.player())
                        .then(argument("duration", StringArgumentType.word())
                                .suggests(DURATION_SUGGESTIONS)
                                .then(argument("can-appeal", BoolArgumentType.bool())
                                        .executes(ctx -> offend(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                StringArgumentType.getString(ctx, "duration"),
                                                BoolArgumentType.getBool(ctx, "can-appeal"),
                                                "No reason provided."))
                                        .then(argument("reason", StringArgumentType.greedyString())
                                                .executes(ctx -> offend(ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        StringArgumentType.getString(ctx, "duration"),
                                                        BoolArgumentType.getBool(ctx, "can-appeal"),
                                                        StringArgumentType.getString(ctx, "reason")))))))
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

    private static int offend(CommandSourceStack source, ServerPlayer target, String durationInput, boolean canAppeal, String reason) {
        boolean permanent = durationInput.equalsIgnoreCase("permanent");
        Long expiresAt = null;
        if (!permanent) {
            long durationMs = parseDuration(durationInput);
            if (durationMs == INVALID_DURATION) {
                source.sendFailure(Component.literal("Invalid duration. Use 1h, 1d, 7d, 1m, 1y, 5y, Permanent, or a custom value like 30m/12h/3d/2w."));
                return 0;
            }
            expiresAt = System.currentTimeMillis() + durationMs;
        }

        ServerPlayer executor = source.getPlayer();
        String bannedBy = executor != null ? executor.getName().getString() : "Console";

        BanRecord record = new BanRecord();
        record.targetName = target.getName().getString();
        record.reason = reason;
        record.expiresAt = expiresAt;
        record.durationLabel = permanent ? "Permanent" : durationInput;
        record.canAppeal = canAppeal;
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
        source.sendFailure(Component.literal("No active ban found for '" + name + "'."));
        return 0;
    }

    private static int setAppeal(CommandSourceStack source, String text) {
        CcfmsMod.banManager.setAppealInfo(text);
        source.sendSuccess(() -> Component.literal("Appeal info updated to: " + text), true);
        return 1;
    }

    private static long parseDuration(String input) {
        switch (input.toLowerCase()) {
            case "1h": return 3_600_000L;
            case "1d": return 86_400_000L;
            case "7d": return 7 * 86_400_000L;
            case "1m": return 30 * 86_400_000L;
            case "1y": return 365 * 86_400_000L;
            case "5y": return 5 * 365 * 86_400_000L;
            default: return parseCustomDuration(input);
        }
    }

    private static long parseCustomDuration(String input) {
        if (input.length() < 2) return INVALID_DURATION;
        char unit = Character.toLowerCase(input.charAt(input.length() - 1));
        long multiplier = switch (unit) {
            case 's' -> 1000L;
            case 'm' -> 60_000L;
            case 'h' -> 3_600_000L;
            case 'd' -> 86_400_000L;
            case 'w' -> 7 * 86_400_000L;
            default -> -1L;
        };
        if (multiplier < 0) return INVALID_DURATION;
        try {
            long amount = Long.parseLong(input.substring(0, input.length() - 1));
            if (amount <= 0) return INVALID_DURATION;
            return amount * multiplier;
        } catch (NumberFormatException e) {
            return INVALID_DURATION;
        }
    }

    public static String formatDate(long epochMilli) {
        return DISPLAY_FORMAT.format(Instant.ofEpochMilli(epochMilli));
    }

    public static String formatExpiryOrPermanent(Long expiresAt) {
        return expiresAt == null ? "Permanent" : formatDate(expiresAt);
    }
}
