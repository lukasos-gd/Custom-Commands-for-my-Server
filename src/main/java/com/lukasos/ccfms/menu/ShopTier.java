package com.lukasos.ccfms.menu;

import net.minecraft.ChatFormatting;

public class ShopTier {
    public static ChatFormatting colorFor(double price) {
        if (price < 5) return ChatFormatting.WHITE;
        if (price < 20) return ChatFormatting.YELLOW;
        if (price < 75) return ChatFormatting.GOLD;
        return ChatFormatting.LIGHT_PURPLE;
    }
}
