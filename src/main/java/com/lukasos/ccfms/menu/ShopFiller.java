package com.lukasos.ccfms.menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ShopFiller {
    public static final Item ITEM = resolve();

    private static Item resolve() {
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minecraft", "gray_stained_glass_pane"));
        return item != null ? item : Items.BARRIER;
    }
}
