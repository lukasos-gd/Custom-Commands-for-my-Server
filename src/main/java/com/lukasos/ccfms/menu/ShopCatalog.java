package com.lukasos.ccfms.menu;

import net.minecraft.world.item.Items;

import java.util.List;

public class ShopCatalog {
    public static final List<ShopCategory> CATEGORIES = List.of(
            new ShopCategory("Blocks", "Essential construction materials for your next project.", Items.COBBLESTONE, List.of(
                    new ShopEntry(9, Items.COBBLESTONE, 1.0),
                    new ShopEntry(10, Items.STONE, 1.5),
                    new ShopEntry(11, Items.OAK_LOG, 3.0),
                    new ShopEntry(12, Items.SAND, 1.0),
                    new ShopEntry(13, Items.GLASS, 2.0),
                    new ShopEntry(14, Items.DIRT, 0.5),
                    new ShopEntry(15, Items.GRAVEL, 1.0),
                    new ShopEntry(16, Items.ANDESITE, 1.5),
                    new ShopEntry(17, Items.GRANITE, 1.5)
            )),
            new ShopCategory("Ores & Ingots", "Raw ores and refined ingots for crafting and trading.", Items.IRON_INGOT, List.of(
                    new ShopEntry(9, Items.COAL, 2.0),
                    new ShopEntry(10, Items.IRON_INGOT, 15.0),
                    new ShopEntry(11, Items.GOLD_INGOT, 25.0),
                    new ShopEntry(12, Items.DIAMOND, 100.0),
                    new ShopEntry(13, Items.EMERALD, 40.0),
                    new ShopEntry(14, Items.REDSTONE, 3.0),
                    new ShopEntry(15, Items.LAPIS_LAZULI, 3.0),
                    new ShopEntry(16, Items.COPPER_INGOT, 10.0),
                    new ShopEntry(17, Items.NETHERITE_SCRAP, 250.0)
            )),
            new ShopCategory("Farm and Food", "Resources for growing crops, tree farming, and cooked meals.", Items.BREAD, List.of(
                    new ShopEntry(9, Items.BREAD, 2.0),
                    new ShopEntry(10, Items.APPLE, 2.0),
                    new ShopEntry(11, Items.COOKED_BEEF, 4.0),
                    new ShopEntry(12, Items.GOLDEN_APPLE, 60.0),
                    new ShopEntry(13, Items.CARROT, 1.0),
                    new ShopEntry(14, Items.POTATO, 1.0),
                    new ShopEntry(15, Items.COOKED_CHICKEN, 3.0),
                    new ShopEntry(16, Items.MELON_SLICE, 1.0),
                    new ShopEntry(17, Items.PUMPKIN_PIE, 5.0)
            )),
            new ShopCategory("Combat", "Weapons and armor to gain the advantage in any fight.", Items.IRON_SWORD, List.of(
                    new ShopEntry(9, Items.ARROW, 0.5),
                    new ShopEntry(10, Items.IRON_SWORD, 30.0),
                    new ShopEntry(11, Items.DIAMOND_SWORD, 120.0),
                    new ShopEntry(12, Items.BOW, 25.0),
                    new ShopEntry(13, Items.CROSSBOW, 35.0),
                    new ShopEntry(14, Items.SHIELD, 20.0),
                    new ShopEntry(15, Items.IRON_CHESTPLATE, 60.0),
                    new ShopEntry(16, Items.IRON_HELMET, 35.0),
                    new ShopEntry(17, Items.TOTEM_OF_UNDYING, 300.0)
            )),
            new ShopCategory("Wood & Building", "Planks, stairs, doors, and decorative building pieces.", Items.OAK_PLANKS, List.of(
                    new ShopEntry(9, Items.OAK_PLANKS, 0.5),
                    new ShopEntry(10, Items.OAK_STAIRS, 1.0),
                    new ShopEntry(11, Items.OAK_SLAB, 0.5),
                    new ShopEntry(12, Items.OAK_DOOR, 3.0),
                    new ShopEntry(13, Items.LADDER, 1.0),
                    new ShopEntry(14, Items.OAK_FENCE, 1.0),
                    new ShopEntry(15, Items.GLASS_PANE, 1.0),
                    new ShopEntry(16, Items.BRICK, 2.0),
                    new ShopEntry(17, Items.STONE_BRICKS, 1.5)
            )),
            new ShopCategory("Redstone", "Engineering supplies for circuits and automation.", Items.REDSTONE, List.of(
                    new ShopEntry(9, Items.REDSTONE, 3.0),
                    new ShopEntry(10, Items.REPEATER, 5.0),
                    new ShopEntry(11, Items.COMPARATOR, 8.0),
                    new ShopEntry(12, Items.PISTON, 6.0),
                    new ShopEntry(13, Items.STICKY_PISTON, 10.0),
                    new ShopEntry(14, Items.REDSTONE_TORCH, 2.0),
                    new ShopEntry(15, Items.LEVER, 1.0),
                    new ShopEntry(16, Items.REDSTONE_LAMP, 6.0),
                    new ShopEntry(17, Items.OBSERVER, 12.0)
            )),
            new ShopCategory("Farming Supplies", "Seeds, saplings, and tools for growing your own food.", Items.WHEAT, List.of(
                    new ShopEntry(9, Items.WHEAT_SEEDS, 0.5),
                    new ShopEntry(10, Items.OAK_SAPLING, 1.0),
                    new ShopEntry(11, Items.BONE_MEAL, 1.0),
                    new ShopEntry(12, Items.CARROT, 1.0),
                    new ShopEntry(13, Items.POTATO, 1.0),
                    new ShopEntry(14, Items.WHEAT, 1.5),
                    new ShopEntry(15, Items.HAY_BLOCK, 5.0),
                    new ShopEntry(16, Items.PUMPKIN_SEEDS, 0.5),
                    new ShopEntry(17, Items.MELON_SEEDS, 0.5)
            )),
            new ShopCategory("Tools", "Essential tools for mining, chopping, and everyday tasks.", Items.IRON_PICKAXE, List.of(
                    new ShopEntry(9, Items.IRON_PICKAXE, 25.0),
                    new ShopEntry(10, Items.IRON_AXE, 25.0),
                    new ShopEntry(11, Items.IRON_SHOVEL, 15.0),
                    new ShopEntry(12, Items.IRON_HOE, 15.0),
                    new ShopEntry(13, Items.FISHING_ROD, 12.0),
                    new ShopEntry(14, Items.FLINT_AND_STEEL, 8.0),
                    new ShopEntry(15, Items.SHEARS, 10.0),
                    new ShopEntry(16, Items.BUCKET, 5.0),
                    new ShopEntry(17, Items.COMPASS, 6.0)
            ))
    );
}
