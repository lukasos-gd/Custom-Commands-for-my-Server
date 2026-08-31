package com.lukasos.ccfms.menu;

import net.minecraft.world.item.Item;

import java.util.List;

public class ShopCategory {
    public final String name;
    public final String description;
    public final Item icon;
    public final List<ShopEntry> entries;

    public ShopCategory(String name, String description, Item icon, List<ShopEntry> entries) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.entries = entries;
    }
}
