package com.lukasos.ccfms.commands;

import com.lukasos.ccfms.CcfmsMod;
import com.lukasos.ccfms.data.PlayerSettings;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class SettingsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("settings")
                .executes(ctx -> showMenu(ctx.getSource()))
                .then(literal("toggle")
                        .then(argument("key", StringArgumentType.word())
                                .executes(ctx -> toggle(ctx.getSource(), StringArgumentType.getString(ctx, "key"))))));
    }

    private static int showMenu(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        PlayerSettings s = CcfmsMod.settingsManager.get(player.getUUID());

        player.sendSystemMessage(Component.literal("--- Your Settings ---").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(toggleLine("Accept teleport requests", "acceptTpaRequests", s.acceptTpaRequests));
        player.sendSystemMessage(toggleLine("Teleport sound effect", "teleportSound", s.teleportSound));
        player.sendSystemMessage(toggleLine("Auto-set /back on death", "backOnDeath", s.backOnDeath));
        player.sendSystemMessage(Component.literal("Click a setting to toggle it.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        return 1;
    }

    private static MutableComponent toggleLine(String label, String key, boolean value) {
        Component state = value
                ? Component.literal("ON").withStyle(ChatFormatting.GREEN)
                : Component.literal("OFF").withStyle(ChatFormatting.RED);
        return Component.literal(" - " + label + ": ")
                .append(state)
                .withStyle(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/settings toggle " + key)));
    }

    private static int toggle(CommandSourceStack source, String key) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        PlayerSettings s = CcfmsMod.settingsManager.get(player.getUUID());

        switch (key) {
            case "acceptTpaRequests" -> s.acceptTpaRequests = !s.acceptTpaRequests;
            case "teleportSound" -> s.teleportSound = !s.teleportSound;
            case "backOnDeath" -> s.backOnDeath = !s.backOnDeath;
            default -> {
                source.sendFailure(Component.literal("Unknown setting."));
                return 0;
            }
        }
        CcfmsMod.settingsManager.save();
        return showMenu(source);
    }
}
