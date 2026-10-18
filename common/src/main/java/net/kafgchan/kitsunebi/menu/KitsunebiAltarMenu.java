package net.kafgchan.kitsunebi.menu;

import net.kafgchan.kitsunebi.block.altar.AltarCraftingEngine;
import net.kafgchan.kitsunebi.registry.ModBlocks;
import net.kafgchan.kitsunebi.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/**
 * 狐火の祭壇コンテナメニュー
 */
public class KitsunebiAltarMenu extends AbstractContainerMenu {
    public static final int INPUT_SLOT_COUNT = 6;
    public static final int RESULT_SLOT = 6;
    public static final int INV_SLOT_START = 7;
    public static final int USE_ROW_SLOT_END = 43;

    private final ContainerLevelAccess access;
    private final Container inputContainer = new SimpleContainer(INPUT_SLOT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            KitsunebiAltarMenu.this.slotsChanged(this);
        }
    };
    private final ResultContainer resultContainer = new ResultContainer();
    private final ContainerData data;

    private int categoryIndex = 0;
    private int selectionIndex = 0;
    private int quantity = 1;

    public KitsunebiAltarMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public KitsunebiAltarMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenus.KITSUNEBI_ALTAR_MENU.get(), containerId);
        this.access = access;
        this.data = new SimpleContainerData(3);
        this.addDataSlots(this.data);

        // 1. 入力素材スロット (3x2 配置)
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new Slot(this.inputContainer, col + row * 3, 20 + col * 18, 20 + row * 18));
            }
        }

        // 2. 出力プレビュー・取り出しスロット
        this.addSlot(new Slot(this.resultContainer, 0, 142, 29) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                AltarCraftingEngine.consumeIngredients(
                        KitsunebiAltarMenu.this.inputContainer,
                        KitsunebiAltarMenu.this.getCategory(),
                        KitsunebiAltarMenu.this.selectionIndex,
                        KitsunebiAltarMenu.this.quantity
                );
                stack.getItem().onCraftedBy(stack, player.level(), player);
                KitsunebiAltarMenu.this.setupResultSlot();
                super.onTake(player, stack);
            }
        });

        // 3. プレイヤーインベントリ
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 102 + row * 18));
            }
        }

        // 4. プレイヤーホットバー
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 160));
        }

        this.data.set(0, 0); // Category
        this.data.set(1, 0); // Selection
        this.data.set(2, 1); // Quantity
    }

    public AltarCraftingEngine.Category getCategory() {
        return AltarCraftingEngine.Category.values()[Math.max(0, Math.min(AltarCraftingEngine.Category.values().length - 1, this.categoryIndex))];
    }

    public int getCategoryIndex() {
        return this.categoryIndex;
    }

    public int getSelectionIndex() {
        return this.selectionIndex;
    }

    public int getQuantity() {
        return this.quantity;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id <= 2) {
            // カテゴリ変更
            this.categoryIndex = id;
            this.selectionIndex = 0;
            this.data.set(0, this.categoryIndex);
            this.data.set(1, this.selectionIndex);
            this.setupResultSlot();
            return true;
        } else if (id >= 100 && id < 200) {
            // アイテム選択変更
            this.selectionIndex = id - 100;
            this.data.set(1, this.selectionIndex);
            this.setupResultSlot();
            return true;
        } else if (id >= 1000 && id <= 1064) {
            // 数量変更 (1〜64)
            this.quantity = id - 1000;
            this.data.set(2, this.quantity);
            this.setupResultSlot();
            return true;
        }
        return false;
    }

    @Override
    public void slotsChanged(Container container) {
        this.setupResultSlot();
    }

    public void setupResultSlot() {
        AltarCraftingEngine.RecipeResult result = AltarCraftingEngine.calculate(
                this.inputContainer,
                this.getCategory(),
                this.selectionIndex,
                this.quantity
        );
        this.resultContainer.setItem(0, result.canCraft() ? result.result() : ItemStack.EMPTY);
        this.broadcastChanges();
    }

    public AltarCraftingEngine.RecipeResult getCurrentResult() {
        return AltarCraftingEngine.calculate(
                this.inputContainer,
                this.getCategory(),
                this.selectionIndex,
                this.quantity
        );
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.KITSUNEBI_ALTAR.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index == RESULT_SLOT) {
                // 完成品スロットからの取り出し
                if (!this.moveItemStackTo(slotStack, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, itemstack);
            } else if (index >= 0 && index < INPUT_SLOT_COUNT) {
                // 入力スロットからプレイヤーインベントリへ
                if (!this.moveItemStackTo(slotStack, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // プレイヤーインベントリから入力スロットへ
                if (!this.moveItemStackTo(slotStack, 0, INPUT_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }
        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.inputContainer));
    }
}
