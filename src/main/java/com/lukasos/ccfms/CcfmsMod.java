package com.lukasos.ccfms;

import com.lukasos.ccfms.commands.HomeCommands;
import com.lukasos.ccfms.commands.ListCommand;
import com.lukasos.ccfms.commands.MiscCommands;
import com.lukasos.ccfms.commands.OffendCommand;
import com.lukasos.ccfms.commands.RtpCommand;
import com.lukasos.ccfms.commands.TpaCommands;
import com.lukasos.ccfms.data.BackManager;
import com.lukasos.ccfms.data.BanManager;
import com.lukasos.ccfms.data.BanRecord;
import com.lukasos.ccfms.data.HomeLocation;
import com.lukasos.ccfms.data.HomeManager;
import com.lukasos.ccfms.data.TpaManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CcfmsMod implements ModInitializer {
    public static final String MOD_ID = "ccfms";

    private static final int BACK_SNAPSHOT_INTERVAL_TICKS = 400;

    public static final HomeManager homeManager = new HomeManager();
    public static final BanManager banManager = new BanManager();
    public static final TpaManager tpaManager = new TpaManager();
    public static final BackManager backManager = new BackManager();
    public static final Map<String, HomeLocation> spawnPoints = new HashMap<>();

    private int tickCounter = 0;

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            BanRecord record = banManager.getBan(player.getUUID());
            if (record != null) {
                handler.disconnect(buildBanMessage(record, banManager.getAppealInfo()));
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter >= BACK_SNAPSHOT_INTERVAL_TICKS) {
                tickCounter = 0;
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    backManager.record(player.getUUID(), currentLocation(player));
                }
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            HomeCommands.register(dispatcher);
            RtpCommand.register(dispatcher);
            TpaCommands.register(dispatcher);
            MiscCommands.register(dispatcher);
            ListCommand.register(dispatcher);
            OffendCommand.register(dispatcher);
        });
    }

    public static void teleport(ServerPlayer player, ServerLevel world, double x, double y, double z, float yaw, float pitch) {
        player.teleportTo(world, x, y, z, Set.<Relative>of(), yaw, pitch, false);
    }

    public static HomeLocation currentLocation(ServerPlayer player) {
        Vec3 pos = player.position();
        return new HomeLocation(
                player.level().dimension().identifier().toString(),
                pos.x, pos.y, pos.z,
                player.getYRot(), player.getXRot()
        );
    }

    public static ServerLevel worldFromDimensionId(MinecraftServer server, String dimensionId) {
        for (ServerLevel w : server.getAllLevels()) {
            if (w.dimension().identifier().toString().equals(dimensionId)) {
                return w;
            }
        }
        return null;
    }

    public static Component buildBanMessage(BanRecord record, String appealInfo) {
        String expiry = com.lukasos.ccfms.commands.OffendCommand.formatExpiry(record.expiresAt);
        return Component.literal("You are banned from this server\n\n").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                .append(Component.literal("Reason:  ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(record.reason + "\n").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("Duration: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(expiry + "\n").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("Appeal:  ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(appealInfo).withStyle(ChatFormatting.AQUA));
    }
}
