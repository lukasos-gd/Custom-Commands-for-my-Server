package com.lukasos.ccfms.menu;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class SellPrices {
    private static final Map<Item, Double> PRICES = new HashMap<>();

    static {
        PRICES.put(Items.COBBLESTONE, 0.1);
        PRICES.put(Items.DIRT, 0.05);
        PRICES.put(Items.OAK_LOG, 0.5);
        PRICES.put(Items.STONE, 0.1);
        PRICES.put(Items.IRON_INGOT, 5.0);
        PRICES.put(Items.GOLD_INGOT, 8.0);
        PRICES.put(Items.DIAMOND, 50.0);
        PRICES.put(Items.EMERALD, 10.0);
        PRICES.put(Items.WHEAT, 0.3);
        PRICES.put(Items.ROTTEN_FLESH, 0.05);
        PRICES.put(Items.COAL, 1.0);
        PRICES.put(Items.REDSTONE, 0.5);
        PRICES.put(Items.LAPIS_LAZULI, 0.5);
    }

    public static Double get(Item item) {
        return PRICES.get(item);
    }
}
