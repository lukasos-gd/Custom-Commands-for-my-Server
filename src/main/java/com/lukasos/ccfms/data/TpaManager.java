package com.lukasos.ccfms.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TpaManager {
    public static final long EXPIRY_MS = 60_000;

    public enum RequestType { NORMAL, HERE }

    public static class Request {
        public final UUID from;
        public final long timestamp;
        public final RequestType type;

        public Request(UUID from, long timestamp, RequestType type) {
            this.from = from;
            this.timestamp = timestamp;
            this.type = type;
        }
    }

    private final Map<UUID, Request> pending = new HashMap<>();

    public void addRequest(UUID from, UUID to, RequestType type) {
        pending.put(to, new Request(from, System.currentTimeMillis(), type));
    }

    public Request getRequest(UUID to) {
        Request req = pending.get(to);
        if (req == null) return null;
        if (System.currentTimeMillis() - req.timestamp > EXPIRY_MS) {
            pending.remove(to);
            return null;
        }
        return req;
    }

    public void clear(UUID to) {
        pending.remove(to);
    }

    public boolean cancelSentBy(UUID from) {
        for (Map.Entry<UUID, Request> e : pending.entrySet()) {
            if (e.getValue().from.equals(from)) {
                pending.remove(e.getKey());
                return true;
            }
        }
        return false;
    }
}
