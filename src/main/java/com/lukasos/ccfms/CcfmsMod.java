package com.lukasos.ccfms;

import com.lukasos.ccfms.commands.BalCommand;
import com.lukasos.ccfms.commands.BaltopCommand;
import com.lukasos.ccfms.commands.EconomyAdminCommands;
import com.lukasos.ccfms.commands.HomeCommands;
import com.lukasos.ccfms.commands.ListCommand;
import com.lukasos.ccfms.commands.MiscCommands;
import com.lukasos.ccfms.commands.OffendCommand;
import com.lukasos.ccfms.commands.PlayerInfoCommand;
import com.lukasos.ccfms.commands.RtpCommand;
import com.lukasos.ccfms.commands.SellCommand;
import com.lukasos.ccfms.commands.ShopCommand;
import com.lukasos.ccfms.commands.TpaCommands;
import com.lukasos.ccfms.data.BackManager;
import com.lukasos.ccfms.data.BanManager;
import com.lukasos.ccfms.data.BanRecord;
import com.lukasos.ccfms.data.EconomyConfigManager;
import com.lukasos.ccfms.data.EconomyManager;
import com.lukasos.ccfms.data.EconomyScoreboard;
import com.lukasos.ccfms.data.HomeLocation;
import com.lukasos.ccfms.data.HomeManager;
import com.lukasos.ccfms.data.PlayerRegistry;
import com.lukasos.ccfms.data.SpawnManager;
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

import java.util.Set;

public class CcfmsMod implements ModInitializer {
    public static final String MOD_ID = "ccfms";

    private static final int BACK_SNAPSHOT_INTERVAL_TICKS = 400;
    private static final int SCOREBOARD_UPDATE_INTERVAL_TICKS = 20;

    public static final HomeManager homeManager = new HomeManager();
    public static final BanManager banManager = new BanManager();
    public static final PlayerRegistry playerRegistry = new PlayerRegistry();
    public static final TpaManager tpaManager = new TpaManager();
    public static final BackManager backManager = new BackManager();
    public static final SpawnManager spawnManager = new SpawnManager();
    public static final EconomyManager economyManager = new EconomyManager();
    public static final EconomyConfigManager economyConfigManager = new EconomyConfigManager();

    private int backTickCounter = 0;
    private int scoreboardTickCounter = 0;

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            playerRegistry.recordJoin(player.getUUID(), player.getName().getString());
            BanRecord record = banManager.getBan(player.getUUID());
            if (record != null) {
                handler.disconnect(buildBanMessage(record, banManager.getAppealInfo()));
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            backTickCounter++;
            if (backTickCounter >= BACK_SNAPSHOT_INTERVAL_TICKS) {
                backTickCounter = 0;
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    backManager.record(player.getUUID(), currentLocation(player));
                }
            }

            scoreboardTickCounter++;
            if (scoreboardTickCounter >= SCOREBOARD_UPDATE_INTERVAL_TICKS) {
                scoreboardTickCounter = 0;
                EconomyScoreboard.update(server);
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            HomeCommands.register(dispatcher);
            RtpCommand.register(dispatcher);
            TpaCommands.register(dispatcher);
            MiscCommands.register(dispatcher);
            ListCommand.register(dispatcher);
            OffendCommand.register(dispatcher);
            PlayerInfoCommand.register(dispatcher);
            BalCommand.register(dispatcher);
            BaltopCommand.register(dispatcher);
            EconomyAdminCommands.register(dispatcher);
            ShopCommand.register(dispatcher);
            SellCommand.register(dispatcher);
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
        net.minecraft.network.chat.MutableComponent msg =
                Component.literal("You are BANNED from this server\n").withStyle(ChatFormatting.RED)
                        .append(Component.literal("Reason: ").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(record.reason + "\n").withStyle(ChatFormatting.GOLD))
                        .append(Component.literal("Ban duration: ").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(record.durationLabel + "\n").withStyle(ChatFormatting.GOLD));

        if (record.expiresAt == null) {
            msg.append(Component.literal("You may ").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal("NEVER").withStyle(ChatFormatting.RED))
                    .append(Component.literal(" return on this server\n").withStyle(ChatFormatting.WHITE));
        } else {
            String date = com.lukasos.ccfms.commands.OffendCommand.formatDate(record.expiresAt);
            msg.append(Component.literal("You may return on: ").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(date + "\n").withStyle(ChatFormatting.GREEN));
        }

        if (record.canAppeal) {
            msg.append(Component.literal("You may appeal your ban at: ").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(appealInfo).withStyle(ChatFormatting.AQUA));
        } else {
            msg.append(Component.literal("This ban cannot be appealed.").withStyle(ChatFormatting.WHITE));
        }

        return msg;
    }
}
