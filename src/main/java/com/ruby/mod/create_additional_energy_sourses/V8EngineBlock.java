package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class V8EngineBlock extends HorizontalKineticBlock implements IBE<V8EngineBlockEntity> {

    public V8EngineBlock(Properties properties) {
        super(properties);
    }



    // Каноничный способ активировать ползунок настроек Create в 1.21.1
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Берём направление, куда смотрит игрок, и КРУТИМ НА 180 градусов (.getOpposite())
        Direction facing = context.getHorizontalDirection();

        return this.defaultBlockState().setValue(HORIZONTAL_FACING, facing);
    }


    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        Direction facing = state.getValue(HORIZONTAL_FACING);
        // Теперь коленвал будет выходить со стороны, противоположной «лицу» в коде (которая стала физическим передом)
        return face == facing.getOpposite();
    }

    @Override
    public net.minecraft.core.Direction.Axis getRotationAxis(net.minecraft.world.level.block.state.BlockState state) {
        // Вернули имя getRotationAxis, которое требует компилятор!
        // Теперь оно идеально переопределяет родительский класс Create без дубликатов
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }


    @Override
    public Class<V8EngineBlockEntity> getBlockEntityClass() {
        return V8EngineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends V8EngineBlockEntity> getBlockEntityType() {
        // Эту корзину мы сейчас добавим в ModBlocks
        return ModBlocks.V8_ENGINE_ENTITY.get();
    }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hitResult) {
        if (!level.isClientSide) {
            // Получаем "мозг" нашего мотора
            if (level.getBlockEntity(pos) instanceof V8EngineBlockEntity v8) {

                // Проверяем, держит ли игрок в ГЛАВНОЙ руке Железный Слиток
                net.minecraft.world.item.ItemStack heldItem = player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND);

                if (heldItem.is(net.minecraft.world.item.Items.IRON_INGOT) && !v8.isTurboCharged) {
                    // Активируем турбину!
                    v8.isTurboCharged = true;

                    // Забираем один слиток из руки игрока (если он не в креативе)
                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                    }

                    // Проигрываем сочный ванильный звук установки (например, наковальни или железа)
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.IRON_TRAPDOOR_OPEN, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.2f);

                    // Мгновенно синхронизируем данные, чтобы очки инженера обновились!
                    v8.setChanged();
                    v8.sendData();
                    level.sendBlockUpdated(pos, state, state, 3);

                    return net.minecraft.world.InteractionResult.SUCCESS;
                }
            }
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }



}