package net.kafgchan.kitsunebi.item;

import net.kafgchan.kitsunebi.block.KitsunebiBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 狐火アイテム（空中設置対応）
 */
public class KitsunebiItem extends BlockItem {
    public KitsunebiItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return super.use(level, player, hand);
        }

        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 targetPos = eyePos.add(lookVec.scale(3.5));

        BlockPos placePos = BlockPos.containing(targetPos);
        BlockState currentState = level.getBlockState(placePos);

        if (currentState.isAir() || currentState.canBeReplaced()) {
            if (!level.isClientSide) {
                FluidState fluidState = level.getFluidState(placePos);
                boolean isWater = fluidState.getType() == Fluids.WATER;

                BlockState placeState = this.getBlock().defaultBlockState()
                        .setValue(KitsunebiBlock.LIT, true)
                        .setValue(KitsunebiBlock.VISIBLE, false)
                        .setValue(KitsunebiBlock.WATERLOGGED, isWater);

                if (level.setBlock(placePos, placeState, Block.UPDATE_ALL)) {
                    level.playSound(null, placePos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.2F);
                    if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }
                }
            }
            player.swing(hand);
            return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
        }

        return super.use(level, player, hand);
    }
}
