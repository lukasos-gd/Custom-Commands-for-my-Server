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

public class HomeManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("ccfms").resolve("homes").resolve("homes.json");
    private static final Type DATA_TYPE = new TypeToken<Map<String, Map<String, HomeLocation>>>() {}.getType();

    public static final String DEFAULT_NAME = "home";
    public static final int MAX_HOMES = 10;

    private final Map<String, Map<String, HomeLocation>> data;

    public HomeManager() {
        this.data = load();
    }

    private Map<String, Map<String, HomeLocation>> load() {
        try {
            Files.createDirectories(FILE.getParent());
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to create homes directory: " + e.getMessage());
        }
        if (!Files.exists(FILE)) {
            return new HashMap<>();
        }
        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            Map<String, Map<String, HomeLocation>> loaded = GSON.fromJson(reader, DATA_TYPE);
            return loaded != null ? loaded : new HashMap<>();
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to load homes: " + e.getMessage());
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
            System.err.println("[ccfms] Failed to save homes: " + e.getMessage());
        }
    }

    private Map<String, HomeLocation> homesFor(UUID player) {
        return data.computeIfAbsent(player.toString(), k -> new HashMap<>());
    }

    public boolean setHome(UUID player, String name, HomeLocation loc) {
        Map<String, HomeLocation> homes = homesFor(player);
        if (!homes.containsKey(name) && homes.size() >= MAX_HOMES) {
            return false;
        }
        homes.put(name, loc);
        save();
        return true;
    }

    public HomeLocation getHome(UUID player, String name) {
        return homesFor(player).get(name);
    }

    public boolean deleteHome(UUID player, String name) {
        boolean removed = homesFor(player).remove(name) != null;
        if (removed) save();
        return removed;
    }

    public Map<String, HomeLocation> getAllHomes(UUID player) {
        return homesFor(player);
    }
}
