package com.lukasos.ccfms.commands;

import com.lukasos.ccfms.CcfmsMod;
import com.lukasos.ccfms.data.HomeLocation;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class MiscCommands {
    private static final String SPAWN_KEY = "global";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("back")
                .executes(ctx -> back(ctx.getSource())));

        dispatcher.register(literal("spawn")
                .executes(ctx -> goSpawn(ctx.getSource())));

        dispatcher.register(literal("setspawn")
                .requires(src -> src.hasPermissionLevel(2))
                .executes(ctx -> setSpawn(ctx.getSource())));
    }

    private static int back(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        HomeLocation loc = CcfmsMod.backManager.get(player.getUuid());
        if (loc == null) {
            source.sendError(Text.literal("You have nowhere to go back to."));
            return 0;
        }
        ServerWorld world = CcfmsMod.worldFromDimensionId(source.getServer(), loc.dimension);
        if (world == null) {
            source.sendError(Text.literal("That location's dimension no longer exists."));
            return 0;
        }
        HomeLocation current = CcfmsMod.currentLocation(player);
        CcfmsMod.teleport(player, world, loc.x, loc.y, loc.z, loc.yaw, loc.pitch);
        CcfmsMod.backManager.record(player.getUuid(), current);
        source.sendFeedback(() -> Text.literal("Teleported back."), false);
        return 1;
    }

    private static int goSpawn(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        HomeLocation spawn = CcfmsMod.spawnPoints.get(SPAWN_KEY);
        if (spawn == null) {
            source.sendError(Text.literal("No spawn point has been set yet. Ask an admin to run /setspawn."));
            return 0;
        }
        ServerWorld world = CcfmsMod.worldFromDimensionId(source.getServer(), spawn.dimension);
        if (world == null) return 0;
        CcfmsMod.backManager.record(player.getUuid(), CcfmsMod.currentLocation(player));
        CcfmsMod.teleport(player, world, spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch);
        source.sendFeedback(() -> Text.literal("Teleported to spawn."), false);
        return 1;
    }

    private static int setSpawn(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        CcfmsMod.spawnPoints.put(SPAWN_KEY, CcfmsMod.currentLocation(player));
        source.sendFeedback(() -> Text.literal("Spawn point set to your current location."), true);
        return 1;
    }
}
