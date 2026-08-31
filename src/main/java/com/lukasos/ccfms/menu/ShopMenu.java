package com.lukasos.ccfms.menu;

import com.lukasos.ccfms.CcfmsMod;
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

public class ShopMenu extends AbstractContainerMenu {
    private static final int BACK_SLOT = 22;

    private final SimpleContainer shopContainer = new SimpleContainer(27);
    private final Map<Integer, ShopEntry> catalogBySlot = new HashMap<>();

    public ShopMenu(int containerId, Inventory playerInventory, ShopCategory category) {
        super(MenuType.GENERIC_9x3, containerId);

        for (ShopEntry entry : category.entries) {
            shopContainer.setItem(entry.slot, buildDisplayItem(entry));
            catalogBySlot.put(entry.slot, entry);
        }

        shopContainer.setItem(BACK_SLOT, buildBackButton());

        ItemStack filler = new ItemStack(ShopFiller.ITEM);
        filler.set(DataComponents.CUSTOM_NAME, Component.literal(" "));
        for (int i = 0; i < 27; i++) {
            if (shopContainer.getItem(i).isEmpty()) {
                shopContainer.setItem(i, filler);
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9;
                addSlot(new ShopSlot(shopContainer, slotIndex, 8 + col * 18, 18 + row * 18));
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

    private ItemStack buildDisplayItem(ShopEntry entry) {
        ItemStack stack = new ItemStack(entry.item);
        stack.set(DataComponents.CUSTOM_NAME, stack.getHoverName().copy().withStyle(ChatFormatting.YELLOW));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(String.format("Price: $%.2f", entry.price)).withStyle(ChatFormatting.GOLD),
                Component.literal("Click to buy 1").withStyle(ChatFormatting.GRAY)
        )));
        return stack;
    }

    private ItemStack buildBackButton() {
        ItemStack back = new ItemStack(Items.ARROW);
        back.set(DataComponents.CUSTOM_NAME, Component.literal("Back to Categories").withStyle(ChatFormatting.RED));
        return back;
    }

    private void handleSlotAction(int slotIndex, ServerPlayer player) {
        if (slotIndex == BACK_SLOT) {
            shopContainer.setItem(BACK_SLOT, buildBackButton());
            player.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("Shop");
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory inv, Player p) {
                    return new ShopHubMenu(containerId, inv);
                }
            });
            return;
        }

        ShopEntry entry = catalogBySlot.get(slotIndex);
        if (entry == null) {
            return;
        }

        boolean success = CcfmsMod.economyManager.removeBalance(player.getUUID(), entry.price);
        if (success) {
            ItemStack toGive = new ItemStack(entry.item, 1);
            if (!player.getInventory().add(toGive)) {
                player.drop(toGive, false);
            }
            player.sendSystemMessage(Component.literal(String.format("Bought %s for $%.2f.",
                    toGive.getHoverName().getString(), entry.price)));
        } else {
            player.sendSystemMessage(Component.literal(String.format("You don't have enough money. This costs $%.2f.", entry.price)));
        }

        shopContainer.setItem(slotIndex, buildDisplayItem(entry));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private class ShopSlot extends Slot {
        private final int slotIndex;

        ShopSlot(Container container, int slotIndex, int x, int y) {
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
