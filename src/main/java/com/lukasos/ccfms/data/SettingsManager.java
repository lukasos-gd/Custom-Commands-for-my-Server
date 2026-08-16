package com.lukasos.ccfms.data;

import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SettingsManager {
    private static final String FILE = "settings.json";
    private static final Type TYPE = new TypeToken<Map<String, PlayerSettings>>() {}.getType();

    private final MinecraftServer server;
    private final Map<String, PlayerSettings> data;

    public SettingsManager(MinecraftServer server) {
        this.server = server;
        this.data = DataStore.load(server, FILE, TYPE, new HashMap<>());
    }

    public PlayerSettings get(UUID player) {
        return data.computeIfAbsent(player.toString(), k -> new PlayerSettings());
    }

    public void save() {
        DataStore.save(server, FILE, data);
    }
}
