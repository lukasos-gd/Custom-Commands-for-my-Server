package com.lukasos.ccfms.commands;

import com.lukasos.ccfms.CcfmsMod;
import com.lukasos.ccfms.data.HomeLocation;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class HomeCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("home")
                .executes(ctx -> goHome(ctx.getSource(), com.lukasos.ccfms.data.HomeManager.DEFAULT_NAME))
                .then(argument("name", StringArgumentType.word())
                        .executes(ctx -> goHome(ctx.getSource(), StringArgumentType.getString(ctx, "name")))));

        dispatcher.register(literal("sethome")
                .executes(ctx -> setHome(ctx.getSource(), com.lukasos.ccfms.data.HomeManager.DEFAULT_NAME))
                .then(argument("name", StringArgumentType.word())
                        .executes(ctx -> setHome(ctx.getSource(), StringArgumentType.getString(ctx, "name")))));

        dispatcher.register(literal("delhome")
                .then(argument("name", StringArgumentType.word())
                        .executes(ctx -> delHome(ctx.getSource(), StringArgumentType.getString(ctx, "name")))));

        dispatcher.register(literal("homes")
                .executes(ctx -> listHomes(ctx.getSource())));
    }

    private static int goHome(CommandSourceStack source, String name) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Only players can use this command."));
            return 0;
        }
        HomeLocation home = CcfmsMod.homeManager.getHome(player.getUUID(), name);
        if (home == null) {
            source.sendFailure(Component.literal("You don't have a home named '" + name + "'. Use /sethome to set one."));
            return 0;
        }
        ServerLevel world = CcfmsMod.worldFromDimensionId(source.getServer(), home.dimension);
        if (world == null) {
            source.sendFailure(Component.literal("That home's dimension no longer exists."));
            return 0;
        }
        CcfmsMod.backManager.record(player.getUUID(), CcfmsMod.currentLocation(player));
        CcfmsMod.teleport(player, world, home.x, home.y, home.z, home.yaw, home.pitch);
        source.sendSuccess(() -> Component.literal("Teleported to home '" + name + "'."), false);
        return 1;
    }

    private static int setHome(CommandSourceStack source, String name) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Only players can use this command."));
            return 0;
        }
        boolean ok = CcfmsMod.homeManager.setHome(player.getUUID(), name, CcfmsMod.currentLocation(player));
        if (!ok) {
            source.sendFailure(Component.literal("You've reached the max number of homes."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Home '" + name + "' set."), false);
        return 1;
    }

    private static int delHome(CommandSourceStack source, String name) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        boolean removed = CcfmsMod.homeManager.deleteHome(player.getUUID(), name);
        if (removed) {
            source.sendSuccess(() -> Component.literal("Home '" + name + "' deleted."), false);
            return 1;
        } else {
            source.sendFailure(Component.literal("You don't have a home named '" + name + "'."));
            return 0;
        }
    }

    private static int listHomes(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        Map<String, HomeLocation> homes = CcfmsMod.homeManager.getAllHomes(player.getUUID());
        if (homes.isEmpty()) {
            source.sendSuccess(() -> Component.literal("You have no homes set."), false);
            return 1;
        }
        source.sendSuccess(() -> Component.literal("Your homes: " + String.join(", ", homes.keySet())), false);
        return 1;
    }
}
