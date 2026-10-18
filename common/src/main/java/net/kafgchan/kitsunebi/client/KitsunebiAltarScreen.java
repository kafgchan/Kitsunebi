package net.kafgchan.kitsunebi.client;

import net.kafgchan.kitsunebi.block.altar.AltarCraftingEngine;
import net.kafgchan.kitsunebi.menu.KitsunebiAltarMenu;
import net.kafgchan.kitsunebi.registry.ModBlocks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 狐火の祭壇 GUI スクリーン
 */
public class KitsunebiAltarScreen extends AbstractContainerScreen<KitsunebiAltarMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png");

    private static final int[] PRESETS = {1, 2, 4, 8, 16, 32, 64};
    private final List<Button> selectionButtons = new ArrayList<>();
    private int quantity = 1;

    public KitsunebiAltarScreen(KitsunebiAltarMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 200;
        this.imageHeight = 184;
        this.inventoryLabelY = 90;
    }

    @Override
    protected void init() {
        super.init();
        this.quantity = this.menu.getQuantity();
        rebuildInterface();
    }

    private void rebuildInterface() {
        this.clearWidgets();
        this.selectionButtons.clear();

        int left = this.leftPos;
        int top = this.topPos;

        // 1. カテゴリ切替ボタン (3種)
        this.addRenderableWidget(Button.builder(Component.translatable("category.kitsunebi.fire"), b -> selectCategory(0))
                .bounds(left + 8, top - 18, 56, 16).build());
        this.addRenderableWidget(Button.builder(Component.translatable("category.kitsunebi.block"), b -> selectCategory(1))
                .bounds(left + 68, top - 18, 56, 16).build());
        this.addRenderableWidget(Button.builder(Component.translatable("category.kitsunebi.phantom"), b -> selectCategory(2))
                .bounds(left + 128, top - 18, 64, 16).build());

        // 2. 数量プリセットボタン (1, 2, 4, 8, 16, 32, 64)
        int px = left + 8;
        int py = top + 64;
        for (int p : PRESETS) {
            final int count = p;
            this.addRenderableWidget(Button.builder(Component.literal(String.valueOf(p)), b -> setQuantity(count))
                    .bounds(px, py, 18, 14).build());
            px += 20;
        }

        // 3. 数量微調整ボタン (-10, -1, +1, +10)
        this.addRenderableWidget(Button.builder(Component.literal("-10"), b -> setQuantity(Math.max(1, this.quantity - 10)))
                .bounds(px + 4, py, 22, 14).build());
        this.addRenderableWidget(Button.builder(Component.literal("+10"), b -> setQuantity(Math.min(64, this.quantity + 10)))
                .bounds(px + 28, py, 22, 14).build());

        // 4. カテゴリ内の各アイテム選択ボタン
        AltarCraftingEngine.Category cat = this.menu.getCategory();
        if (cat == AltarCraftingEngine.Category.KITSUNEBI) {
            String[] colors = ModBlocks.COLORS;
            for (int i = 0; i < colors.length; i++) {
                final int idx = i;
                Button btn = Button.builder(Component.literal(colors[i].substring(0, 1).toUpperCase()), b -> selectItem(idx))
                        .bounds(left + 82 + (i % 4) * 16, top + 18 + (i / 4) * 16, 15, 15).build();
                this.selectionButtons.add(btn);
                this.addRenderableWidget(btn);
            }
        } else if (cat == AltarCraftingEngine.Category.PHANTOM) {
            for (int i = 0; i < 17; i++) {
                final int idx = i;
                int bx = left + 82 + (i % 6) * 16;
                int by = top + 14 + (i / 6) * 16;
                Button btn = Button.builder(Component.literal(String.valueOf(i + 1)), b -> selectItem(idx))
                        .bounds(bx, by, 15, 14).build();
                this.selectionButtons.add(btn);
                this.addRenderableWidget(btn);
            }
        }
    }

    private void selectCategory(int catIndex) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, catIndex);
            this.rebuildInterface();
        }
    }

    private void selectItem(int itemIndex) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 100 + itemIndex);
        }
    }

    private void setQuantity(int q) {
        this.quantity = Math.max(1, Math.min(64, q));
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1000 + this.quantity);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // 背景描画（スロット枠とパネル）
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFFC6C6C6);
        guiGraphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, 0xFFE0E0E0);

        // 入力スロット枠 (3x2)
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 3; c++) {
                guiGraphics.fill(x + 19 + c * 18, y + 19 + r * 18, x + 37 + c * 18, y + 37 + r * 18, 0xFF8B8B8B);
                guiGraphics.fill(x + 20 + c * 18, y + 20 + r * 18, x + 36 + c * 18, y + 36 + r * 18, 0xFF373737);
            }
        }

        // 出力スロット枠 (強調枠)
        guiGraphics.fill(x + 140, y + 27, x + 160, y + 47, 0xFFFFD700);
        guiGraphics.fill(x + 141, y + 28, x + 159, y + 46, 0xFF373737);

        // プレイヤーインベントリ枠
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                guiGraphics.fill(x + 7 + c * 18, y + 101 + r * 18, x + 25 + c * 18, y + 119 + r * 18, 0xFF8B8B8B);
                guiGraphics.fill(x + 8 + c * 18, y + 102 + r * 18, x + 24 + c * 18, y + 118 + r * 18, 0xFF373737);
            }
        }
        for (int c = 0; c < 9; c++) {
            guiGraphics.fill(x + 7 + c * 18, y + 159, x + 25 + c * 18, y + 177, 0xFF8B8B8B);
            guiGraphics.fill(x + 8 + c * 18, y + 160, x + 24 + c * 18, y + 176, 0xFF373737);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);
        guiGraphics.drawString(this.font, Component.literal("x" + this.quantity), 143, 14, 0x1E3A8A, false);

        // 必要素材と現在の投入数のプレビュー描画
        AltarCraftingEngine.RecipeResult res = this.menu.getCurrentResult();
        int reqY = 80;
        int maxItemsToShow = Math.min(2, res.ingredients().size());
        for (int i = 0; i < maxItemsToShow; i++) {
            var ing = res.ingredients().get(i);
            int color = ing.isSatisfied() ? 0x008000 : 0xB91C1C;
            String text = ing.label() + ": " + ing.providedCount() + "/" + ing.requiredCount();
            guiGraphics.drawString(this.font, Component.literal(text), 8 + i * 95, reqY, color, false);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
