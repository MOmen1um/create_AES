package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class EngineCarterBlock extends HorizontalKineticBlock implements IBE<EngineCarterBlockEntity> {

    // Правильный конструктор для блоков Create
    public EngineCarterBlock(Properties properties) {
        super(properties);
    }

    // Связываем BlockEntity с нашим классом
    @Override
    public Class<EngineCarterBlockEntity> getBlockEntityClass() {
        return EngineCarterBlockEntity.class;
    }

    // Регистрируем тип нашей сущности
    @Override
    public BlockEntityType<? extends EngineCarterBlockEntity> getBlockEntityType() {
        return ModBlocks.ENGINE_CARTER_ENTITY.get(); // Убедись, что это имя совпадает с твоим регистратором
    }

    // Вспомогательный метод для успешного завершения шага (тратит предмет и обновляет блок)
    private InteractionResult finishStep(Player player, ItemStack heldItem, Level level, BlockPos pos, SoundEvent sound, EngineCarterBlockEntity be) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 0.5f, 1.0f);

        if (!player.isCreative()) {
            heldItem.shrink(1);
        }

        be.setChanged();
        level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
        return InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof EngineCarterBlockEntity be) {
            ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

            // =================================================================
            // ЭТАП 1: КОЛЕНВАЛ (Для всех движков одинаково)
            // =================================================================
            if (!be.hasCrankshaft) {
                if (heldItem.is(Items.IRON_INGOT)) {
                    be.hasCrankshaft = true;
                    player.displayClientMessage(Component.literal("§6[ДВС] Коленчатый вал успешно установлен!"), true);
                    return finishStep(player, heldItem, level, pos, SoundEvents.ANVIL_PLACE, be);
                } else {
                    player.displayClientMessage(Component.literal("§c[ДВС] Сначала установите коленчатый вал (Железный слиток)!"), true);
                    level.playSound(null, pos, com.simibubi.create.AllSoundEvents.DENY.getMainEvent(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.CONSUME;
                }
            }

            // =================================================================
            // ЭТАП 2: ПОРШНИ (Автоматически подтягивают нужный металл)
            // =================================================================
            if (be.installedPistons < be.maxPistons) {
                Item requiredPiston = be.engineMaterial.equals("titanium") ? ModItems.TITANIUM_PISTON.get() :
                        (be.engineMaterial.equals("aluminum") ? ModItems.ALUMINUM_PISTON.get() : Items.PISTON);

                if (heldItem.is(requiredPiston)) {
                    be.installedPistons++;
                    player.displayClientMessage(Component.literal("§6[ДВС] Установлено поршней: §a" + be.installedPistons + "/" + be.maxPistons), true);
                    return finishStep(player, heldItem, level, pos, SoundEvents.ANVIL_PLACE, be);
                } else {
                    String pistonName = be.engineMaterial.equals("titanium") ? "Титановый поршень" : (be.engineMaterial.equals("aluminum") ? "Алюминиевый поршень" : "Поршень");
                    player.displayClientMessage(Component.literal("§c[ДВС] Требуется " + pistonName + "! Прогресс: (" + be.installedPistons + "/" + be.maxPistons + ")"), true);
                    level.playSound(null, pos, com.simibubi.create.AllSoundEvents.DENY.getMainEvent(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.CONSUME;
                }
            }

            // =================================================================
            // ЭТАП 3: ГБЦ И УПРАВЛЕНИЕ (Работает на твоих новых флагах!)
            // =================================================================
            if (be.installedGBC < be.maxGBC) {
                // Определяем предметы ГБЦ в зависимости от материала и типа
                Item normalGBC = getNormalGbcItem(be);
                Item brainGBC = getBrainGbcItem(be);

                // Проверяем, является ли текущий клик финальным для ГБЦ
                boolean isFinalGbcClick = (be.installedGBC == be.maxGBC - 1);

                // Если у движка совмещенный контроллер (hasUnitedController), то на последнем шаге просим brainGBC, иначе всегда normalGBC
                Item targetGBC = (be.hasUnitedController && isFinalGbcClick) ? brainGBC : normalGBC;

                if (heldItem.is(targetGBC)) {
                    be.installedGBC++;
                    be.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                    if (!player.isCreative()) heldItem.shrink(1);
                    level.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.5f, 1.0f);

                    // Если это был совмещенный движок (I4, R32) и это последний клик — сразу собираем!
                    if (be.hasUnitedController && be.installedGBC == be.maxGBC) {
                        buildFinalEngine(be, level, pos, state, player);
                        return InteractionResult.CONSUME;
                    }

                    player.displayClientMessage(Component.literal("§a[ДВС] ГБЦ установлена! Прогресс: (" + be.installedGBC + "/" + be.maxGBC + ")"), true);
                    return InteractionResult.CONSUME;
                } else {
                    String missingName = (be.hasUnitedController && isFinalGbcClick) ? "ГБЦ с контроллером" : "Обычную ГБЦ";
                    player.displayClientMessage(Component.literal("§c[ДВС] Требуется " + missingName + "! Прогресс ГБЦ: (" + be.installedGBC + "/" + be.maxGBC + ")"), true);
                    level.playSound(null, pos, com.simibubi.create.AllSoundEvents.DENY.getMainEvent(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.CONSUME;
                }
            }

            // =================================================================
            // ЭТАП 4: ОТДЕЛЬНЫЙ КОНТРОЛЛЕР (Для W16 и V8, у которых нет совмещенной ГБЦ)
            // =================================================================
            if (!be.hasUnitedController && !be.installedBrain) {
                if (heldItem.is(ModItems.CONTROLLER.get())) { // Твой кастомный контроллер!
                    be.installedBrain = true;
                    if (!player.isCreative()) heldItem.shrink(1);
                    buildFinalEngine(be, level, pos, state, player);
                    return InteractionResult.CONSUME;
                } else {
                    player.displayClientMessage(Component.literal("§6[ДВС] Все ГБЦ на месте! Установите Контроллер двигателя для запуска!"), true);
                    level.playSound(null, pos, com.simibubi.create.AllSoundEvents.DENY.getMainEvent(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.CONSUME;
                }
            }
        }
        return net.minecraft.world.InteractionResult.PASS;
    }
    private void buildFinalEngine(EngineCarterBlockEntity be, Level level, BlockPos pos, BlockState state, Player player) {
        // Автоматическая генерация имени: например "titanium_w16_engine" или "iron_i4_engine" (для чугуна префикса нет)
        String prefix = be.engineMaterial.equals("iron") ? "" : be.engineMaterial + "_";
        String finalEngineName = prefix + be.engineType + "_engine";

        Block finalEngineBlock = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", finalEngineName)
        );

        if (finalEngineBlock != net.minecraft.world.level.block.Blocks.AIR) {
            // Меняем блок с сохранением поворота!
            level.setBlock(pos, finalEngineBlock.defaultBlockState().setValue(HORIZONTAL_FACING, state.getValue(HORIZONTAL_FACING)), 3);
            level.playSound(null, pos, SoundEvents.NETHERITE_BLOCK_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
            player.displayClientMessage(Component.literal("§a§l[ДВС] Двигатель " + finalEngineName.toUpperCase() + " успешно собран!"), true);
        } else {
            player.displayClientMessage(Component.literal("§c[ДВС] Ошибка регистрации: блок " + finalEngineName + " не найден в игре!"), true);
        }
    }

    private Item getNormalGbcItem(EngineCarterBlockEntity be) {
        boolean isW16 = be.engineType.equals("w16");
        if (be.engineMaterial.equals("titanium")) return isW16 ? ModItems.TITANIUM_W16_GBC.get() : ModItems.TITANIUM_GBC.get();
        if (be.engineMaterial.equals("aluminum")) return isW16 ? ModItems.ALUMINUM_W16_GBC.get() : ModItems.ALUMINUM_GBC.get();
        return Items.IRON_BLOCK; // Дефолт для чугуна
    }

    private Item getBrainGbcItem(EngineCarterBlockEntity be) {
        if (be.engineMaterial.equals("titanium")) return ModItems.TITANIUM_BRAIN_GBC.get();
        if (be.engineMaterial.equals("aluminum")) return ModItems.ALUMINUM_BRAIN_GBC.get();
        return ModItems.IRON_BRAIN_GBC.get();
    }
}