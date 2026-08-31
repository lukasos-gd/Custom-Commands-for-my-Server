package com.lukasos.ccfms.menu;

import net.minecraft.world.item.Item;

public class ShopEntry {
    public final int slot;
    public final Item item;
    public final double buyPrice;
    public final double sellPrice;

    public ShopEntry(int slot, Item item, double buyPrice) {
        this(slot, item, buyPrice, buyPrice * 0.5);
    }

    public ShopEntry(int slot, Item item, double buyPrice, double sellPrice) {
        this.slot = slot;
        this.item = item;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }
}
