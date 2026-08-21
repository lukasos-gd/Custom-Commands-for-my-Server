package com.lukasos.ccfms.commands;

import com.lukasos.ccfms.CcfmsMod;
import com.lukasos.ccfms.data.TpaManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class TpaCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("tpa")
                .then(argument("target", EntityArgument.player())
                        .executes(ctx -> sendRequest(ctx.getSource(), EntityArgument.getPlayer(ctx, "target"), TpaManager.RequestType.NORMAL))));

        dispatcher.register(literal("tpahere")
                .then(argument("target", EntityArgument.player())
                        .executes(ctx -> sendRequest(ctx.getSource(), EntityArgument.getPlayer(ctx, "target"), TpaManager.RequestType.HERE))));

        dispatcher.register(literal("tpaccept")
                .executes(ctx -> respond(ctx.getSource(), true)));

        dispatcher.register(literal("tpdeny")
                .executes(ctx -> respond(ctx.getSource(), false)));

        dispatcher.register(literal("tpcancel")
                .executes(ctx -> cancel(ctx.getSource())));
    }

    private static int sendRequest(CommandSourceStack source, ServerPlayer target, TpaManager.RequestType type) {
        ServerPlayer sender = source.getPlayer();
        if (sender == null) {
            source.sendFailure(Component.literal("Only players can use this command."));
            return 0;
        }
        if (sender.getUUID().equals(target.getUUID())) {
            source.sendFailure(Component.literal("You can't send a teleport request to yourself."));
            return 0;
        }

        CcfmsMod.tpaManager.addRequest(sender.getUUID(), target.getUUID(), type);

        String verb = type == TpaManager.RequestType.HERE ? "to teleport to them" : "to teleport to you";
        source.sendSuccess(() -> Component.literal("Teleport request sent to " + target.getName().getString() + "."), false);

        Component accept = Component.literal("[Accept]").withStyle(ChatFormatting.GREEN)
                .withStyle(s -> s.withClickEvent(new ClickEvent.RunCommand("/tpaccept")));
        Component deny = Component.literal("[Deny]").withStyle(ChatFormatting.RED)
                .withStyle(s -> s.withClickEvent(new ClickEvent.RunCommand("/tpdeny")));

        String verbForTarget = type == TpaManager.RequestType.HERE ? "wants you to teleport to them" : "wants to teleport to you";
        target.sendSystemMessage(Component.literal(sender.getName().getString() + " " + verbForTarget + ". ")
                .append(accept).append(Component.literal(" ")).append(deny));

        return 1;
    }

    private static int respond(CommandSourceStack source, boolean accept) {
        ServerPlayer responder = source.getPlayer();
        if (responder == null) return 0;

        TpaManager.Request req = CcfmsMod.tpaManager.getRequest(responder.getUUID());
        if (req == null) {
            source.sendFailure(Component.literal("You have no pending teleport requests."));
            return 0;
        }
        CcfmsMod.tpaManager.clear(responder.getUUID());

        MinecraftServer server = source.getServer();
        ServerPlayer requester = server.getPlayerList().getPlayer(req.from);
        if (requester == null) {
            source.sendFailure(Component.literal("That player is no longer online."));
            return 0;
        }

        if (!accept) {
            requester.sendSystemMessage(Component.literal(responder.getName().getString() + " denied your teleport request."));
            source.sendSuccess(() -> Component.literal("Teleport request denied."), false);
            return 1;
        }

        ServerPlayer mover = req.type == TpaManager.RequestType.HERE ? responder : requester;
        ServerPlayer stationary = req.type == TpaManager.RequestType.HERE ? requester : responder;

        CcfmsMod.backManager.record(mover.getUUID(), CcfmsMod.currentLocation(mover));
        ServerLevel destWorld = stationary.level();
        CcfmsMod.teleport(mover, destWorld, stationary.getX(), stationary.getY(), stationary.getZ(), mover.getYRot(), mover.getXRot());

        requester.sendSystemMessage(Component.literal("Teleport request accepted."));
        responder.sendSystemMessage(Component.literal("Teleport request accepted."));
        return 1;
    }

    private static int cancel(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        boolean cancelled = CcfmsMod.tpaManager.cancelSentBy(player.getUUID());
        if (cancelled) {
            source.sendSuccess(() -> Component.literal("Teleport request cancelled."), false);
            return 1;
        }
        source.sendFailure(Component.literal("You don't have a pending outgoing request."));
        return 0;
    }
}
