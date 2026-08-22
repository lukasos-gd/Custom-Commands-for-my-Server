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

public class SpawnManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("ccfms").resolve("spawn").resolve("spawn.json");

    private HomeLocation spawn;

    public SpawnManager() {
        this.spawn = load();
    }

    private HomeLocation load() {
        try {
            Files.createDirectories(FILE.getParent());
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to create spawn directory: " + e.getMessage());
        }
        if (!Files.exists(FILE)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, HomeLocation.class);
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to load spawn: " + e.getMessage());
            return null;
        }
    }

    private void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(spawn, writer);
            }
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to save spawn: " + e.getMessage());
        }
    }

    public HomeLocation get() {
        return spawn;
    }

    public void set(HomeLocation location) {
        this.spawn = location;
        save();
    }
}
