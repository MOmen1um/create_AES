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
        if (level == null || level.isClientSide || getBlockState() == null) return;

        cylinderCount = 0;
        isTitaniumEngine = false;
        isAluminumEngine = false;

        Direction facing = getBlockState().getValue(CrankshaftBlock.HORIZONTAL_FACING);

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

        updateGeneratedRotation();
    }

    @Override
    public float getGeneratedSpeed() {
        if (cylinderCount <= 0) return 0;
        if (isAluminumEngine) return 256.0f;
        if (isTitaniumEngine) return 64.0f;
        return 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (cylinderCount <= 0) return 0;
        if (isTitaniumEngine) return cylinderCount * 512.0f;
        if (isAluminumEngine) return cylinderCount * 128.0f;
        return 0;
    }
}
