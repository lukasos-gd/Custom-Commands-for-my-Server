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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BanManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path BANS_DIR = FabricLoader.getInstance().getConfigDir().resolve("ccfms").resolve("bans");
    private static final Path ACTIVE_FILE = BANS_DIR.resolve("banned-players.json");
    private static final Path HISTORY_FILE = BANS_DIR.resolve("bans-record.json");

    private final OffendData active;
    private final BanHistoryData history;

    public BanManager() {
        this.active = loadJson(ACTIVE_FILE, OffendData.class, new OffendData());
        this.history = loadJson(HISTORY_FILE, BanHistoryData.class, new BanHistoryData());
    }

    private <T> T loadJson(Path file, Class<T> type, T fallback) {
        try {
            Files.createDirectories(BANS_DIR);
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to create bans directory: " + e.getMessage());
        }
        if (!Files.exists(file)) {
            return fallback;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            T loaded = GSON.fromJson(reader, type);
            return loaded != null ? loaded : fallback;
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to load " + file.getFileName() + ": " + e.getMessage());
            return fallback;
        }
    }

    private void saveActive() {
        saveJson(ACTIVE_FILE, active);
    }

    private void saveHistory() {
        saveJson(HISTORY_FILE, history);
    }

    private void saveJson(Path file, Object data) {
        try {
            Files.createDirectories(BANS_DIR);
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to save " + file.getFileName() + ": " + e.getMessage());
        }
    }

    public void ban(UUID player, BanRecord record) {
        active.bans.put(player.toString(), record);
        saveActive();

        List<BanRecord> entries = history.history.computeIfAbsent(player.toString(), k -> new ArrayList<>());
        entries.add(record);
        saveHistory();
    }

    public boolean unoffendByName(String name) {
        String matchKey = null;
        for (var entry : active.bans.entrySet()) {
            if (entry.getValue().targetName.equalsIgnoreCase(name)) {
                matchKey = entry.getKey();
                break;
            }
        }
        if (matchKey == null) return false;
        active.bans.remove(matchKey);
        saveActive();
        return true;
    }

    public BanRecord getBan(UUID player) {
        BanRecord record = active.bans.get(player.toString());
        if (record == null) return null;
        if (record.expiresAt != null && System.currentTimeMillis() > record.expiresAt) {
            active.bans.remove(player.toString());
            saveActive();
            return null;
        }
        return record;
    }

    public List<BanRecord> getHistory(UUID player) {
        List<BanRecord> entries = history.history.get(player.toString());
        return entries != null ? entries : List.of();
    }

    public void setAppealInfo(String text) {
        active.appealInfo = text;
        saveActive();
    }

    public String getAppealInfo() {
        return active.appealInfo;
    }
}
