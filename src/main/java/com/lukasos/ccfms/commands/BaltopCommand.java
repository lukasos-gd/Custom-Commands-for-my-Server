package com.lukasos.ccfms.commands;

import com.lukasos.ccfms.CcfmsMod;
import com.lukasos.ccfms.data.PlayerRecord;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

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
            source.sendSuccess(() -> Component.literal("No balances recorded yet."), false);
            return 1;
        }

        StringBuilder sb = new StringBuilder("--- Top Balances ---\n");
        int rank = 1;
        for (Map.Entry<UUID, Double> entry : top) {
            PlayerRecord record = CcfmsMod.playerRegistry.getRecord(entry.getKey());
            String name = record != null ? record.name : entry.getKey().toString().substring(0, 8);
            sb.append(String.format("%d. %s - $%.2f%n", rank, name, entry.getValue()));
            rank++;
        }

        String result = sb.toString();
        source.sendSuccess(() -> Component.literal(result), false);
        return 1;
    }
}
