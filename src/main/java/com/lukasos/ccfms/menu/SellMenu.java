package com.lukasos.ccfms.menu;

import com.lukasos.ccfms.CcfmsMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SellMenu extends AbstractContainerMenu {
    private final SimpleContainer sellContainer = new SimpleContainer(9);
    private boolean processing = false;

    public SellMenu(int containerId, Inventory playerInventory, ServerPlayer sellingPlayer) {
        super(MenuType.GENERIC_9x1, containerId);

        sellContainer.addListener(c -> onContainerChanged(sellingPlayer));

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(sellContainer, col, 8 + col * 18, 18));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
        }
    }

    private void onContainerChanged(ServerPlayer player) {
        if (processing) return;
        processing = true;
        try {
            for (int i = 0; i < sellContainer.getContainerSize(); i++) {
                ItemStack stack = sellContainer.getItem(i);
                if (stack.isEmpty()) continue;
                Double unitPrice = SellPrices.get(stack.getItem());
                if (unitPrice == null) continue;
                double total = unitPrice * stack.getCount();
                CcfmsMod.economyManager.addBalance(player.getUUID(), total);
                player.sendSystemMessage(Component.literal(String.format("Sold %dx %s for $%.2f",
                        stack.getCount(), stack.getItem().getDescription().getString(), total)));
                sellContainer.setItem(i, ItemStack.EMPTY);
            }
        } finally {
            processing = false;
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            newStack = stackInSlot.copy();
            if (index < 9) {
                if (!this.moveItemStackTo(stackInSlot, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stackInSlot, 0, 9, false)) {
                return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
