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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class AdvancedPickaxeItem extends PickaxeItem {
    public AdvancedPickaxeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    // Метод, который вызывается при разрушении блока
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            // Проверяем, на какую сторону блока смотрит игрок
            HitResult rayTrace = player.pick(20.0D, 0.0F, false);
            if (rayTrace.getType() == HitResult.Type.BLOCK) {
                Direction side = ((BlockHitResult) rayTrace).getDirection();

                // Проходим циклом по соседним блокам в зависимости от стороны
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        if (x == 0 && y == 0) continue; // Пропускаем центральный (уже сломанный) блок

                        BlockPos extraPos;
                        // Если смотрим вверх/вниз, расширяем по X и Z
                        if (side == Direction.UP || side == Direction.DOWN) {
                            extraPos = pos.offset(x, 0, y);
                        } else if (side == Direction.NORTH || side == Direction.SOUTH) {
                            // Если смотрим на север/юг, расширяем по X и Y
                            extraPos = pos.offset(x, y, 0);
                        } else {
                            // Если смотрим на запад/восток, расширяем по Z и Y
                            extraPos = pos.offset(0, x, y);
                        }

                        // Ломаем блок, если кирка может его добыть
                        BlockState extraState = level.getBlockState(extraPos);
                        if (this.isCorrectToolForDrops(stack, extraState)) {
                            level.destroyBlock(extraPos, true, player);
                        }
                    }
                }
            }
        }
        return super.mineBlock(stack, level, state, pos, entity);
    }
}
