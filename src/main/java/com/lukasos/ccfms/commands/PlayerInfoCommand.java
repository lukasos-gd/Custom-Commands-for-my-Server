package com.lukasos.ccfms.commands;

import com.lukasos.ccfms.CcfmsMod;
import com.lukasos.ccfms.data.BanRecord;
import com.lukasos.ccfms.data.HomeLocation;
import com.lukasos.ccfms.data.PlayerRecord;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PlayerInfoCommand {
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
    private static final int MAX_HISTORY_SHOWN = 5;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("player-info")
                .then(argument("username", StringArgumentType.word())
                        .executes(ctx -> info(ctx.getSource(), StringArgumentType.getString(ctx, "username")))));
    }

    private static int info(CommandSourceStack source, String username) {
        ServerPlayer online = null;
        for (ServerPlayer p : source.getServer().getPlayerList().getPlayers()) {
            if (p.getName().getString().equalsIgnoreCase(username)) {
                online = p;
                break;
            }
        }

        UUID uuid;
        String resolvedName;
        if (online != null) {
            uuid = online.getUUID();
            resolvedName = online.getName().getString();
        } else {
            uuid = CcfmsMod.playerRegistry.findUuidByName(username);
            if (uuid == null) {
                source.sendFailure(Component.literal("No record of a player named '" + username + "'."));
                return 0;
            }
            PlayerRecord record = CcfmsMod.playerRegistry.getRecord(uuid);
            resolvedName = record != null ? record.name : username;
        }

        PlayerRecord record = CcfmsMod.playerRegistry.getRecord(uuid);
        Map<String, HomeLocation> homes = CcfmsMod.homeManager.getAllHomes(uuid);
        BanRecord activeBan = CcfmsMod.banManager.getBan(uuid);
        List<BanRecord> history = CcfmsMod.banManager.getHistory(uuid);

        MutableComponent msg = Component.literal("--- Player Info: " + resolvedName + " ---\n").withStyle(ChatFormatting.GOLD);

        msg.append(line("UUID", uuid.toString()));
        msg.append(line("Status", online != null ? "Online" : "Offline"));
        if (record != null) {
            msg.append(line("First seen", DISPLAY_FORMAT.format(Instant.ofEpochMilli(record.firstSeen))));
            msg.append(line("Last seen", online != null ? "Now" : DISPLAY_FORMAT.format(Instant.ofEpochMilli(record.lastSeen))));
        }
        msg.append(line("Homes", String.valueOf(homes.size())));

        if (activeBan != null) {
            msg.append(Component.literal("Current ban: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("ACTIVE\n").withStyle(ChatFormatting.RED));
            msg.append(line("  Reason", activeBan.reason));
            msg.append(line("  Expires", OffendCommand.formatExpiryOrPermanent(activeBan.expiresAt)));
            msg.append(line("  Can appeal", activeBan.canAppeal ? "Yes" : "No"));
            msg.append(line("  Banned by", activeBan.bannedBy));
        } else {
            msg.append(line("Current ban", "None"));
        }

        msg.append(Component.literal("Ban history: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(history.size() + " total\n").withStyle(ChatFormatting.WHITE));

        int shown = 0;
        for (int i = history.size() - 1; i >= 0 && shown < MAX_HISTORY_SHOWN; i--, shown++) {
            BanRecord entry = history.get(i);
            String date = DISPLAY_FORMAT.format(Instant.ofEpochMilli(entry.timestamp));
            msg.append(Component.literal("  - [" + date + "] ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(entry.durationLabel + " ").withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal("by " + entry.bannedBy + ": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(entry.reason + "\n").withStyle(ChatFormatting.WHITE));
        }
        if (history.size() > MAX_HISTORY_SHOWN) {
            msg.append(Component.literal("  ...and " + (history.size() - MAX_HISTORY_SHOWN) + " more").withStyle(ChatFormatting.DARK_GRAY));
        }

        source.sendSuccess(() -> msg, false);
        return 1;
    }

    private static MutableComponent line(String label, String value) {
        return Component.literal(label + ": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(value + "\n").withStyle(ChatFormatting.WHITE));
    }
}
