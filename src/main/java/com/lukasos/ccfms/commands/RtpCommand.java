package com.lukasos.ccfms.commands;

import com.lukasos.ccfms.CcfmsMod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Random;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class RtpCommand {
    private static final Random RANDOM = new Random();

    private static final int OVERWORLD_RADIUS = 5000;
    private static final int NETHER_RADIUS = 1000;
    private static final int END_RADIUS = 2000;
    private static final int MAX_ATTEMPTS = 30;

    private static final SuggestionProvider<CommandSourceStack> DIMENSION_SUGGESTIONS = (ctx, builder) -> {
        builder.suggest("overworld");
        builder.suggest("nether");
        builder.suggest("end");
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("rtp")
                .executes(ctx -> rtp(ctx.getSource(), null))
                .then(argument("dimension", StringArgumentType.word())
                        .suggests(DIMENSION_SUGGESTIONS)
                        .executes(ctx -> rtp(ctx.getSource(), StringArgumentType.getString(ctx, "dimension")))));
    }

    private static int rtp(CommandSourceStack source, String dimensionArg) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Only players can use this command."));
            return 0;
        }

        ResourceKey<Level> dimKey = resolveDimension(dimensionArg, player);
        if (dimKey == null) {
            source.sendFailure(Component.literal("Unknown dimension. Use: overworld, nether, or end."));
            return 0;
        }

        ServerLevel world = source.getServer().getLevel(dimKey);
        if (world == null) {
            source.sendFailure(Component.literal("That dimension isn't loaded on this server."));
            return 0;
        }

        int radius = radiusFor(dimKey);
        BlockPos safe = findSafeLocation(world, radius);
        if (safe == null) {
            source.sendFailure(Component.literal("Couldn't find a safe spot, try again."));
            return 0;
        }

        CcfmsMod.backManager.record(player.getUUID(), CcfmsMod.currentLocation(player));
        CcfmsMod.teleport(player, world, safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5, player.getYRot(), player.getXRot());
        source.sendSuccess(() -> Component.literal(String.format("Teleported to %d, %d, %d.", safe.getX(), safe.getY(), safe.getZ())), false);
        return 1;
    }

    private static ResourceKey<Level> resolveDimension(String arg, ServerPlayer player) {
        if (arg == null) {
            return player.level().dimension();
        }
        return switch (arg.toLowerCase()) {
            case "overworld" -> Level.OVERWORLD;
            case "nether" -> Level.NETHER;
            case "end" -> Level.END;
            default -> null;
        };
    }

    private static int radiusFor(ResourceKey<Level> dim) {
        if (dim.equals(Level.NETHER)) return NETHER_RADIUS;
        if (dim.equals(Level.END)) return END_RADIUS;
        return OVERWORLD_RADIUS;
    }

    private static BlockPos findSafeLocation(ServerLevel world, int radius) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            int x = RANDOM.nextInt(radius * 2) - radius;
            int z = RANDOM.nextInt(radius * 2) - radius;

            world.getChunk(x >> 4, z >> 4);

            int topY;
            if (world.dimension().equals(Level.NETHER)) {
                topY = findNetherFloor(world, x, z);
                if (topY == -1) continue;
            } else {
                topY = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            }

            BlockPos ground = new BlockPos(x, topY - 1, z);
            BlockPos foot = ground.above();
            BlockPos head = foot.above();

            boolean groundOk = !world.getBlockState(ground).isAir() && world.getFluidState(ground).isEmpty();
            boolean spaceOk = world.getBlockState(foot).isAir() && world.getBlockState(head).isAir();

            if (groundOk && spaceOk) {
                return foot;
            }
        }
        return null;
    }

    private static int findNetherFloor(ServerLevel world, int x, int z) {
        for (int y = 120; y > 5; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos above = pos.above();
            BlockPos above2 = above.above();
            boolean groundOk = !world.getBlockState(pos).isAir() && world.getFluidState(pos).isEmpty();
            boolean spaceOk = world.getBlockState(above).isAir() && world.getBlockState(above2).isAir();
            if (groundOk && spaceOk) {
                return y + 1;
            }
        }
        return -1;
    }
}
