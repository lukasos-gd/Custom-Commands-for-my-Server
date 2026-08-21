package com.lukasos.ccfms.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.commands.Commands.literal;

public class ListCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("list")
                .executes(ctx -> list(ctx.getSource())));
    }

    private static int list(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        String names = players.stream().map(p -> p.getName().getString()).collect(Collectors.joining(", "));
        if (players.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No players online."), false);
            return 1;
        }
        source.sendSuccess(() -> Component.literal("Online (" + players.size() + "): " + names), false);
        return 1;
    }
}
