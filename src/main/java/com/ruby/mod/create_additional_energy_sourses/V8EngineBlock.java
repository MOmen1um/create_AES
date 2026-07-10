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

                    v8.updateGeneratedRotation();

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
    // 1. ОТКЛЮЧАЕМ ЭФФЕКТ РЕНТГЕНА (Говорим игре честно рендерить соседние блоки)
    @Override
    protected boolean propagatesSkylightDown(BlockState state, net.minecraft.world.level.BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    protected int getLightBlock(BlockState state, net.minecraft.world.level.BlockGetter worldIn, BlockPos pos) {
        return 0; // Свет проходит сквозь пустые места модели
    }


    // 2. Указываем правильный тип рендера для кастомных 3D-моделей
    @Override
    public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.MODEL;
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
    // Говорим Create, что при клике или зажатии W на этом блоке нужно открыть Ponder
    @Override
    public void onPlace(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.level.block.state.BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        // Этот метод-маркер автоматически связывает физический блок в мире с подсказкой кнопки W
    }
}
