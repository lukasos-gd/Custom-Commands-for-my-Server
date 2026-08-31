package com.lukasos.ccfms.commands;

import com.lukasos.ccfms.CcfmsMod;
import com.lukasos.ccfms.data.PlayerRecord;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.commands.Commands.literal;

public class BaltopCommand {
    private static final int TOP_COUNT = 10;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("baltop")
                .executes(ctx -> baltop(ctx.getSource())));
    }

    private static int baltop(CommandSourceStack source) {
        List<Map.Entry<UUID, Double>> top = CcfmsMod.economyManager.topBalances(TOP_COUNT);
        if (top.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No balances recorded yet.").withStyle(ChatFormatting.GRAY), false);
            return 1;
        }

        MutableComponent msg = Component.literal("--- Top Balances ---\n").withStyle(ChatFormatting.GOLD);
        int rank = 1;
        for (Map.Entry<UUID, Double> entry : top) {
            PlayerRecord record = CcfmsMod.playerRegistry.getRecord(entry.getKey());
            String name = record != null ? record.name : entry.getKey().toString().substring(0, 8);

            ChatFormatting rankColor = switch (rank) {
                case 1 -> ChatFormatting.GOLD;
                case 2 -> ChatFormatting.GRAY;
                case 3 -> ChatFormatting.RED;
                default -> ChatFormatting.WHITE;
            };

            msg.append(Component.literal(rank + ". ").withStyle(rankColor))
                    .append(Component.literal(name).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(String.format("$%.2f", entry.getValue())).withStyle(ChatFormatting.GREEN))
                    .append(Component.literal("\n"));

            rank++;
        }

        source.sendSuccess(() -> msg, false);
        return 1;
    }
}
