package com.lukasos.ccfms.commands;

import com.lukasos.ccfms.CcfmsMod;
import com.lukasos.ccfms.data.TpaManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TpaCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("tpa")
                .then(argument("target", EntityArgumentType.player())
                        .executes(ctx -> sendRequest(ctx.getSource(), EntityArgumentType.getPlayer(ctx, "target")))));

        dispatcher.register(literal("tpaccept")
                .executes(ctx -> respond(ctx.getSource(), true)));

        dispatcher.register(literal("tpdeny")
                .executes(ctx -> respond(ctx.getSource(), false)));

        dispatcher.register(literal("tpcancel")
                .executes(ctx -> cancel(ctx.getSource())));
    }

    private static int sendRequest(ServerCommandSource source, ServerPlayerEntity target) {
        ServerPlayerEntity sender = source.getPlayer();
        if (sender == null) {
            source.sendError(Text.literal("Only players can use this command."));
            return 0;
        }
        if (sender.getUuid().equals(target.getUuid())) {
            source.sendError(Text.literal("You can't send a teleport request to yourself."));
            return 0;
        }
        if (!CcfmsMod.settingsManager.get(target.getUuid()).acceptTpaRequests) {
            source.sendError(Text.literal(target.getName().getString() + " isn't accepting teleport requests right now."));
            return 0;
        }

        CcfmsMod.tpaManager.addRequest(sender.getUuid(), target.getUuid());

        source.sendFeedback(() -> Text.literal("Teleport request sent to " + target.getName().getString() + "."), false);

        Text accept = Text.literal("[Accept]").formatted(Formatting.GREEN)
                .styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept")));
        Text deny = Text.literal("[Deny]").formatted(Formatting.RED)
                .styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny")));

        target.sendMessage(Text.literal(sender.getName().getString() + " wants to teleport to you. ")
                .append(accept).append(Text.literal(" ")).append(deny));

        return 1;
    }

    private static int respond(ServerCommandSource source, boolean accept) {
        ServerPlayerEntity target = source.getPlayer();
        if (target == null) return 0;

        TpaManager.Request req = CcfmsMod.tpaManager.getRequest(target.getUuid());
        if (req == null) {
            source.sendError(Text.literal("You have no pending teleport requests."));
            return 0;
        }
        CcfmsMod.tpaManager.clear(target.getUuid());

        MinecraftServer server = source.getServer();
        ServerPlayerEntity sender = server.getPlayerManager().getPlayer(req.from);
        if (sender == null) {
            source.sendError(Text.literal("That player is no longer online."));
            return 0;
        }

        if (!accept) {
            sender.sendMessage(Text.literal(target.getName().getString() + " denied your teleport request."));
            source.sendFeedback(() -> Text.literal("Teleport request denied."), false);
            return 1;
        }

        CcfmsMod.backManager.record(sender.getUuid(), CcfmsMod.currentLocation(sender));
        ServerWorld destWorld = target.getServerWorld();
        CcfmsMod.teleport(sender, destWorld, target.getX(), target.getY(), target.getZ(), sender.getYaw(), sender.getPitch());

        sender.sendMessage(Text.literal("Teleport request accepted."));
        target.sendMessage(Text.literal(sender.getName().getString() + " has teleported to you."));
        return 1;
    }

    private static int cancel(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        boolean cancelled = CcfmsMod.tpaManager.cancelSentBy(player.getUuid());
        if (cancelled) {
            source.sendFeedback(() -> Text.literal("Teleport request cancelled."), false);
            return 1;
        }
        source.sendError(Text.literal("You don't have a pending outgoing request."));
        return 0;
    }
}
