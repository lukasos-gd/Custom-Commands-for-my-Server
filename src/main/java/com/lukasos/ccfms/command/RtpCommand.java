package com.lukasos.ccfms.commands;

import com.lukasos.ccfms.CcfmsMod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Heightmap;
import net.minecraft.world.World;

import java.util.Random;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RtpCommand {
    private static final Random RANDOM = new Random();

    private static final int OVERWORLD_RADIUS = 5000;
    private static final int NETHER_RADIUS = 1000;
    private static final int END_RADIUS = 2000;
    private static final int MAX_ATTEMPTS = 30;

    private static final SuggestionProvider<ServerCommandSource> DIMENSION_SUGGESTIONS = (ctx, builder) -> {
        builder.suggest("overworld");
        builder.suggest("nether");
        builder.suggest("end");
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("rtp")
                .executes(ctx -> rtp(ctx.getSource(), null))
                .then(argument("dimension", StringArgumentType.word())
                        .suggests(DIMENSION_SUGGESTIONS)
                        .executes(ctx -> rtp(ctx.getSource(), StringArgumentType.getString(ctx, "dimension")))));
    }

    private static int rtp(ServerCommandSource source, String dimensionArg) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("Only players can use this command."));
            return 0;
        }

        RegistryKey<World> dimKey = resolveDimension(dimensionArg, player);
        if (dimKey == null) {
            source.sendError(Text.literal("Unknown dimension. Use: overworld, nether, or end."));
            return 0;
        }

        ServerWorld world = source.getServer().getWorld(dimKey);
        if (world == null) {
            source.sendError(Text.literal("That dimension isn't loaded on this server."));
            return 0;
        }

        int radius = radiusFor(dimKey);
        BlockPos safe = findSafeLocation(world, radius);
        if (safe == null) {
            source.sendError(Text.literal("Couldn't find a safe spot, try again."));
            return 0;
        }

        CcfmsMod.backManager.record(player.getUuid(), CcfmsMod.currentLocation(player));
        CcfmsMod.teleport(player, world, safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5, player.getYaw(), player.getPitch());
        source.sendFeedback(() -> Text.literal(String.format("Teleported to %d, %d, %d.", safe.getX(), safe.getY(), safe.getZ())), false);
        return 1;
    }

    private static RegistryKey<World> resolveDimension(String arg, ServerPlayerEntity player) {
        if (arg == null) {
            return player.getWorld().getRegistryKey();
        }
        return switch (arg.toLowerCase()) {
            case "overworld" -> World.OVERWORLD;
            case "nether" -> World.NETHER;
            case "end" -> World.END;
            default -> null;
        };
    }

    private static int radiusFor(RegistryKey<World> dim) {
        if (dim.equals(World.NETHER)) return NETHER_RADIUS;
        if (dim.equals(World.END)) return END_RADIUS;
        return OVERWORLD_RADIUS;
    }

    private static BlockPos findSafeLocation(ServerWorld world, int radius) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            int x = RANDOM.nextInt(radius * 2) - radius;
            int z = RANDOM.nextInt(radius * 2) - radius;

            world.getChunk(x >> 4, z >> 4);

            int topY;
            if (world.getRegistryKey().equals(World.NETHER)) {
                topY = findNetherFloor(world, x, z);
                if (topY == -1) continue;
            } else {
                topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
            }

            BlockPos ground = new BlockPos(x, topY - 1, z);
            BlockPos foot = ground.up();
            BlockPos head = foot.up();

            boolean groundOk = world.getBlockState(ground).isSolidBlock(world, ground) && world.getFluidState(ground).isEmpty();
            boolean spaceOk = world.getBlockState(foot).isAir() && world.getBlockState(head).isAir();

            if (groundOk && spaceOk) {
                return foot;
            }
        }
        return null;
    }

    private static int findNetherFloor(ServerWorld world, int x, int z) {
        for (int y = 120; y > 5; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos above = pos.up();
            BlockPos above2 = above.up();
            boolean groundOk = world.getBlockState(pos).isSolidBlock(world, pos) && world.getFluidState(pos).isEmpty();
            boolean spaceOk = world.getBlockState(above).isAir() && world.getBlockState(above2).isAir();
            if (groundOk && spaceOk) {
                return y + 1;
            }
        }
        return -1;
    }
}
