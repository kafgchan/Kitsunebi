package net.kafgchan.kitsunebi.item.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
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
                .attributes(HoeItem.createAttributes(ModToolTiers.KITSUNEBI, 0.0F, -1.0F))
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
            // 3x3 または 5x5 耕作 (super.useOn が内部で1マス毎に耐久を適正消費するため、二重消費を防止)
            if (level.isClientSide) return InteractionResult.SUCCESS;
            if (!(context.getPlayer() instanceof ServerPlayer sp)) return InteractionResult.PASS;
            int radius = (mode == 1) ? 1 : 2;
            int count = 0;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (stack.isEmpty() || stack.getDamageValue() >= stack.getMaxDamage()) break;
                    BlockPos pos = clickedPos.offset(dx, 0, dz);
                    if (!level.mayInteract(sp, pos) || !sp.mayUseItemAt(pos, context.getClickedFace(), stack)) continue;
                    UseOnContext subContext = new UseOnContext(context.getPlayer(), context.getHand(),
                            new net.minecraft.world.phys.BlockHitResult(context.getClickLocation().add(dx,0,dz), context.getClickedFace(), pos, context.isInside()));
                    if (super.useOn(subContext).consumesAction()) {
                        count++;
                    }
                }
            }
            report(sp, "till", count);
            return count > 0 ? InteractionResult.SUCCESS : InteractionResult.PASS;
        } else if (mode == 3 && !level.isClientSide && context.getPlayer() instanceof ServerPlayer sp) {
            // 9x9 一括収穫＆自動植え直し
            harvestAndReplant(sp.serverLevel(), clickedPos, sp, stack);
            return InteractionResult.SUCCESS;
        } else if (mode == 4 && !level.isClientSide && context.getPlayer() instanceof ServerPlayer sp) {
            // 9x9 成長促進
            applyGrowth(sp.serverLevel(), clickedPos, sp, stack);
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }

    @Override
    public void executeSkill(ItemStack stack, ServerPlayer player) {
        if (!ToolStateHelper.isActive(stack)) return;
        // Vキー発動: プレイヤー足元中心の 9x9 成長促進または収穫
        var hit=player.pick(6.0,0,false);
        BlockPos center=hit.getType()==net.minecraft.world.phys.HitResult.Type.BLOCK ? ((net.minecraft.world.phys.BlockHitResult)hit).getBlockPos() : player.blockPosition().below();
        int mode = ToolStateHelper.getMode(stack);
        if (mode <= 2) {
            var context=new UseOnContext(player,net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(center),net.minecraft.core.Direction.UP,center,false));
            var result=useOn(context);
            if(mode==0)report(player,"till",result.consumesAction()?1:0);
        } else if (mode == 3) {
            harvestAndReplant(player.serverLevel(), center, player, stack);
        } else if (mode == 4) {
            applyGrowth(player.serverLevel(), center, player, stack);
        }
    }

    private void harvestAndReplant(ServerLevel level, BlockPos center, ServerPlayer player, ItemStack tool) {
        int radius = 4; // 9x9
        int harvested=0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (tool.isEmpty() || tool.getDamageValue() >= tool.getMaxDamage()) return;
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                        net.minecraft.world.item.Item seedItem = getSeedForCrop(crop);
                        // 通常の採掘破壊経路（保護判定・幸運等）を経由して破壊
                        int damageBefore=tool.getDamageValue();
                        if (player.gameMode.destroyBlock(pos)) {
                            harvested++;
                            if(tool.getDamageValue()==damageBefore)tool.hurtAndBreak(1,player,EquipmentSlot.MAINHAND);
                            // ドロップまたは所持品から種を確保・消費できた場合のみ再播種
                            if (consumeSeed(level, pos, player, seedItem)) {
                                level.setBlock(pos, crop.getStateForAge(0), 3);
                            }
                        }
                    }
                }
            }
        }
        report(player,"harvest",harvested);
    }

    private static net.minecraft.world.item.Item getSeedForCrop(CropBlock crop) {
        if (crop == net.minecraft.world.level.block.Blocks.WHEAT) return net.minecraft.world.item.Items.WHEAT_SEEDS;
        if (crop == net.minecraft.world.level.block.Blocks.BEETROOTS) return net.minecraft.world.item.Items.BEETROOT_SEEDS;
        if (crop == net.minecraft.world.level.block.Blocks.CARROTS) return net.minecraft.world.item.Items.CARROT;
        if (crop == net.minecraft.world.level.block.Blocks.POTATOES) return net.minecraft.world.item.Items.POTATO;
        return crop.asItem();
    }

    private static boolean consumeSeed(ServerLevel level, BlockPos pos, ServerPlayer player, net.minecraft.world.item.Item seedItem) {
        if (player.isCreative()) return true;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (!item.isEmpty() && item.is(seedItem)) {
                item.shrink(1);
                return true;
            }
        }
        for(var drop:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(pos).inflate(1))) {
            if(drop.getItem().is(seedItem)) {
                drop.getItem().shrink(1);
                if(drop.getItem().isEmpty())drop.discard();
                return true;
            }
        }
        return false;
    }

    private static void report(ServerPlayer player,String action,int count) {
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.kitsunebi.farming_"+action,count),true);
        if(count>0)player.serverLevel().playSound(null,player.blockPosition(),net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,net.minecraft.sounds.SoundSource.PLAYERS,.6F,1.3F);
    }

    private void applyGrowth(ServerLevel serverLevel, BlockPos center, ServerPlayer player, ItemStack tool) {
        int radius = 4; // 9x9
        int affected = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (tool.isEmpty() || tool.getDamageValue() >= tool.getMaxDamage()) break;
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!serverLevel.mayInteract(player, pos)) continue;
                    BlockState state = serverLevel.getBlockState(pos);
                    if (state.getBlock() instanceof BonemealableBlock bonemealable && bonemealable.isValidBonemealTarget(serverLevel, pos, state)) {
                        if (bonemealable.isBonemealSuccess(serverLevel, serverLevel.random, pos, state)) {
                            bonemealable.performBonemeal(serverLevel, serverLevel.random, pos, state);
                            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.2, 0.2, 0.2, 0.05);
                            affected++;
                        }
                    }
                }
            }
        }
        report(player,"growth",affected);
        if (affected > 0) {
            tool.hurtAndBreak(affected, player, EquipmentSlot.MAINHAND);
        }
    }
}
