package com.lukasos.ccfms.data;

import com.lukasos.ccfms.CcfmsMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.Comparator;
import java.util.List;

public class EconomyScoreboard {
    private static final String OBJECTIVE_NAME = "ccfms_money";
    private static final String RANK_TEAM_PREFIX = "ccfms_rank_";
    private static Objective objective;

    public static void update(MinecraftServer server) {
        EconomyConfigData config = CcfmsMod.economyConfigManager.get();
        Scoreboard scoreboard = server.getScoreboard();

        if (!config.scoreboardDisplay) {
            if (objective != null) {
                scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, null);
            }
            return;
        }

        if (objective == null) {
            objective = scoreboard.getObjective(OBJECTIVE_NAME);
            if (objective == null) {
                objective = scoreboard.addObjective(OBJECTIVE_NAME, ObjectiveCriteria.DUMMY,
                        Component.literal(config.scoreboardName), ObjectiveCriteria.RenderType.INTEGER, true, null);
            }
        }
        scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, objective);

        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        List<ServerPlayer> sorted = players.stream()
                .sorted(Comparator.comparingDouble((ServerPlayer p) -> CcfmsMod.economyManager.getBalance(p.getUUID())).reversed())
                .toList();

        int rank = 1;
        for (ServerPlayer player : sorted) {
            double balance = CcfmsMod.economyManager.getBalance(player.getUUID());
            scoreboard.getOrCreatePlayerScore(player, objective).set((int) Math.round(balance));

            if (config.showPlayerPosition) {
                assignRankTeam(scoreboard, player, rank);
            }
            rank++;
        }
    }

    private static void assignRankTeam(Scoreboard scoreboard, ServerPlayer player, int rank) {
        String teamName = RANK_TEAM_PREFIX + rank;
        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            team.setPlayerPrefix(Component.literal("#" + rank + " ").withStyle(ChatFormatting.GRAY));
        }
        scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
    }
}
