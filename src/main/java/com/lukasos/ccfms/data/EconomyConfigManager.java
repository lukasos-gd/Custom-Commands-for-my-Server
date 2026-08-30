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

public class EconomyConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("ccfms").resolve("economy").resolve("config.json");

    private EconomyConfigData data;

    public EconomyConfigManager() {
        this.data = load();
    }

    private EconomyConfigData load() {
        try {
            Files.createDirectories(FILE.getParent());
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to create economy config directory: " + e.getMessage());
        }
        if (!Files.exists(FILE)) {
            return new EconomyConfigData();
        }
        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            EconomyConfigData loaded = GSON.fromJson(reader, EconomyConfigData.class);
            return loaded != null ? loaded : new EconomyConfigData();
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to load economy config: " + e.getMessage());
            return new EconomyConfigData();
        }
    }

    private void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to save economy config: " + e.getMessage());
        }
    }

    public EconomyConfigData get() {
        return data;
    }

    public void setScoreboardName(String name) {
        data.scoreboardName = name;
        save();
    }

    public void setScoreboardDisplay(boolean enabled) {
        data.scoreboardDisplay = enabled;
        save();
    }

    public void setShowPlayerPosition(boolean enabled) {
        data.showPlayerPosition = enabled;
        save();
    }
}
