package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CrankshaftBlockEntity extends GeneratingKineticBlockEntity {

    private int cylinderCount = 0;
    private boolean isTitaniumEngine = false;
    private boolean isAluminumEngine = false;

    public CrankshaftBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        super(type, pos, state);
    }

    public void scanEngineStructure() {
        // Добавили жесткую проверку безопасности: если мира нет или он пустой — выходим!
        if (level == null || level.isClientSide || getBlockState() == null) return;

        cylinderCount = 0;
        isTitaniumEngine = false;
        isAluminumEngine = false;

        Direction facing = getBlockState().getValue(CrankshaftBlock.HORIZONTAL_FACING);

        // 3D-МАТРИЦА СКАНИРОВАНИЯ
        for (int yOffset = 1; yOffset <= 4; yOffset++) {
            for (int widthOffset = -2; widthOffset <= 2; widthOffset++) {
                BlockPos targetPos = worldPosition.above(yOffset).relative(facing.getClockWise(), widthOffset);
                BlockState checkState = level.getBlockState(targetPos);

                if (checkState.is(ModBlocks.TITANIUM_BLOCK.get())) {
                    cylinderCount++;
                    isTitaniumEngine = true;
                } else if (checkState.is(ModBlocks.ALUMINUM_BLOCK.get())) {
                    cylinderCount++;
                    isAluminumEngine = true;
                }
            }
        }

        // БЕЗОПАСНЫЙ ВЫВОД: отправляем сообщения, только если в мире РЕАЛЬНО есть игроки!
        if (!level.players().isEmpty()) {
            if (cylinderCount > 0) {
                String motorType = isTitaniumEngine ? "§6Тяжелый Титановый" : "§bЛегкий Алюминиевый";
                level.players().forEach(player -> player.sendSystemMessage(
                        Component.literal("§e[ДВС] §aСтруктура обнаружена! Тип: " + motorType + "§a, Мощность блоков: §e" + cylinderCount)
                ));
            } else {
                level.players().forEach(player -> player.sendSystemMessage(
                        Component.literal("§e[ДВС] §cДвигатель не собран! Поставьте поршни сверху.")
                ));
            }
        }

        // Метод Create, который обновляет вращение валов в сети
        updateGeneratedRotation();
    }
}
