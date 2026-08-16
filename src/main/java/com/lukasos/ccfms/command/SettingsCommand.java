package com.lukasos.ccfms.commands;

import com.lukasos.ccfms.CcfmsMod;
import com.lukasos.ccfms.data.PlayerSettings;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SettingsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("settings")
                .executes(ctx -> showMenu(ctx.getSource()))
                .then(literal("toggle")
                        .then(argument("key", StringArgumentType.word())
                                .executes(ctx -> toggle(ctx.getSource(), StringArgumentType.getString(ctx, "key"))))));
    }

    private static int showMenu(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        PlayerSettings s = CcfmsMod.settingsManager.get(player.getUuid());

        player.sendMessage(Text.literal("--- Your Settings ---").formatted(Formatting.GOLD));
        player.sendMessage(toggleLine("Accept teleport requests", "acceptTpaRequests", s.acceptTpaRequests));
        player.sendMessage(toggleLine("Teleport sound effect", "teleportSound", s.teleportSound));
        player.sendMessage(toggleLine("Auto-set /back on death", "backOnDeath", s.backOnDeath));
        player.sendMessage(Text.literal("Click a setting to toggle it.").formatted(Formatting.GRAY, Formatting.ITALIC));
        return 1;
    }

    private static MutableText toggleLine(String label, String key, boolean value) {
        Text state = value
                ? Text.literal("ON").formatted(Formatting.GREEN)
                : Text.literal("OFF").formatted(Formatting.RED);
        return Text.literal(" - " + label + ": ")
                .append(state)
                .styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/settings toggle " + key)));
    }

    private static int toggle(ServerCommandSource source, String key) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        PlayerSettings s = CcfmsMod.settingsManager.get(player.getUuid());

        switch (key) {
            case "acceptTpaRequests" -> s.acceptTpaRequests = !s.acceptTpaRequests;
            case "teleportSound" -> s.teleportSound = !s.teleportSound;
            case "backOnDeath" -> s.backOnDeath = !s.backOnDeath;
            default -> {
                source.sendError(Text.literal("Unknown setting."));
                return 0;
            }
        }
        CcfmsMod.settingsManager.save();
        return showMenu(source);
    }
}
