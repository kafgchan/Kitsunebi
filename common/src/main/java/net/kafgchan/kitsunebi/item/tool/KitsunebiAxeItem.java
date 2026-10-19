package net.kafgchan.kitsunebi.item.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 狐火の斧
 * - 最大耐久: 1,048,576
 * - 超高速伐採＋単木一括伐採（最大512ブロック）
 */
public class KitsunebiAxeItem extends AxeItem implements IKitsunebiTool {

    public KitsunebiAxeItem(Properties properties) {
        super(ModToolTiers.KITSUNEBI, properties
                .durability(ModToolTiers.KITSUNEBI.getUses())
                .attributes(AxeItem.createAttributes(ModToolTiers.KITSUNEBI, 6.0F, -3.0F))
                .fireResistant()
                .rarity(Rarity.EPIC));
    }

    @Override
    public int getModeCount() {
        return 2;
    }

    @Override
    public String getModeTranslationKey(int mode) {
        return switch (mode) {
            case 1 -> "kitsunebi.skill.axe.fell_tree";
            default -> "kitsunebi.skill.axe.single";
        };
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean result = super.mineBlock(stack, level, state, pos, miner);
        if (!level.isClientSide && miner instanceof ServerPlayer player && ToolStateHelper.isActive(stack)) {
            int mode = ToolStateHelper.getMode(stack);
            if (mode == 1) {
                MiningHelper.fellTree((ServerLevel) level, player, pos, stack, net.kafgchan.kitsunebi.config.KitsunebiConfig.get().maxTreeFellLogs);
            }
        }
        return result;
    }

    @Override
    public void executeSkill(ItemStack stack, ServerPlayer player) {
        if (!ToolStateHelper.isActive(stack)) return;
        // 斧のVキー発動: 目の前の原木を一括伐採
        BlockPos origin = player.blockPosition().relative(player.getDirection());
        MiningHelper.fellTree(player.serverLevel(), player, origin, stack, net.kafgchan.kitsunebi.config.KitsunebiConfig.get().maxTreeFellLogs);
    }
}
