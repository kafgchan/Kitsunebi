package net.kafgchan.kitsunebi.item;

import net.kafgchan.kitsunebi.block.KitsunebiBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * 狐火専用道具（御幣）
 */
public class KitsunebiToolItem extends Item {
    public KitsunebiToolItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (state.getBlock() instanceof KitsunebiBlock) {
            boolean isSneaking = player != null && player.isShiftKeyDown();

            if (isSneaking) {
                boolean currentLit = state.getValue(KitsunebiBlock.LIT);
                boolean newLit = !currentLit;
                level.setBlock(pos, state.setValue(KitsunebiBlock.LIT, newLit), Block.UPDATE_ALL);

                if (!level.isClientSide) {
                    float pitch = newLit ? 1.5F : 0.8F;
                    level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, pitch);
                    if (player != null) {
                        Component message = newLit
                                ? Component.translatable("message.kitsunebi.light_on")
                                : Component.translatable("message.kitsunebi.light_off");
                        player.displayClientMessage(message, true);
                    }
                }
            } else {
                boolean currentVisible = state.getValue(KitsunebiBlock.VISIBLE);
                boolean newVisible = !currentVisible;
                level.setBlock(pos, state.setValue(KitsunebiBlock.VISIBLE, newVisible), Block.UPDATE_ALL);

                if (!level.isClientSide) {
                    float pitch = newVisible ? 1.4F : 1.0F;
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, pitch);
                    if (player != null) {
                        Component message = newVisible
                                ? Component.translatable("message.kitsunebi.visible_on")
                                : Component.translatable("message.kitsunebi.visible_off");
                        player.displayClientMessage(message, true);
                    }
                }
            }

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.END_ROD,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        10,
                        0.2, 0.2, 0.2, 0.02
                );
            }

            if (player != null) {
                player.swing(context.getHand());
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.kitsunebi.tool_mode_toggle"));
        tooltipComponents.add(Component.translatable("tooltip.kitsunebi.tool_light_toggle"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
