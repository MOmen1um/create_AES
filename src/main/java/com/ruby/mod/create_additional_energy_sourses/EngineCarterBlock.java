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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class EngineCarterBlock extends HorizontalKineticBlock implements IBE<EngineCarterBlockEntity> {

    // Правильный конструктор для блоков Create
    public EngineCarterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return IBE.super.newBlockEntity(pos, state);
    }

    @Override
    public net.minecraft.core.Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    // 3. Передаем логику открытия/удаления блока нашей сущности
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            level.removeBlockEntity(pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
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

        if (level.getBlockEntity(pos) instanceof EngineCarterBlockEntity be) {
            ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

            // =================================================================
            // ЭТАП 1: КОЛЕНВАЛ (Для всех движков одинаково)
            // =================================================================
            if (!be.hasCrankshaft) {
                if (heldItem.is(getRightCrackshaft(be))) {
                    if (!level.isClientSide) {
                        be.hasCrankshaft = true;
                    }
                    player.displayClientMessage(Component.literal("§6[ДВС] Коленчатый вал успешно установлен!"), true);
                    return finishStep(player, heldItem, level, pos, SoundEvents.ANVIL_PLACE, be);
                } else {
                    String crankshaftName = be.engineMaterial.equals("titanium") ? "§dТитановый коленчатый вал" : (be.engineMaterial.equals("aluminum") ? "§dАлюминиевый коленчатый вал" : "§dКоленчатый вал");
                    player.displayClientMessage(Component.literal("§c[ДВС] Сначала установите " + crankshaftName), true);
                    level.playSound(null, pos, com.simibubi.create.AllSoundEvents.DENY.getMainEvent(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.CONSUME;
                }
            }

            // =================================================================
            // ЭТАП 2: ПОРШНИ (Автоматически подтягивают нужный металл)
            // =================================================================
            if (be.installedPistons < be.maxPistons) {
                Item requiredPiston = be.engineMaterial.equals("titanium") ? ModItems.TITANIUM_PISTON.get() :
                        (be.engineMaterial.equals("aluminum") ? ModItems.ALUMINUM_PISTON.get() : ModItems.CAST_IRON_PISTON.get());

                if (heldItem.is(requiredPiston)) {
                    if (!level.isClientSide) {
                        be.installedPistons++;
                    }
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
                    if (!level.isClientSide) {
                        be.installedGBC++;
                        be.setChanged();
                        level.sendBlockUpdated(pos, state, state, 3);
                        if (!player.isCreative()) heldItem.shrink(1);
                        level.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.5f, 1.0f);
                    }

                    // Если это был совмещенный движок (I4, R32) и это последний клик — сразу собираем!
                    if (be.hasUnitedController && be.installedGBC == be.maxGBC) {
                        buildFinalEngine(be, level, pos, state, player);
                        return InteractionResult.CONSUME;
                    }

                    player.displayClientMessage(Component.literal("§a[ДВС] ГБЦ установлена! Прогресс: (" + be.installedGBC + "/" + be.maxGBC + ")"), true);
                    return InteractionResult.CONSUME;
                } else {
                    boolean isW16 = be.engineType.equals("w16");
                    String missingName = (be.hasUnitedController && isFinalGbcClick) ? "ГБЦ с контроллером" : (isW16 ? "Гбц W16" : "Обычную ГБЦ");
                    player.displayClientMessage(Component.literal("§c[ДВС] Требуется " + missingName + "! Прогресс ГБЦ: (" + be.installedGBC + "/" + be.maxGBC + ")"), true);
                    level.playSound(null, pos, com.simibubi.create.AllSoundEvents.DENY.getMainEvent(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.CONSUME.sidedSuccess(level.isClientSide);
                }
            }

            // =================================================================
            // ЭТАП 4: ОТДЕЛЬНЫЙ КОНТРОЛЛЕР (Для W16 и V8, у которых нет совмещенной ГБЦ)
            // =================================================================
            if (!be.hasUnitedController && !be.installedBrain) {
                // Логика разветвляется в зависимости от типа двигателя
                if (be.isModular) {
                    // МОДУЛЬНЫЙ: Контроллер или Ключ Create
                    if (heldItem.is(ModItems.CONTROLLER.get())) {
                        be.installedBrain = true;
                        if (!player.isCreative()) heldItem.shrink(1);
                        buildModularEngine(be, level, pos, state, player, true);
                        return InteractionResult.CONSUME;
                    } else if (heldItem.is(net.minecraft.world.item.Items.IRON_INGOT)) {
                        buildModularEngine(be, level, pos, state, player, false);
                        return InteractionResult.CONSUME;
                    } else {
                        player.displayClientMessage(Component.literal("§eУстановите Контроллер или §bнажмите Ключом"), true);
                        return InteractionResult.CONSUME;
                    }
                } else {
                    // КЛАССИЧЕСКИЙ: Только контроллер
                    if (heldItem.is(ModItems.CONTROLLER.get())) {
                        be.installedBrain = true;
                        if (!player.isCreative()) heldItem.shrink(1);
                        buildFinalEngine(be, level, pos, state, player);
                        return InteractionResult.CONSUME;
                    } else {
                        player.displayClientMessage(Component.literal("§6Установите Контроллер!"), true);
                        return InteractionResult.CONSUME;
                    }
                }

            }
        }
        return net.minecraft.world.InteractionResult.PASS;
    }
    @Override
    public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // Логика подключения валов Create к нашему картеру
    @Override
    public boolean hasShaftTowards(net.minecraft.world.level.LevelReader world, BlockPos pos, BlockState state, net.minecraft.core.Direction face) {
        // 1. Ищем наш BlockEntity
        if (world.getBlockEntity(pos) instanceof EngineCarterBlockEntity be) {
            // 2. Если коленвала еще нет внутри — подключать кинетику Create ЗАПРЕЩЕНО
            if (!be.hasCrankshaft) return false;

            // 3. Получаем направление, куда смотрит сам картер
            net.minecraft.core.Direction blockFacing = state.getValue(HORIZONTAL_FACING);

            // 4. Разрешаем подключение только сзади (блок смотрит вперед, значит зад — это Opposite)
            // Если твоя моделька сместилась, и вал должен быть спереди, просто замени blockFacing.getOpposite() на blockFacing
            return face == blockFacing.getOpposite();
        }
        return false;
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
            level.setBlock(pos, finalEngineBlock.defaultBlockState().setValue(HORIZONTAL_FACING, state.getValue(HORIZONTAL_FACING).getOpposite()), 3);
            level.playSound(null, pos, SoundEvents.NETHERITE_BLOCK_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
            player.displayClientMessage(Component.literal("§a§l[ДВС] Двигатель " + finalEngineName.toUpperCase() + " успешно собран!"), true);
        } else {
            player.displayClientMessage(Component.literal("§c[ДВС] Ошибка регистрации: блок " + finalEngineName + " не найден в игре!"), true);
        }
    }

    private void buildModularEngine(EngineCarterBlockEntity be, Level level, BlockPos pos, BlockState state, Player player, boolean isMainController) {
        // 1. Формируем префикс материала (для чугуна префикса нет)
        String materialPrefix = be.engineMaterial.equals("iron") ? "" : be.engineMaterial + "_";

        // 2. Выбираем суффикс в зависимости от выбора игрока (Контроллер или Модуль)
        String suffix = isMainController ? "_controlling_engine" : "_modular_engine";

        // 3. Собираем полный ID блока
        String finalBlockName = materialPrefix + be.engineType + suffix;

        Block finalEngineBlock = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", finalBlockName)
        );

        if (finalEngineBlock != net.minecraft.world.level.block.Blocks.AIR) {
            // 4. Подменяем блок в мире с сохранением направления [index:0.1.2]
            level.setBlock(pos, finalEngineBlock.defaultBlockState().setValue(HORIZONTAL_FACING, state.getValue(HORIZONTAL_FACING)), 3);
            level.playSound(null, pos, SoundEvents.NETHERITE_BLOCK_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);

            // 5. Выводим красивое сообщение в зависимости от сборки [index:0.1.2]
            String msg = isMainController ? "§a§l[ДВС] Главный двигатель собран!" : "§b§l[ДВС] Дополнительный модуль добавлен!";
            player.displayClientMessage(Component.literal(msg), true);

            // 6. МГНОВЕННЫЙ ПЕРЕРАСЧЕТ СЕТИ ПОСЛЕ КЛИКА
            // Если поставили контроллер — заставляем его сразу просканировать линию картеров сзади
            if (level.getBlockEntity(pos) instanceof ControllingModularEnginesBlockEntity cmebe) {
                cmebe.calculateConfiguration();
            }
            // If поставили модуль — ищем главный контроллер впереди и заставляем его пересчитать SU
            else if (level.getBlockEntity(pos) instanceof ModularEnginesBlockEntity mebe) {
                ControllingModularEnginesBlockEntity master = mebe.findMasterController();
                if (master != null) {
                    master.calculateConfiguration();
                }
            }
        } else {
            player.displayClientMessage(Component.literal("§c[ДВС] Ошибка: блок " + finalBlockName + " не зарегистрирован!"), true);
        }
    }


    private Item getRightCrackshaft(EngineCarterBlockEntity be) {
        if (be.engineMaterial.equals("titanium")) return ModItems.TITANIUM_CRANKSHAFT.get();
        if (be.engineMaterial.equals("aluminum")) return ModItems.ALUMINUM_CRANKSHAFT.get();
        return ModItems.CAST_IRON_CRANKSHAFT.get();
    }

    private Item getNormalGbcItem(EngineCarterBlockEntity be) {
        boolean isW16 = be.engineType.equals("w16");
        if (be.engineMaterial.equals("titanium")) return isW16 ? ModItems.TITANIUM_W16_GBC.get() : ModItems.TITANIUM_GBC.get();
        if (be.engineMaterial.equals("aluminum")) return isW16 ? ModItems.ALUMINUM_W16_GBC.get() : ModItems.ALUMINUM_GBC.get();
        return isW16 ? ModItems.CAST_IRON_W16_GBC.get() : ModItems.CAST_IRON_GBC.get();
    }

    private Item getBrainGbcItem(EngineCarterBlockEntity be) {
        if (be.engineMaterial.equals("titanium")) return ModItems.TITANIUM_BRAIN_GBC.get();
        if (be.engineMaterial.equals("aluminum")) return ModItems.ALUMINUM_BRAIN_GBC.get();
        return ModItems.CAST_IRON_BRAIN_GBC.get();
    }
}