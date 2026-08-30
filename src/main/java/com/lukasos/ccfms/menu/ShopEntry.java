package com.lukasos.ccfms.menu;

import net.minecraft.world.item.Item;

public class ShopEntry {
    public final int slot;
    public final Item item;
    public final double price;

    public ShopEntry(int slot, Item item, double price) {
        this.slot = slot;
        this.item = item;
        this.price = price;
    }
}
