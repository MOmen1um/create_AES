package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
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
    public net.minecraft.core.Direction.Axis getRotationAxis(BlockState state) {
        // Направление вала должно СОВПАДАТЬ с тем, куда смотрит сам двигатель!
        if (state.hasProperty(HORIZONTAL_FACING)) {
            return state.getValue(HORIZONTAL_FACING).getAxis();
        }
        return net.minecraft.core.Direction.Axis.X;
    }


    @Override
    public Class<V8EngineBlockEntity> getBlockEntityClass() {
        return V8EngineBlockEntity.class;
    }

    @Override
    public net.minecraft.world.level.block.entity.BlockEntityType<? extends V8EngineBlockEntity> getBlockEntityType() {
        // Указали универсальный тип с помощью '? extends', чтобы чугун, алюминий и титан могли использовать этот базовый класс блока!
        return ModBlocks.V8_ENGINE_ENTITY.get();
    }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hitResult) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof V8EngineBlockEntity v8) {

                // Получаем предмет в главной руке игрока
                net.minecraft.world.item.ItemStack heldItem = player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND);

                // --- СВЯЗКА С CREATE DIESEL GENERATORS ---
                // Динамически ищем ID турбонаддува в глобальном реестре Майнкрафта
                net.minecraft.resources.ResourceLocation turbochargerId = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("createdieselgenerators", "engine_turbocharger");
                net.minecraft.world.item.Item turbochargerItem = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(turbochargerId);

                // Проверяем: если предмет существует в сборке, игрок держит именно его и турбина ещё не установлена
                if (turbochargerItem != null && heldItem.is(turbochargerItem) && !v8.isTurboCharged) {
                    // Активируем турбонаддув!
                    v8.isTurboCharged = true;

                    // Забираем 1 турбокомпрессор из руки (если игрок не в креативе)
                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                    }

                    // Проигрываем сочный звук шестерёнок Create при успешной установке
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.IRON_TRAPDOOR_CLOSE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.2f);

                    // Мгновенно синхронизируем данные, чтобы меню и очки инженера обновились!
                    v8.setChanged();
                    v8.sendData();
                    level.sendBlockUpdated(pos, state, state, 3);

                    return net.minecraft.world.InteractionResult.SUCCESS;
                }
                // -----------------------------------------
            }
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
    @Override
    public void onRemove(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                if (level.getBlockEntity(pos) instanceof V8EngineBlockEntity v8) {

                    // 1. ВОССТАНОВИЛИ: Создаем ItemStack нашего двигателя (берём его BlockItem)
                    net.minecraft.world.item.ItemStack dropItem = new net.minecraft.world.item.ItemStack(this.asItem());

                    // 2. Создаем NBT-тег и зашиваем туда наши параметры сохранения
                    net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();

                    // ИСПРАВЛЕНО: Добавили принудительное приведение типов (float) к Math.round, чтобы Java не ругалась!
                    tag.putFloat("EngineQuality", (float) Math.round(v8.engineQuality * 100.0f) / 100.0f);
                    tag.putFloat("SecretEfficiency", (float) Math.round(v8.secretEfficiency * 100.0f) / 100.0f);

                    // Безопасное хранение в CUSTOM_DATA для Minecraft 1.21.1
                    dropItem.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                            net.minecraft.world.item.component.CustomData.of(tag));

                    // 3. Выбрасываем предмет физически в мир на координаты блока
                    net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                            level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, dropItem
                    );
                    level.addFreshEntity(itemEntity);
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
    // 1. ОТКЛЮЧАЕМ ЭФФЕКТ РЕНТГЕНА (Говорим игре честно рендерить соседние блоки)
    @Override
    protected boolean propagatesSkylightDown(BlockState state, net.minecraft.world.level.BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    protected int getLightBlock(BlockState state, net.minecraft.world.level.BlockGetter worldIn, BlockPos pos) {
        return 0; // Свет проходит сквозь пустые места модели
    }

    // 2. КАСТОМНАЯ СЕТКА ХИТБОКСА (VoxelShape)
    // Сейчас мы настроим сетку, которая чуть меньше стандартного блока по высоте и ширине!
    @Override
    protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter world, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        // Координаты задаются в пикселях от 0 до 16:
        // x1: 1, y1: 0, z1: 1 (отступаем по 1 пикселю с краев)
        // x2: 15, y2: 14, z2: 15 (модель чуть ниже блока по высоте)
        return Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
    }
}