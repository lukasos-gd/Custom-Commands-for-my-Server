package com.lukasos.ccfms.data;

import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeManager {
    private static final String FILE = "homes.json";
    private static final Type TYPE = new TypeToken<Map<String, Map<String, HomeLocation>>>() {}.getType();
    public static final String DEFAULT_NAME = "home";
    public static final int MAX_HOMES = 10;

    private final MinecraftServer server;
    private final Map<String, Map<String, HomeLocation>> data;

    public HomeManager(MinecraftServer server) {
        this.server = server;
        this.data = DataStore.load(server, FILE, TYPE, new HashMap<>());
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

    private void save() {
        DataStore.save(server, FILE, data);
    }
}
