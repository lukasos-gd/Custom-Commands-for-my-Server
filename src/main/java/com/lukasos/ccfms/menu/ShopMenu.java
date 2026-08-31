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

public class ShopMenu extends AbstractContainerMenu {
    private static final int BACK_SLOT = 22;

    private final SimpleContainer shopContainer = new SimpleContainer(27);
    private final Map<Integer, ShopEntry> catalogBySlot = new HashMap<>();
    private final ShopCategory category;

    public ShopMenu(int containerId, Inventory playerInventory, ShopCategory category) {
        super(MenuType.GENERIC_9x3, containerId);
        this.category = category;

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
        ChatFormatting tier = ShopTier.colorFor(entry.buyPrice);
        stack.set(DataComponents.CUSTOM_NAME, stack.getHoverName().copy().withStyle(tier));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("Left-click to choose quantity").withStyle(ChatFormatting.GREEN),
                Component.literal("Right-click to sell 1").withStyle(ChatFormatting.YELLOW),
                Component.literal("Shift-right-click to sell as much as possible").withStyle(ChatFormatting.GRAY),
                Component.literal(""),
                Component.literal("Buy: ").withStyle(ChatFormatting.WHITE)
                        .append(Component.literal(String.format("$%.2f", entry.buyPrice)).withStyle(ChatFormatting.GREEN)),
                Component.literal("Sell: ").withStyle(ChatFormatting.WHITE)
                        .append(Component.literal(String.format("$%.2f", entry.sellPrice)).withStyle(ChatFormatting.RED))
        )));
        return stack;
    }

    private ItemStack buildBackButton() {
        ItemStack back = new ItemStack(Items.ARROW);
        back.set(DataComponents.CUSTOM_NAME, Component.literal("Back to Categories").withStyle(ChatFormatting.RED));
        return back;
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

        if (slotIndex == BACK_SLOT) {
            serverPlayer.openMenu(new MenuProvider() {
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

        boolean rightClick = buttonNum == 1;
        boolean shiftHeld = containerInput == ContainerInput.QUICK_MOVE;

        if (rightClick && shiftHeld) {
            sellFromInventory(serverPlayer, entry, true);
        } else if (rightClick) {
            sellFromInventory(serverPlayer, entry, false);
        } else if (!shiftHeld) {
            serverPlayer.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("Confirm Purchase");
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory inv, Player p) {
                    return new ConfirmPurchaseMenu(containerId, inv, entry, category);
                }
            });
        }
    }

    private void sellFromInventory(ServerPlayer player, ShopEntry entry, boolean sellMax) {
        Container inv = player.getInventory();
        int toSell = sellMax ? countMatching(inv, entry) : 1;
        if (toSell <= 0) {
            player.sendSystemMessage(Component.literal("You don't have any " + new ItemStack(entry.item).getHoverName().getString() + " to sell."));
            return;
        }

        int sold = removeMatching(inv, entry, toSell);
        if (sold <= 0) {
            player.sendSystemMessage(Component.literal("You don't have any " + new ItemStack(entry.item).getHoverName().getString() + " to sell."));
            return;
        }

        double total = entry.sellPrice * sold;
        CcfmsMod.economyManager.addBalance(player.getUUID(), total);
        player.sendSystemMessage(Component.literal(String.format("Sold %dx %s for $%.2f.",
                sold, new ItemStack(entry.item).getHoverName().getString(), total)));
    }

    private int countMatching(Container inv, ShopEntry entry) {
        int total = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == entry.item) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private int removeMatching(Container inv, ShopEntry entry, int amount) {
        int remaining = amount;
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty() || stack.getItem() != entry.item) {
                continue;
            }
            int take = Math.min(remaining, stack.getCount());
            stack.shrink(take);
            inv.setItem(i, stack);
            remaining -= take;
        }
        return amount - remaining;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private static class ShopSlot extends Slot {
        ShopSlot(Container container, int slotIndex, int x, int y) {
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
