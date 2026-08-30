package com.lukasos.ccfms.menu;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopHubMenu extends AbstractContainerMenu {
    private static final int[] CATEGORY_SLOTS = {11, 13, 15, 17};

    private final SimpleContainer container = new SimpleContainer(27);
    private final Map<Integer, ShopCategory> categoryBySlot = new HashMap<>();

    public ShopHubMenu(int containerId, Inventory playerInventory) {
        super(MenuType.GENERIC_9x3, containerId);

        List<ShopCategory> categories = ShopCatalog.CATEGORIES;
        for (int i = 0; i < categories.size() && i < CATEGORY_SLOTS.length; i++) {
            ShopCategory category = categories.get(i);
            int slot = CATEGORY_SLOTS[i];
            container.setItem(slot, buildCategoryIcon(category));
            categoryBySlot.put(slot, category);
        }

        ItemStack filler = new ItemStack(Items.BARRIER);
        filler.set(DataComponents.CUSTOM_NAME, Component.literal(" "));
        for (int i = 0; i < 27; i++) {
            if (container.getItem(i).isEmpty()) {
                container.setItem(i, filler);
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new HubSlot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    private ItemStack buildCategoryIcon(ShopCategory category) {
        ItemStack stack = new ItemStack(category.icon);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(category.name).withStyle(ChatFormatting.GOLD));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(category.entries.size() + " items available").withStyle(ChatFormatting.GRAY),
                Component.literal("Click to browse").withStyle(ChatFormatting.GRAY)
        )));
        return stack;
    }

    private void handleSlotAction(int slotIndex, ServerPlayer player) {
        ShopCategory category = categoryBySlot.get(slotIndex);
        if (category == null) {
            return;
        }
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal(category.name);
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inv, Player p) {
                return new ShopMenu(containerId, inv, category);
            }
        });
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private class HubSlot extends Slot {
        private final int slotIndex;

        HubSlot(Container container, int slotIndex, int x, int y) {
            super(container, slotIndex, x, y);
            this.slotIndex = slotIndex;
        }

        @Override
        public boolean mayPickup(Player player) {
            if (player instanceof ServerPlayer serverPlayer) {
                handleSlotAction(slotIndex, serverPlayer);
            }
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
