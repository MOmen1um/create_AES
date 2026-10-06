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
        // 1. Фиксим урон: регистрируем свойства через новые дата-компоненты NeoForge 1.21.1.
        // 4.0F — урон, который плюсуется к урону тира. -2.8F — скорость атаки инструментом.
        super(tier, properties.attributes(PickaxeItem.createAttributes(tier, 4.0F, -2.8F)));
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {

            // Если игрок копает сидя — работает как обычная кирка 1х1
            if (!player.isCrouching()) {
                return super.mineBlock(stack, level, state, pos, entity);
            }

            HitResult rayTrace = player.pick(20.0D, 0.0F, false);
            if (rayTrace.getType() == HitResult.Type.BLOCK) {
                Direction side = ((BlockHitResult) rayTrace).getDirection();

                // 2. Делаем трехмерный куб 5х5х5.
                // Цикл 'depth' отвечает за продвижение на 5 блоков ВГЛУБЬ (от 0 до 4) относительно стороны блока
                for (int depth = -4; depth < 1; depth++) {
                    for (int a = -2; a <= 2; a++) {
                        for (int b = -1; b <= 3; b++) {
                            // Пропускаем самый первый блок, так как его игра ломает сама
                            if (depth == 1 && a == 0 && b == 0) continue;

                            BlockPos extraPos;

                            // Вычисляем смещение с учетом взгляда на плоскость и глубины погружения
                            if (side == Direction.UP || side == Direction.DOWN) {
                                // Если смотрим в пол/потолок: 'depth' идет по оси Y (внутрь), 'a' и 'b' по X и Z
                                int yOffset = (side == Direction.DOWN) ? depth : -depth;
                                extraPos = pos.offset(a, yOffset, b);
                            } else if (side == Direction.NORTH || side == Direction.SOUTH) {
                                // Если смотрим на север/юг: 'depth' идет по оси Z, 'a' и 'b' по X и Y
                                int zOffset = (side == Direction.SOUTH) ? depth : -depth;
                                extraPos = pos.offset(a, b, zOffset);
                            } else {
                                // Если смотрим на восток/запад: 'depth' идет по оси X, 'a' и 'b' по Z и Y
                                int xOffset = (side == Direction.EAST) ? depth : -depth;
                                extraPos = pos.offset(xOffset, b, a);
                            }

                            BlockState extraState = level.getBlockState(extraPos);
                            boolean isBedrock = extraState.getBlock() == Blocks.BEDROCK;

                            if (this.isCorrectToolForDrops(stack, extraState) || isBedrock) {
                                // Ломаем блок с полноценным выпадением лута
                                level.destroyBlock(extraPos, true, player);

                                // Если это был бедрок — принудительно спавним предмет бедрока
                                if (isBedrock) {
                                    ItemStack bedrockDrop = new ItemStack(Blocks.BEDROCK, 1);
                                    net.minecraft.world.level.block.Block.popResource(level, extraPos, bedrockDrop);
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.mineBlock(stack, level, state, pos, entity);
    }
}
