package net.kafgchan.kitsunebi.item.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 狐火のシャベル
 * - 最大耐久: 1,048,576
 * - 超高速掘削、面掘削（単体 / 3x3 / 5x5）
 */
public class KitsunebiShovelItem extends ShovelItem implements IKitsunebiTool {

    public KitsunebiShovelItem(Properties properties) {
        super(ModToolTiers.KITSUNEBI, 1.5F, -3.0F, properties
                .durability(ModToolTiers.KITSUNEBI.getUses())
                .fireResistant()
                .rarity(Rarity.EPIC));
    }

    @Override
    public int getModeCount() {
        return 3;
    }

    @Override
    public String getModeTranslationKey(int mode) {
        return switch (mode) {
            case 1 -> "kitsunebi.skill.shovel.area_3x3";
            case 2 -> "kitsunebi.skill.shovel.area_5x5";
            default -> "kitsunebi.skill.shovel.single";
        };
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean result = super.mineBlock(stack, level, state, pos, miner);
        if (!level.isClientSide && miner instanceof ServerPlayer player && ToolStateHelper.isActive(stack)) {
            int mode = ToolStateHelper.getMode(stack);
            ServerLevel serverLevel = (ServerLevel) level;
            switch (mode) {
                case 1 -> MiningHelper.breakArea(serverLevel, player, pos, 1, stack);
                case 2 -> MiningHelper.breakArea(serverLevel, player, pos, 2, stack);
            }
        }
        return result;
    }

    @Override
    public void executeSkill(ItemStack stack, ServerPlayer player) {
        int mode = ToolStateHelper.getMode(stack);
        BlockPos origin = player.blockPosition().relative(player.getDirection());
        ServerLevel level = player.serverLevel();
        if (mode == 2) {
            MiningHelper.breakArea(level, player, origin, 2, stack);
        } else if (mode == 1) {
            MiningHelper.breakArea(level, player, origin, 1, stack);
        }
    }
}
