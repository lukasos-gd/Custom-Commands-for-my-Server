package com.lukasos.ccfms.commands;

import com.lukasos.ccfms.CcfmsMod;
import com.lukasos.ccfms.data.HomeLocation;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

public class MiscCommands {
    private static final String SPAWN_KEY = "global";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("back")
                .executes(ctx -> back(ctx.getSource())));

        dispatcher.register(literal("spawn")
                .executes(ctx -> goSpawn(ctx.getSource())));

        dispatcher.register(literal("setspawn")
                .requires(src -> {
                    ServerPlayer player = src.getPlayer();
                    return player == null || src.getServer().getPlayerList().isOp(player.nameAndId());
                })
                .executes(ctx -> setSpawn(ctx.getSource())));
    }

    private static int back(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        HomeLocation loc = CcfmsMod.backManager.get(player.getUUID());
        if (loc == null) {
            source.sendFailure(Component.literal("You have nowhere to go back to."));
            return 0;
        }
        ServerLevel world = CcfmsMod.worldFromDimensionId(source.getServer(), loc.dimension);
        if (world == null) {
            source.sendFailure(Component.literal("That location's dimension no longer exists."));
            return 0;
        }
        HomeLocation current = CcfmsMod.currentLocation(player);
        CcfmsMod.teleport(player, world, loc.x, loc.y, loc.z, loc.yaw, loc.pitch);
        CcfmsMod.backManager.record(player.getUUID(), current);
        source.sendSuccess(() -> Component.literal("Teleported back."), false);
        return 1;
    }

    private static int goSpawn(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        HomeLocation spawn = CcfmsMod.spawnPoints.get(SPAWN_KEY);
        if (spawn == null) {
            source.sendFailure(Component.literal("No spawn point has been set yet. Ask an admin to run /setspawn."));
            return 0;
        }
        ServerLevel world = CcfmsMod.worldFromDimensionId(source.getServer(), spawn.dimension);
        if (world == null) return 0;
        CcfmsMod.backManager.record(player.getUUID(), CcfmsMod.currentLocation(player));
        CcfmsMod.teleport(player, world, spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch);
        source.sendSuccess(() -> Component.literal("Teleported to spawn."), false);
        return 1;
    }

    private static int setSpawn(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        CcfmsMod.spawnPoints.put(SPAWN_KEY, CcfmsMod.currentLocation(player));
        source.sendSuccess(() -> Component.literal("Spawn point set to your current location."), true);
        return 1;
    }
}
