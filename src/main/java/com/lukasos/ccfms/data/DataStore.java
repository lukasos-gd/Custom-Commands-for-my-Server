package com.lukasos.ccfms.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DataStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final LevelResource DATA_FOLDER = new LevelResource("ccfms-data");

    private DataStore() {}

    public static Path dataDir(MinecraftServer server) {
        Path dir = server.getWorldPath(DATA_FOLDER);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create ccfms-data directory", e);
        }
        return dir;
    }

    public static <T> T load(MinecraftServer server, String fileName, Type type, T fallback) {
        Path file = dataDir(server).resolve(fileName);
        if (!Files.exists(file)) {
            return fallback;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            T result = GSON.fromJson(reader, type);
            return result != null ? result : fallback;
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to load " + fileName + ": " + e.getMessage());
            return fallback;
        }
    }

    public static void save(MinecraftServer server, String fileName, Object data) {
        Path file = dataDir(server).resolve(fileName);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("[ccfms] Failed to save " + fileName + ": " + e.getMessage());
        }
    }
}
