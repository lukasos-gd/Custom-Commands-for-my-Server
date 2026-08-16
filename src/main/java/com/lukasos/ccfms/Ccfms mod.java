package com.lukasos.ccfms;

import com.lukasos.ccfms.commands.HomeCommands;
import com.lukasos.ccfms.commands.MiscCommands;
import com.lukasos.ccfms.commands.RtpCommand;
import com.lukasos.ccfms.commands.SettingsCommand;
import com.lukasos.ccfms.commands.TpaCommands;
import com.lukasos.ccfms.data.BackManager;
import com.lukasos.ccfms.data.HomeLocation;
import com.lukasos.ccfms.data.HomeManager;
import com.lukasos.ccfms.data.SettingsManager;
import com.lukasos.ccfms.data.TpaManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

public class CcfmsMod implements ModInitializer {
    public static final String MOD_ID = "ccfms";

    public static HomeManager homeManager;
    public static SettingsManager settingsManager;
    public static final TpaManager tpaManager = new TpaManager();
    public static final BackManager backManager = new BackManager();
    public static final Map<String, HomeLocation> spawnPoints = new HashMap<>();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            homeManager = new HomeManager(server);
            settingsManager = new SettingsManager(server);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (settingsManager != null) settingsManager.save();
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            HomeCommands.register(dispatcher);
            RtpCommand.register(dispatcher);
            TpaCommands.register(dispatcher);
            SettingsCommand.register(dispatcher);
            MiscCommands.register(dispatcher);
        });
    }

    public static void teleport(ServerPlayerEntity player, ServerWorld world, double x, double y, double z, float yaw, float pitch) {
        player.teleport(world, x, y, z, yaw, pitch);
    }

    public static HomeLocation currentLocation(ServerPlayerEntity player) {
        Vec3d pos = player.getPos();
        return new HomeLocation(
                player.getWorld().getRegistryKey().getValue().toString(),
                pos.x, pos.y, pos.z,
                player.getYaw(), player.getPitch()
        );
    }

    public static ServerWorld worldFromDimensionId(MinecraftServer server, String dimensionId) {
        for (ServerWorld w : server.getWorlds()) {
            if (w.getRegistryKey().getValue().toString().equals(dimensionId)) {
                return w;
            }
        }
        return null;
    }
}
