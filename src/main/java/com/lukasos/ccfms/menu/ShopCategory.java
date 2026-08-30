package com.lukasos.ccfms.menu;

import net.minecraft.world.item.Item;

import java.util.List;

public class ShopCategory {
    public final String name;
    public final Item icon;
    public final List<ShopEntry> entries;

    public ShopCategory(String name, Item icon, List<ShopEntry> entries) {
        this.name = name;
        this.icon = icon;
        this.entries = entries;
    }
}
