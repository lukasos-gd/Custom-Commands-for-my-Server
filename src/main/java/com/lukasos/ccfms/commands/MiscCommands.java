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
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("back")
                .executes(ctx -> back(ctx.getSource())));

        dispatcher.register(literal("spawn")
                .executes(ctx -> goSpawn(ctx.getSource())));

        dispatcher.register(literal("setspawn")
                .executes(ctx -> setSpawn(ctx.getSource())));
    }

    private static int back(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        HomeLocation loc = CcfmsMod.backManager.get(player.getUUID());
        if (loc == null) {
            source.sendFailure(Component.literal("No saved position yet. Your position is saved automatically every 20 seconds."));
            return 0;
        }
        ServerLevel world = CcfmsMod.worldFromDimensionId(source.getServer(), loc.dimension);
        if (world == null) {
            source.sendFailure(Component.literal("That location's dimension no longer exists."));
            return 0;
        }
        CcfmsMod.teleport(player, world, loc.x, loc.y, loc.z, loc.yaw, loc.pitch);
        source.sendSuccess(() -> Component.literal("Teleported to your last saved position."), false);
        return 1;
    }

    private static int goSpawn(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        HomeLocation spawn = CcfmsMod.spawnManager.get(player.getUUID());
        if (spawn == null) {
            source.sendFailure(Component.literal("You haven't set a custom spawn point yet. Use /setspawn first."));
            return 0;
        }
        ServerLevel world = CcfmsMod.worldFromDimensionId(source.getServer(), spawn.dimension);
        if (world == null) return 0;
        CcfmsMod.teleport(player, world, spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch);
        source.sendSuccess(() -> Component.literal("Teleported to your spawn point."), false);
        return 1;
    }

    private static int setSpawn(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        CcfmsMod.spawnManager.set(player.getUUID(), CcfmsMod.currentLocation(player));
        source.sendSuccess(() -> Component.literal("Your spawn point has been set to your current location."), false);
        return 1;
    }
}
