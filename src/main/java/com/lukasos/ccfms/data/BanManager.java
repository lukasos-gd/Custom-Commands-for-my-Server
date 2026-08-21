package com.lukasos.ccfms.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class BanManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("ccfms").resolve("bans").resolve("banned-players.json");

    private final OffendData data;

    public BanManager() {
        this.data = load();
    }

    private OffendData load() {
        try {
            Files.createDirectories(FILE.getParent());
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to create bans directory: " + e.getMessage());
        }
        if (!Files.exists(FILE)) {
            return new OffendData();
        }
        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            OffendData loaded = GSON.fromJson(reader, OffendData.class);
            return loaded != null ? loaded : new OffendData();
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to load bans: " + e.getMessage());
            return new OffendData();
        }
    }

    private void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to save bans: " + e.getMessage());
        }
    }

    public void ban(UUID player, BanRecord record) {
        data.bans.put(player.toString(), record);
        save();
    }

    public boolean unoffendByName(String name) {
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
}
