package com.ruby.mod.create_additional_energy_sourses.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class AdvancedPickaxeItem extends PickaxeItem {
    public AdvancedPickaxeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {

            if (!player.isCrouching()) {
                return super.mineBlock(stack, level, state, pos, entity);
            }

            HitResult rayTrace = player.pick(20.0D, 0.0F, false);
            if (rayTrace.getType() == HitResult.Type.BLOCK) {
                Direction side = ((BlockHitResult) rayTrace).getDirection();

                for (int a = -2; a <= 2; a++) {
                    for (int b = -1; b <= 3; b++) {
                        if (a == 0 && b == 0) continue;

                        BlockPos extraPos;
                        if (side == Direction.UP || side == Direction.DOWN) {
                            extraPos = pos.offset(a, 0, b);
                        } else if (side == Direction.NORTH || side == Direction.SOUTH) {
                            extraPos = pos.offset(a, b, 0);
                        } else {
                            extraPos = pos.offset(0, b, a);
                        }
                        BlockState extraState = level.getBlockState(extraPos);
                        boolean isBedrock = extraState.getBlock() == Blocks.BEDROCK;

                        if (this.isCorrectToolForDrops(stack, extraState) || isBedrock) {
                            // 1. Сначала ломаем сам блок в мире
                            level.destroyBlock(extraPos, false, player); // Ставим false, чтобы игра не пыталась безуспешно искать стандартный лут

                            // 2. Если это был бедрок — принудительно спавним предмет бедрока на его координатах!
                            if (isBedrock) {
                                ItemStack bedrockDrop = new ItemStack(Blocks.BEDROCK, 1);
                                net.minecraft.world.level.block.Block.popResource(level, extraPos, bedrockDrop);
                            }
                        }

                    }
                }
            }
        }
        return super.mineBlock(stack, level, state, pos, entity);
    }
}