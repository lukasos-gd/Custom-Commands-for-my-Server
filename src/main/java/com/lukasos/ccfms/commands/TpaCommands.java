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
                        .executes(ctx -> sendRequest(ctx.getSource(), EntityArgument.getPlayer(ctx, "target")))));

        dispatcher.register(literal("tpaccept")
                .executes(ctx -> respond(ctx.getSource(), true)));

        dispatcher.register(literal("tpdeny")
                .executes(ctx -> respond(ctx.getSource(), false)));

        dispatcher.register(literal("tpcancel")
                .executes(ctx -> cancel(ctx.getSource())));
    }

    private static int sendRequest(CommandSourceStack source, ServerPlayer target) {
        ServerPlayer sender = source.getPlayer();
        if (sender == null) {
            source.sendFailure(Component.literal("Only players can use this command."));
            return 0;
        }
        if (sender.getUUID().equals(target.getUUID())) {
            source.sendFailure(Component.literal("You can't send a teleport request to yourself."));
            return 0;
        }

        CcfmsMod.tpaManager.addRequest(sender.getUUID(), target.getUUID());

        source.sendSuccess(() -> Component.literal("Teleport request sent to " + target.getName().getString() + "."), false);

        Component accept = Component.literal("[Accept]").withStyle(ChatFormatting.GREEN)
                .withStyle(s -> s.withClickEvent(new ClickEvent.RunCommand("/tpaccept")));
        Component deny = Component.literal("[Deny]").withStyle(ChatFormatting.RED)
                .withStyle(s -> s.withClickEvent(new ClickEvent.RunCommand("/tpdeny")));

        target.sendSystemMessage(Component.literal(sender.getName().getString() + " wants to teleport to you. ")
                .append(accept).append(Component.literal(" ")).append(deny));

        return 1;
    }

    private static int respond(CommandSourceStack source, boolean accept) {
        ServerPlayer target = source.getPlayer();
        if (target == null) return 0;

        TpaManager.Request req = CcfmsMod.tpaManager.getRequest(target.getUUID());
        if (req == null) {
            source.sendFailure(Component.literal("You have no pending teleport requests."));
            return 0;
        }
        CcfmsMod.tpaManager.clear(target.getUUID());

        MinecraftServer server = source.getServer();
        ServerPlayer sender = server.getPlayerList().getPlayer(req.from);
        if (sender == null) {
            source.sendFailure(Component.literal("That player is no longer online."));
            return 0;
        }

        if (!accept) {
            sender.sendSystemMessage(Component.literal(target.getName().getString() + " denied your teleport request."));
            source.sendSuccess(() -> Component.literal("Teleport request denied."), false);
            return 1;
        }

        CcfmsMod.backManager.record(sender.getUUID(), CcfmsMod.currentLocation(sender));
        ServerLevel destWorld = target.level();
        CcfmsMod.teleport(sender, destWorld, target.getX(), target.getY(), target.getZ(), sender.getYRot(), sender.getXRot());

        sender.sendSystemMessage(Component.literal("Teleport request accepted."));
        target.sendSystemMessage(Component.literal(sender.getName().getString() + " has teleported to you."));
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
