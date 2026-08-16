package com.lukasos.ccfms.commands;

import com.lukasos.ccfms.CcfmsMod;
import com.lukasos.ccfms.data.HomeLocation;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class HomeCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
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

    private static int goHome(ServerCommandSource source, String name) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("Only players can use this command."));
            return 0;
        }
        HomeLocation home = CcfmsMod.homeManager.getHome(player.getUuid(), name);
        if (home == null) {
            source.sendError(Text.literal("You don't have a home named '" + name + "'. Use /sethome to set one."));
            return 0;
        }
        ServerWorld world = CcfmsMod.worldFromDimensionId(source.getServer(), home.dimension);
        if (world == null) {
            source.sendError(Text.literal("That home's dimension no longer exists."));
            return 0;
        }
        CcfmsMod.backManager.record(player.getUuid(), CcfmsMod.currentLocation(player));
        CcfmsMod.teleport(player, world, home.x, home.y, home.z, home.yaw, home.pitch);
        source.sendFeedback(() -> Text.literal("Teleported to home '" + name + "'."), false);
        return 1;
    }

    private static int setHome(ServerCommandSource source, String name) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("Only players can use this command."));
            return 0;
        }
        boolean ok = CcfmsMod.homeManager.setHome(player.getUuid(), name, CcfmsMod.currentLocation(player));
        if (!ok) {
            source.sendError(Text.literal("You've reached the max number of homes."));
            return 0;
        }
        source.sendFeedback(() -> Text.literal("Home '" + name + "' set."), false);
        return 1;
    }

    private static int delHome(ServerCommandSource source, String name) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        boolean removed = CcfmsMod.homeManager.deleteHome(player.getUuid(), name);
        if (removed) {
            source.sendFeedback(() -> Text.literal("Home '" + name + "' deleted."), false);
            return 1;
        } else {
            source.sendError(Text.literal("You don't have a home named '" + name + "'."));
            return 0;
        }
    }

    private static int listHomes(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        Map<String, HomeLocation> homes = CcfmsMod.homeManager.getAllHomes(player.getUuid());
        if (homes.isEmpty()) {
            source.sendFeedback(() -> Text.literal("You have no homes set."), false);
            return 1;
        }
        source.sendFeedback(() -> Text.literal("Your homes: " + String.join(", ", homes.keySet())), false);
        return 1;
    }
  }
