package net.kafgchan.kitsunebi.item.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 狐火のツルハシ
 * - 最大耐久: 1,048,576
 * - 超高速採掘（黒曜石約0.1秒）
 * - モード: 単体 / 3x3 面採掘 / 5x5 面採掘 / 連結鉱脈採掘（最大128） / トンネル掘削（5x5x32）
 */
public class KitsunebiPickaxeItem extends PickaxeItem implements IKitsunebiTool {

    public KitsunebiPickaxeItem(Properties properties) {
        super(ModToolTiers.KITSUNEBI, properties
                .durability(ModToolTiers.KITSUNEBI.getUses())
                .attributes(PickaxeItem.createAttributes(ModToolTiers.KITSUNEBI, 1.0F, -2.8F))
                .fireResistant()
                .rarity(Rarity.EPIC));
    }

    @Override
    public int getModeCount() {
        return 5;
    }

    @Override
    public String getModeTranslationKey(int mode) {
        return switch (mode) {
            case 1 -> "kitsunebi.skill.pickaxe.area_3x3";
            case 2 -> "kitsunebi.skill.pickaxe.area_5x5";
            case 3 -> "kitsunebi.skill.pickaxe.vein";
            case 4 -> "kitsunebi.skill.pickaxe.tunnel";
            default -> "kitsunebi.skill.pickaxe.single";
        };
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean result = super.mineBlock(stack, level, state, pos, miner);
        if (!level.isClientSide && miner instanceof ServerPlayer player && ToolStateHelper.isActive(stack)) {
            int mode = ToolStateHelper.getMode(stack);
            ServerLevel serverLevel = (ServerLevel) level;
            net.kafgchan.kitsunebi.config.KitsunebiConfig config = net.kafgchan.kitsunebi.config.KitsunebiConfig.get();
            switch (mode) {
                case 1 -> MiningHelper.breakArea(serverLevel, player, pos, 1, stack); // 3x3
                case 2 -> MiningHelper.breakArea(serverLevel, player, pos, 2, stack); // 5x5
                case 3 -> MiningHelper.breakVein(serverLevel, player, pos, stack, config.maxVeinBlocks); // 鉱脈
                case 4 -> MiningHelper.breakTunnel(serverLevel, player, pos, stack, config.tunnelLength); // トンネル
            }
        }
        return result;
    }

    @Override
    public void executeSkill(ItemStack stack, ServerPlayer player) {
        if (!ToolStateHelper.isActive(stack)) return;
        int mode = ToolStateHelper.getMode(stack);
        BlockPos origin = player.blockPosition().relative(player.getDirection());
        ServerLevel level = player.serverLevel();
        net.kafgchan.kitsunebi.config.KitsunebiConfig config = net.kafgchan.kitsunebi.config.KitsunebiConfig.get();

        if (mode == 4) {
            MiningHelper.breakTunnel(level, player, origin, stack, config.tunnelLength);
        } else if (mode == 2) {
            MiningHelper.breakArea(level, player, origin, 2, stack);
        } else if (mode == 1) {
            MiningHelper.breakArea(level, player, origin, 1, stack);
        }
    }
}
