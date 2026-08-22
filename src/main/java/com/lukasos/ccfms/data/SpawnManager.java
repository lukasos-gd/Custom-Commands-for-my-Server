package com.lukasos.ccfms.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpawnManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("ccfms").resolve("spawn").resolve("spawn.json");
    private static final Type DATA_TYPE = new TypeToken<Map<String, HomeLocation>>() {}.getType();

    private final Map<String, HomeLocation> data;

    public SpawnManager() {
        this.data = load();
    }

    private Map<String, HomeLocation> load() {
        try {
            Files.createDirectories(FILE.getParent());
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to create spawn directory: " + e.getMessage());
        }
        if (!Files.exists(FILE)) {
            return new HashMap<>();
        }
        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            Map<String, HomeLocation> loaded = GSON.fromJson(reader, DATA_TYPE);
            return loaded != null ? loaded : new HashMap<>();
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to load spawn: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to save spawn: " + e.getMessage());
        }
    }

    public HomeLocation get(UUID player) {
        return data.get(player.toString());
    }

    public void set(UUID player, HomeLocation location) {
        data.put(player.toString(), location);
        save();
    }
}
