package com.lukasos.ccfms.commands;

import com.lukasos.ccfms.CcfmsMod;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class BalCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("bal")
                .executes(ctx -> balSelf(ctx.getSource()))
                .then(argument("player", EntityArgument.player())
                        .executes(ctx -> balOther(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));
    }

    private static int balSelf(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Only players can use this command."));
            return 0;
        }
        double balance = CcfmsMod.economyManager.getBalance(player.getUUID());
        source.sendSuccess(() -> Component.literal("Your balance: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("$%.2f", balance)).withStyle(ChatFormatting.GOLD)), false);
        return 1;
    }

    private static int balOther(CommandSourceStack source, ServerPlayer target) {
        double balance = CcfmsMod.economyManager.getBalance(target.getUUID());
        source.sendSuccess(() -> Component.literal(target.getName().getString() + "'s balance: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("$%.2f", balance)).withStyle(ChatFormatting.GOLD)), false);
        return 1;
    }
}
