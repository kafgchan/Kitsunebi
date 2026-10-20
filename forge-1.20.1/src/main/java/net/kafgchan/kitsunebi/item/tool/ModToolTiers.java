package net.kafgchan.kitsunebi.item.tool;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/**
 * 狐火ツール用ティア定義
 * 最大耐久 1,048,576、超高速採掘、金床修理不可（祭壇修理のみ）
 */
public enum ModToolTiers implements Tier {
    KITSUNEBI(
            1_048_576,
            100.0F,
            9999.0F,
            BlockTags.NEEDS_DIAMOND_TOOL,
            25,
            Ingredient.EMPTY
    );

    private final int uses;
    private final float speed;
    private final float attackDamageBonus;
    private final TagKey<Block> incorrectBlocksForDrops;
    private final int enchantmentValue;
    private final Ingredient repairIngredient;

    ModToolTiers(int uses, float speed, float attackDamageBonus, TagKey<Block> incorrectBlocksForDrops, int enchantmentValue, Ingredient repairIngredient) {
        this.uses = uses;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.incorrectBlocksForDrops = incorrectBlocksForDrops;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getLevel(){return 4;}
    public int getUses() {
        return this.uses;
    }

    @Override
    public float getSpeed() {
        return this.speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return this.attackDamageBonus;
    }

    public TagKey<Block> getIncorrectBlocksForDrops() {
        return this.incorrectBlocksForDrops;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient;
    }
}
