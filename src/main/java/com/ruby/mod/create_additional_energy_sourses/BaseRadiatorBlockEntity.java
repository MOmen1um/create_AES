package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import java.util.List;
import net.minecraft.network.chat.Component;

public class BaseRadiatorBlockEntity extends SmartBlockEntity {
    // Внутренний бак радиатора на 4000 мБ (4 ведра воды)
    // 1. Переопределяем бак, чтобы он пинал блок при изменении содержимого
    public final FluidTank waterTank = new FluidTank(16000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            // Вызываем наш кастомный метод обновления
            BaseRadiatorBlockEntity.this.onWaterChanged();
        }
    };

    // 2. Метод, который находит двигатель и мгновенно обновляет его очки
    public void onWaterChanged() {
        this.setChanged();

        if (this.level != null && !this.level.isClientSide) {
            // Обновляем сам радиатор на клиенте
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);

            // Ищем двигатель во всех направлениях вокруг радиатора
            for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
                net.minecraft.core.BlockPos neighborPos = this.worldPosition.relative(direction);
                net.minecraft.world.level.block.entity.BlockEntity neighborBE = this.level.getBlockEntity(neighborPos);

                // Если рядом с радиатором нашёлся наш ДВС
                if (neighborBE instanceof V8EngineBlockEntity engine) {
                    engine.setChanged();
                    // Мгновенно отправляем пакет обновлений на клиент двигателя!
                    this.level.sendBlockUpdated(engine.getBlockPos(), engine.getBlockState(), engine.getBlockState(), 3);
                }
            }
        }
    }

    public BaseRadiatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}


    // Сохранение бака радиатора на диск (1.21.1 стандарты с registries)
    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        CompoundTag fluidTag = new CompoundTag();
        this.waterTank.writeToNBT(registries, fluidTag);
        tag.put("WaterTank", fluidTag);
    }


    // Чтение бака радиатора с диска
    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("WaterTank")) {
            this.waterTank.readFromNBT(registries, tag.getCompound("WaterTank"));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        CompoundTag fluidTag = new CompoundTag();
        this.waterTank.writeToNBT(registries, fluidTag);
        tag.put("WaterTank", fluidTag);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
    public void onFluidChanged() {
        this.setChanged();

        if (this.level != null && !this.level.isClientSide) {
            // 1. Мгновенно обновляем сам радиатор на клиенте
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);

            // 2. ФОКУС: Ищем двигатель во всех направлениях вокруг радиатора
            for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
                net.minecraft.core.BlockPos neighborPos = this.worldPosition.relative(direction);
                net.minecraft.world.level.block.entity.BlockEntity neighborBE = this.level.getBlockEntity(neighborPos);

                // Если рядом с радиатором нашёлся наш ДВС
                if (neighborBE instanceof V8EngineBlockEntity engine) {
                    // Принудительно заставляем двигатель обновиться!
                    engine.setChanged();

                    // Отправляем пакет на клиент игрока, чтобы очки инженера
                    // мгновенно переключили надпись "НЕТ ВОДЫ!" на синюю полоску с литрами!
                    this.level.sendBlockUpdated(engine.getBlockPos(), engine.getBlockState(), engine.getBlockState(), 3);
                }
            }
        }
    }

}

