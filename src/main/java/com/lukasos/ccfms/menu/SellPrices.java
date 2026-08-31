package com.lukasos.ccfms.menu;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SellPrices {
    private static final double DEFAULT_PRICE = 0.1;
    private static final Map<Item, Double> SPECIAL_PRICES = new HashMap<>();
    private static final Set<Item> EXCLUDED = new HashSet<>();

    static {
        SPECIAL_PRICES.put(Items.DIRT, 0.05);
        SPECIAL_PRICES.put(Items.COBBLESTONE, 0.1);
        SPECIAL_PRICES.put(Items.STONE, 0.1);
        SPECIAL_PRICES.put(Items.GRAVEL, 0.1);
        SPECIAL_PRICES.put(Items.SAND, 0.1);
        SPECIAL_PRICES.put(Items.OAK_LOG, 0.5);
        SPECIAL_PRICES.put(Items.ROTTEN_FLESH, 0.05);
        SPECIAL_PRICES.put(Items.STRING, 0.2);
        SPECIAL_PRICES.put(Items.SPIDER_EYE, 0.3);
        SPECIAL_PRICES.put(Items.BONE, 0.3);
        SPECIAL_PRICES.put(Items.GUNPOWDER, 0.5);
        SPECIAL_PRICES.put(Items.WHEAT, 0.3);
        SPECIAL_PRICES.put(Items.WHEAT_SEEDS, 0.1);
        SPECIAL_PRICES.put(Items.COAL, 1.0);
        SPECIAL_PRICES.put(Items.REDSTONE, 0.5);
        SPECIAL_PRICES.put(Items.LAPIS_LAZULI, 0.5);
        SPECIAL_PRICES.put(Items.IRON_INGOT, 5.0);
        SPECIAL_PRICES.put(Items.IRON_NUGGET, 0.6);
        SPECIAL_PRICES.put(Items.COPPER_INGOT, 3.0);
        SPECIAL_PRICES.put(Items.GOLD_INGOT, 8.0);
        SPECIAL_PRICES.put(Items.GOLD_NUGGET, 1.0);
        SPECIAL_PRICES.put(Items.EMERALD, 10.0);
        SPECIAL_PRICES.put(Items.DIAMOND, 50.0);
        SPECIAL_PRICES.put(Items.NETHERITE_SCRAP, 120.0);
        SPECIAL_PRICES.put(Items.NETHERITE_INGOT, 200.0);
        SPECIAL_PRICES.put(Items.AMETHYST_SHARD, 3.0);
        SPECIAL_PRICES.put(Items.QUARTZ, 2.0);
        SPECIAL_PRICES.put(Items.ENDER_PEARL, 8.0);
        SPECIAL_PRICES.put(Items.BLAZE_ROD, 10.0);
        SPECIAL_PRICES.put(Items.GHAST_TEAR, 15.0);
        SPECIAL_PRICES.put(Items.NETHER_STAR, 500.0);
        SPECIAL_PRICES.put(Items.ELYTRA, 300.0);
        SPECIAL_PRICES.put(Items.TOTEM_OF_UNDYING, 250.0);
        SPECIAL_PRICES.put(Items.DRAGON_EGG, 1000.0);
        SPECIAL_PRICES.put(Items.SHULKER_SHELL, 30.0);

        EXCLUDED.add(Items.BARRIER);
        EXCLUDED.add(Items.STRUCTURE_VOID);
        EXCLUDED.add(Items.JIGSAW);
        EXCLUDED.add(Items.COMMAND_BLOCK);
        EXCLUDED.add(Items.CHAIN_COMMAND_BLOCK);
        EXCLUDED.add(Items.REPEATING_COMMAND_BLOCK);
        EXCLUDED.add(Items.COMMAND_BLOCK_MINECART);
        EXCLUDED.add(Items.STRUCTURE_BLOCK);
        EXCLUDED.add(Items.DEBUG_STICK);
        EXCLUDED.add(Items.KNOWLEDGE_BOOK);
        EXCLUDED.add(Items.LIGHT);
        EXCLUDED.add(Items.BEDROCK);
        EXCLUDED.add(Items.SPAWNER);
        EXCLUDED.add(Items.AIR);
    }

    public static Double get(Item item) {
        if (item == null || item == Items.AIR || EXCLUDED.contains(item)) {
            return null;
        }
        Double special = SPECIAL_PRICES.get(item);
        if (special != null) {
            return special;
        }
        return DEFAULT_PRICE;
    }
}
