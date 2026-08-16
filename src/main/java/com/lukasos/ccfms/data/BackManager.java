package com.lukasos.ccfms.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackManager {
    private final Map<UUID, HomeLocation> lastPos = new HashMap<>();

    public void record(UUID player, HomeLocation loc) {
        lastPos.put(player, loc);
    }

    public HomeLocation get(UUID player) {
        return lastPos.get(player);
    }
}
