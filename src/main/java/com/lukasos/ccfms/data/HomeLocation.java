package com.lukasos.ccfms.data;

public class HomeLocation {
    public String dimension;
    public double x, y, z;
    public float yaw, pitch;

    public HomeLocation() {}

    public HomeLocation(String dimension, double x, double y, double z, float yaw, float pitch) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
