package com.lukasos.ccfms.commands;

import com.lukasos.ccfms.CcfmsMod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class EconomyAdminCommands {
    private static final SuggestionProvider<CommandSourceStack> SCOREBOARD_FIELD_SUGGESTIONS = (ctx, builder) -> {
        builder.suggest("name");
        builder.suggest("display");
        builder.suggest("show-player-position");
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("add-money")
                .requires(EconomyAdminCommands::isOp)
                .then(argument("player", EntityArgument.player())
                        .then(argument("amount", DoubleArgumentType.doubleArg(0.01))
                                .executes(ctx -> addMoney(ctx.getSource(),
                                        EntityArgument.getPlayer(ctx, "player"),
                                        DoubleArgumentType.getDouble(ctx, "amount"))))));

        dispatcher.register(literal("remove-money")
                .requires(EconomyAdminCommands::isOp)
                .then(argument("player", EntityArgument.player())
                        .then(argument("amount", DoubleArgumentType.doubleArg(0.01))
                                .executes(ctx -> removeMoney(ctx.getSource(),
                                        EntityArgument.getPlayer(ctx, "player"),
                                        DoubleArgumentType.getDouble(ctx, "amount"))))));

        dispatcher.register(literal("economy")
                .requires(EconomyAdminCommands::isOp)
                .then(literal("config")
                        .then(literal("money-scoreboard")
                                .then(argument("field", StringArgumentType.word())
                                        .suggests(SCOREBOARD_FIELD_SUGGESTIONS)
                                        .then(argument("value", StringArgumentType.greedyString())
                                                .executes(ctx -> configScoreboard(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "field"),
                                                        StringArgumentType.getString(ctx, "value"))))))));
    }

    private static boolean isOp(CommandSourceStack src) {
        ServerPlayer player = src.getPlayer();
        return player == null || src.getServer().getPlayerList().isOp(player.nameAndId());
    }

    private static int addMoney(CommandSourceStack source, ServerPlayer target, double amount) {
        CcfmsMod.economyManager.addBalance(target.getUUID(), amount);
        double newBalance = CcfmsMod.economyManager.getBalance(target.getUUID());
        source.sendSuccess(() -> Component.literal(String.format("Added $%.2f to %s. New balance: $%.2f",
                amount, target.getName().getString(), newBalance)), true);
        return 1;
    }

    private static int removeMoney(CommandSourceStack source, ServerPlayer target, double amount) {
        boolean success = CcfmsMod.economyManager.removeBalance(target.getUUID(), amount);
        if (!success) {
            source.sendFailure(Component.literal(target.getName().getString() + " doesn't have enough money to remove $" + amount + "."));
            return 0;
        }
        double newBalance = CcfmsMod.economyManager.getBalance(target.getUUID());
        source.sendSuccess(() -> Component.literal(String.format("Removed $%.2f from %s. New balance: $%.2f",
                amount, target.getName().getString(), newBalance)), true);
        return 1;
    }

    private static int configScoreboard(CommandSourceStack source, String field, String value) {
        switch (field.toLowerCase()) {
            case "name" -> {
                CcfmsMod.economyConfigManager.setScoreboardName(value);
                source.sendSuccess(() -> Component.literal("Scoreboard name set to: " + value), true);
            }
            case "display" -> {
                boolean enabled = parseBool(value);
                CcfmsMod.economyConfigManager.setScoreboardDisplay(enabled);
                source.sendSuccess(() -> Component.literal("Scoreboard display " + (enabled ? "enabled" : "disabled") + "."), true);
            }
            case "show-player-position" -> {
                boolean enabled = parseBool(value);
                CcfmsMod.economyConfigManager.setShowPlayerPosition(enabled);
                source.sendSuccess(() -> Component.literal("Show player position " + (enabled ? "enabled" : "disabled") + "."), true);
            }
            default -> {
                source.sendFailure(Component.literal("Unknown field. Use: name, display, or show-player-position."));
                return 0;
            }
        }
        return 1;
    }

    private static boolean parseBool(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("on");
    }
}
