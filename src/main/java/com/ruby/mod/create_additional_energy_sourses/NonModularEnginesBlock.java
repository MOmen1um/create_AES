package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class NonModularEnginesBlock extends HorizontalKineticBlock implements EntityBlock {

    private final String material;
    private final String engineType;

    public NonModularEnginesBlock(Properties properties, String material, String engineType) {
        super(properties);
        this.material = material;
        this.engineType = engineType;
    }

    // 1. Создание сущности блока (Связано с нашим универсальным типом)
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Оставляем строго 2 аргумента! Используем правильный путь к нашему классу в папке Setup
        return new com.ruby.mod.create_additional_energy_sourses.NonModularEnginesBlockEntity(pos, state);
    }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hitResult) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof NonModularEnginesBlockEntity v8) {

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

    // Обязательные кинетические методы Create
    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        Direction facing = state.getValue(HORIZONTAL_FACING);
        // Теперь коленвал будет выходить со стороны, противоположной «лицу» в коде (которая стала физическим передом)
        return face == facing.getOpposite();
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Берём направление, куда смотрит игрок, и КРУТИМ НА 180 градусов (.getOpposite())
        Direction facing = context.getHorizontalDirection();

        return this.defaultBlockState().setValue(HORIZONTAL_FACING, facing);
    }
    @Override
    protected boolean propagatesSkylightDown(BlockState state, net.minecraft.world.level.BlockGetter reader, BlockPos pos) {
        return true;
    }
    @Override
    public void neighborChanged(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.Block block, net.minecraft.core.BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);

        // Когда блок рядом меняется (например, убрали радиатор),
        // мы принудительно дергаем BlockEntity, чтобы он пересчитал параметры
        if (!level.isClientSide) {
            net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof NonModularEnginesBlockEntity engine) {
                // Вызываем принудительное обновление сети Create и NBT
                engine.setChanged();
                engine.notifyUpdate();
            }
        }
    }
}

