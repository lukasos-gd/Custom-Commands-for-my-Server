package com.lukasos.ccfms.data;

import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public class BanManager {
    private static final String FILE = "offend.json";

    private final MinecraftServer server;
    private final OffendData data;

    public BanManager(MinecraftServer server) {
        this.server = server;
        this.data = DataStore.load(server, FILE, OffendData.class, new OffendData());
    }

    public void ban(UUID player, BanRecord record) {
        data.bans.put(player.toString(), record);
        save();
    }

    public boolean pardonByName(String name) {
        String matchKey = null;
        for (var entry : data.bans.entrySet()) {
            if (entry.getValue().targetName.equalsIgnoreCase(name)) {
                matchKey = entry.getKey();
                break;
            }
        }
        if (matchKey == null) return false;
        data.bans.remove(matchKey);
        save();
        return true;
    }

    public BanRecord getBan(UUID player) {
        BanRecord record = data.bans.get(player.toString());
        if (record == null) return null;
        if (record.expiresAt != null && System.currentTimeMillis() > record.expiresAt) {
            data.bans.remove(player.toString());
            save();
            return null;
        }
        return record;
    }

    public void setAppealInfo(String text) {
        data.appealInfo = text;
        save();
    }

    public String getAppealInfo() {
        return data.appealInfo;
    }

    private void save() {
        DataStore.save(server, FILE, data);
    }
}
