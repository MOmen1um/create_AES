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
    public final FluidTank waterTank = new FluidTank(16000);

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

}

