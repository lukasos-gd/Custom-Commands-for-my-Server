package com.lukasos.ccfms.menu;

import net.minecraft.world.item.Items;

import java.util.List;

public class ShopCatalog {
    public static final List<ShopCategory> CATEGORIES = List.of(
            new ShopCategory("Blocks", Items.COBBLESTONE, List.of(
                    new ShopEntry(10, Items.COBBLESTONE, 1.0),
                    new ShopEntry(11, Items.STONE, 1.5),
                    new ShopEntry(12, Items.OAK_LOG, 3.0),
                    new ShopEntry(13, Items.SAND, 1.0),
                    new ShopEntry(14, Items.GLASS, 2.0),
                    new ShopEntry(15, Items.DIRT, 0.5)
            )),
            new ShopCategory("Ores & Ingots", Items.IRON_INGOT, List.of(
                    new ShopEntry(10, Items.COAL, 2.0),
                    new ShopEntry(11, Items.IRON_INGOT, 15.0),
                    new ShopEntry(12, Items.GOLD_INGOT, 25.0),
                    new ShopEntry(13, Items.DIAMOND, 100.0),
                    new ShopEntry(14, Items.EMERALD, 40.0),
                    new ShopEntry(15, Items.REDSTONE, 3.0),
                    new ShopEntry(16, Items.LAPIS_LAZULI, 3.0)
            )),
            new ShopCategory("Food", Items.BREAD, List.of(
                    new ShopEntry(10, Items.BREAD, 2.0),
                    new ShopEntry(11, Items.APPLE, 2.0),
                    new ShopEntry(12, Items.COOKED_BEEF, 4.0),
                    new ShopEntry(13, Items.GOLDEN_APPLE, 60.0)
            )),
            new ShopCategory("Combat", Items.IRON_SWORD, List.of(
                    new ShopEntry(10, Items.ARROW, 0.5),
                    new ShopEntry(11, Items.IRON_SWORD, 30.0),
                    new ShopEntry(12, Items.BOW, 25.0),
                    new ShopEntry(13, Items.SHIELD, 20.0)
            ))
    );
}
