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

public class PlayerRegistry {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("ccfms").resolve("players").resolve("players.json");

    private final PlayerRegistryData data;

    public PlayerRegistry() {
        this.data = load();
    }

    private PlayerRegistryData load() {
        try {
            Files.createDirectories(FILE.getParent());
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to create players directory: " + e.getMessage());
        }
        if (!Files.exists(FILE)) {
            return new PlayerRegistryData();
        }
        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            PlayerRegistryData loaded = GSON.fromJson(reader, PlayerRegistryData.class);
            return loaded != null ? loaded : new PlayerRegistryData();
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to load players: " + e.getMessage());
            return new PlayerRegistryData();
        }
    }

    private void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to save players: " + e.getMessage());
        }
    }

    public void recordJoin(UUID uuid, String name) {
        PlayerRecord record = data.players.get(uuid.toString());
        long now = System.currentTimeMillis();
        if (record == null) {
            record = new PlayerRecord();
            record.firstSeen = now;
        }
        record.name = name;
        record.lastSeen = now;
        data.players.put(uuid.toString(), record);
        save();
    }

    public PlayerRecord getRecord(UUID uuid) {
        return data.players.get(uuid.toString());
    }

    public UUID findUuidByName(String name) {
        for (var entry : data.players.entrySet()) {
            if (entry.getValue().name.equalsIgnoreCase(name)) {
                return UUID.fromString(entry.getKey());
            }
        }
        return null;
    }
}
