package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ModularEnginesBlock extends V8EngineBlock {

    private final boolean isController;

    public ModularEnginesBlock(Properties properties) {
        super(properties);
        this.isController = false;
    }

    public ModularEnginesBlock(Properties properties, boolean isController) {
        super(properties);
        this.isController = isController;
    }

    // ХАК ДЛЯ КРАША: Явно говорим Create, какой ТИП сущности привязан к этому блоку!
    // Этот метод ищет внутренний движок Create при проверке чанков
    public BlockEntityType<? extends ModularEnginesBlockEntity> getBlockEntityType() {
        return ModBlocks.MODULAR_ENGINE_ENTITY.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ModularEnginesBlockEntity(pos, state, this.isController);
    }

    public boolean isController() {
        return this.isController;
    }
    @Override
    public void setPlacedBy(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        // Получаем наш тайл энтити с правильной позиции через level
        if (level.getBlockEntity(pos) instanceof ModularEnginesBlockEntity thisEntity) {
            // HORIZONTAL_FACING берем прямо из стейта, который нам передал метод
            net.minecraft.core.Direction facing = state.getValue(HORIZONTAL_FACING);
            net.minecraft.core.BlockPos behindPos = pos.relative(facing.getOpposite());

            // Проверяем блок сзади
            if (level.getBlockEntity(behindPos) instanceof ModularEnginesBlockEntity neighbor
                    && neighbor.getBlockState().hasProperty(HORIZONTAL_FACING)
                    && neighbor.getBlockState().getValue(HORIZONTAL_FACING) == facing) {

                thisEntity.setController(false); // Становимся пак-блоком
            } else {
                thisEntity.setController(true);  // Становимся главным контроллером
            }
        }
    }
}


