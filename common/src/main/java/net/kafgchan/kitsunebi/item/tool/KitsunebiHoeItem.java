package net.kafgchan.kitsunebi.item.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 狐火のクワ
 * - 最大耐久: 1,048,576
 * - 単体/3x3/5x5 耕作
 * - 9x9 一括収穫＆自動植え直し
 * - 9x9 成長促進（骨粉相当1回適用、消費なし）
 */
public class KitsunebiHoeItem extends HoeItem implements IKitsunebiTool {

    public KitsunebiHoeItem(Properties properties) {
        super(ModToolTiers.KITSUNEBI, properties
                .durability(ModToolTiers.KITSUNEBI.getUses())
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
            case 1 -> "kitsunebi.skill.hoe.till_3x3";
            case 2 -> "kitsunebi.skill.hoe.till_5x5";
            case 3 -> "kitsunebi.skill.hoe.harvest_9x9";
            case 4 -> "kitsunebi.skill.hoe.growth_9x9";
            default -> "kitsunebi.skill.hoe.single";
        };
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        if (!ToolStateHelper.isActive(stack)) {
            return super.useOn(context);
        }

        int mode = ToolStateHelper.getMode(stack);
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();

        if (mode == 1 || mode == 2) {
            // 3x3 または 5x5 耕作
            int radius = (mode == 1) ? 1 : 2;
            int count = 0;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = clickedPos.offset(dx, 0, dz);
                    UseOnContext subContext = new UseOnContext(context.getPlayer(), context.getHand(),
                            new net.minecraft.world.phys.BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, context.isInside()));
                    if (super.useOn(subContext).consumesAction()) {
                        count++;
                    }
                }
            }
            if (count > 0 && context.getPlayer() != null) {
                stack.hurtAndBreak(count, context.getPlayer(), EquipmentSlot.MAINHAND);
                return InteractionResult.SUCCESS;
            }
        } else if (mode == 3 && !level.isClientSide) {
            // 9x9 一括収穫＆自動植え直し
            harvestAndReplant(level, clickedPos, context.getPlayer(), stack);
            return InteractionResult.SUCCESS;
        } else if (mode == 4 && !level.isClientSide) {
            // 9x9 成長促進
            applyGrowth(level, clickedPos, stack);
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }

    @Override
    public void executeSkill(ItemStack stack, ServerPlayer player) {
        // Vキー発動: プレイヤー足元中心の 9x9 成長促進または収穫
        BlockPos center = player.blockPosition();
        int mode = ToolStateHelper.getMode(stack);
        if (mode == 3) {
            harvestAndReplant(player.serverLevel(), center, player, stack);
        } else {
            applyGrowth(player.serverLevel(), center, stack);
        }
    }

    private void harvestAndReplant(Level level, BlockPos center, net.minecraft.world.entity.player.Player player, ItemStack tool) {
        int radius = 4; // 9x9
        int harvested = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                        level.destroyBlock(pos, true, player);
                        // 植え直し（初期段階作物の設置）
                        level.setBlock(pos, crop.getStateForAge(0), 3);
                        harvested++;
                    }
                }
            }
        }
        if (harvested > 0 && player != null) {
            tool.hurtAndBreak(harvested, player, EquipmentSlot.MAINHAND);
        }
    }

    private void applyGrowth(Level level, BlockPos center, ItemStack tool) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        int radius = 4; // 9x9
        int affected = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof BonemealableBlock bonemealable && bonemealable.isValidBonemealTarget(level, pos, state)) {
                        if (bonemealable.isBonemealSuccess(level, level.random, pos, state)) {
                            bonemealable.performBonemeal(serverLevel, level.random, pos, state);
                            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.2, 0.2, 0.2, 0.05);
                            affected++;
                        }
                    }
                }
            }
        }
    }
}
