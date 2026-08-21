package com.lukasos.ccfms.data;

import java.util.HashMap;
import java.util.Map;

public class OffendData {
    public Map<String, BanRecord> bans = new HashMap<>();
    public String appealInfo = "No appeal information configured. Contact a server admin.";
}
