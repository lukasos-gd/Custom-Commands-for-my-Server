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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class EconomyManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir()
            .resolve("ccfms").resolve("economy").resolve("balances.json");

    public static final double DEFAULT_BALANCE = 0.0;

    private final EconomyData data;

    public EconomyManager() {
        this.data = load();
    }

    private EconomyData load() {
        try {
            Files.createDirectories(FILE.getParent());
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to create economy directory: " + e.getMessage());
        }
        if (!Files.exists(FILE)) {
            return new EconomyData();
        }
        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            EconomyData loaded = GSON.fromJson(reader, EconomyData.class);
            return loaded != null ? loaded : new EconomyData();
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to load economy: " + e.getMessage());
            return new EconomyData();
        }
    }

    private void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to save economy: " + e.getMessage());
        }
    }

    public double getBalance(UUID player) {
        return data.balances.getOrDefault(player.toString(), DEFAULT_BALANCE);
    }

    public void setBalance(UUID player, double amount) {
        data.balances.put(player.toString(), Math.max(0, amount));
        save();
    }

    public void addBalance(UUID player, double amount) {
        setBalance(player, getBalance(player) + amount);
    }

    public boolean removeBalance(UUID player, double amount) {
        double current = getBalance(player);
        if (current < amount) return false;
        setBalance(player, current - amount);
        return true;
    }

    public List<Map.Entry<UUID, Double>> topBalances(int limit) {
        return data.balances.entrySet().stream()
                .map(e -> Map.entry(UUID.fromString(e.getKey()), e.getValue()))
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
