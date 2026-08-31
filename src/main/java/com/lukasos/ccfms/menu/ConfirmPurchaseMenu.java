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
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfirmPurchaseMenu extends AbstractContainerMenu {
    private static final int[] QUANTITY_SLOTS = {10, 11, 12, 13, 14};
    private static final int[] QUANTITIES = {1, 8, 16, 32, 64};
    private static final int CANCEL_SLOT = 16;

    private final SimpleContainer container = new SimpleContainer(27);
    private final Map<Integer, Integer> quantityBySlot = new HashMap<>();
    private final ShopEntry entry;
    private final ShopCategory category;

    public ConfirmPurchaseMenu(int containerId, Inventory playerInventory, ShopEntry entry, ShopCategory category) {
        super(MenuType.GENERIC_9x3, containerId);
        this.entry = entry;
        this.category = category;

        for (int i = 0; i < QUANTITY_SLOTS.length; i++) {
            int slot = QUANTITY_SLOTS[i];
            int quantity = QUANTITIES[i];
            container.setItem(slot, buildQuantityItem(quantity));
            quantityBySlot.put(slot, quantity);
        }

        ItemStack cancel = new ItemStack(Items.BARRIER);
        cancel.set(DataComponents.CUSTOM_NAME, Component.literal("Cancel").withStyle(ChatFormatting.RED));
        container.setItem(CANCEL_SLOT, cancel);

        ItemStack filler = new ItemStack(ShopFiller.ITEM);
        filler.set(DataComponents.CUSTOM_NAME, Component.literal(" "));
        for (int i = 0; i < 27; i++) {
            if (container.getItem(i).isEmpty()) {
                container.setItem(i, filler);
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new ConfirmSlot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
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

    private ItemStack buildQuantityItem(int quantity) {
        ItemStack stack = new ItemStack(entry.item, Math.min(quantity, 64));
        double total = entry.buyPrice * quantity;
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Buy " + quantity + "x").withStyle(ChatFormatting.GREEN));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Total: ").withStyle(ChatFormatting.WHITE)
                        .append(Component.literal(String.format("$%.2f", total)).withStyle(ChatFormatting.GREEN)),
                Component.literal("Click to confirm").withStyle(ChatFormatting.GRAY)
        )));
        return stack;
    }

    @Override
    public void clicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player) {
        if (slotIndex < 0 || slotIndex >= 27) {
            super.clicked(slotIndex, buttonNum, containerInput, player);
            return;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (slotIndex == CANCEL_SLOT) {
            reopenCategory(serverPlayer);
            return;
        }

        Integer quantity = quantityBySlot.get(slotIndex);
        if (quantity == null) {
            return;
        }

        double total = entry.buyPrice * quantity;
        boolean success = CcfmsMod.economyManager.removeBalance(serverPlayer.getUUID(), total);
        if (success) {
            ItemStack toGive = new ItemStack(entry.item, quantity);
            if (!serverPlayer.getInventory().add(toGive)) {
                serverPlayer.drop(toGive, false);
            }
            serverPlayer.sendSystemMessage(Component.literal(String.format("Bought %dx %s for $%.2f.",
                    quantity, toGive.getHoverName().getString(), total)));
        } else {
            serverPlayer.sendSystemMessage(Component.literal(String.format("You don't have enough money. This costs $%.2f.", total)));
        }

        reopenCategory(serverPlayer);
    }

    private void reopenCategory(ServerPlayer player) {
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("Shop: " + category.name);
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

    private static class ConfirmSlot extends Slot {
        ConfirmSlot(Container container, int slotIndex, int x, int y) {
            super(container, slotIndex, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
  }
