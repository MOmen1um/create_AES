package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.ruby.mod.create_additional_energy_sourses.EngineCarterBlockEntity;

public class EngineCarterBlock extends HorizontalKineticBlock implements IBE<EngineCarterBlockEntity> {

    public EngineCarterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<EngineCarterBlockEntity> getBlockEntityClass() {
        return EngineCarterBlockEntity.class;
    }

    @Override
    public net.minecraft.world.level.block.entity.BlockEntityType<? extends EngineCarterBlockEntity> getBlockEntityType() {
        // Подтягиваем наш изолированный тип сущности картеров из ModBlocks
        return ModBlocks.ENGINE_CARTER_ENTITY.get();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Оставляем анимированным, чтобы работал наш кастомный ModClientSetup вал!
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
    }

    // ХИТРЫЙ ПАССИВНЫЙ РЕЖИМ ВАЛА: Говорим Create, что у картера есть ось вращения!
    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        if (world.getBlockEntity(pos) instanceof EngineCarterBlockEntity be) {
            // Ось вращения "пробивается" наружу блока ТОЛЬКО если коленвал уже внутри установлен!
            return be.hasCrankshaft;
        }
        return false;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(
            net.minecraft.world.level.block.state.BlockState state,
            net.minecraft.world.level.Level level,
            net.minecraft.core.BlockPos pos,
            net.minecraft.world.entity.player.Player player,
            net.minecraft.world.phys.BlockHitResult hitResult) {

        if (level.isClientSide) return net.minecraft.world.InteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof EngineCarterBlockEntity be) {
            net.minecraft.world.item.ItemStack heldItem = player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND);

            // =========================================================================
            // СИСТЕМА ДИНАМИЧЕСКОГО РЕБАЛАНСА МАТЕРИАЛОВ (С ЗАГЛУШКАМИ)
            // =========================================================================

            // 1. НАСТРОЙКА ПОРШНЕЙ (titanium/aluminum)
            net.minecraft.world.item.Item requiredPiston = be.engineMaterial.equals("titanium") ? ModItems.TITANIUM_PISTON.get() :
                    (be.engineMaterial.equals("aluminum") ? ModItems.ALUMINUM_PISTON.get() : net.minecraft.world.item.Items.PISTON);
            String pistonName = be.engineMaterial.equals("titanium") ? "§bТитановый поршень" :
                    (be.engineMaterial.equals("aluminum") ? "§fАлюминиевый поршень" : "§7Поршень");

            // 2. НАСТРОЙКА ГБЦ ПО МАТЕРИАЛАМ И КОМПОНОВКАМ
            net.minecraft.world.item.Item requiredGBC = net.minecraft.world.item.Item.byBlock(net.minecraft.world.level.block.Blocks.IRON_BLOCK); // Дефолт
            // Проверяем, не w16 ли это, чтобы динамически изменить текст подсказки
            String gbcName = be.engineType.equals("w16") ? "§6Сдвоенную ГБЦ" : "§7ГБЦ (Железный блок)";

            if (be.engineMaterial.equals("titanium")) {
                requiredGBC = be.engineType.equals("w16") ? ModItems.TITANIUM_W16_GBC.get() : ModItems.TITANIUM_GBC.get();
                gbcName = be.engineType.equals("w16") ? "§bТитановую сдвоенную ГБЦ" : "§bТитановую ГБЦ";
            } else if (be.engineMaterial.equals("aluminum")) {
                requiredGBC = be.engineType.equals("w16") ? ModItems.ALUMINUM_W16_GBC.get() : ModItems.ALUMINUM_GBC.get();
                gbcName = be.engineType.equals("w16") ? "§fАлюминиевую сдвоенную ГБЦ" : "§fАлюминиевую ГБЦ";
            } else if (be.engineMaterial.equals("iron")) {
                //requiredGBC = be.engineType.equals("w16") ? ModItems.IRON_W16_GBC.get() : ...
            }

            // 3. НАСТРОЙКА КОНТРОЛЛЕРОВ / ГБЦ С МОЗГАМИ
            net.minecraft.world.item.Item requiredBrainGBC = net.minecraft.world.item.Items.GOLD_INGOT; // Временная заглушка (Золото)
            String brainGbcName = be.engineType.equals("w16") ? "§eСдвоенную ГБЦ с контроллером" : "§eКонтроллер двигателя";

            if (be.engineMaterial.equals("titanium")) {
                requiredBrainGBC = ModItems.TITANIUM_BRAIN_GBC.get();
                brainGbcName = be.engineType.equals("w16") ? "§dТитановую сдвоенную ГБЦ с контроллером" : "§dТитановую ГБЦ с контроллером";
            } else if (be.engineMaterial.equals("iron")) {
                requiredBrainGBC = ModItems.IRON_BRAIN_GBC.get();
                brainGbcName = be.engineType.equals("w16") ? "§6Чугунную cдвоенную ГБЦ с контроллером" : "§eЧугунную ГБЦ с контроллером";
            } else if (be.engineMaterial.equals("aluminum")) {
                requiredBrainGBC = ModItems.ALUMINUM_BRAIN_GBC.get();
                brainGbcName = be.engineType.equals("w16") ? "§7Алюминиевую сдвоенную ГБЦ с контроллером" : "§7Алюминиевую ГБЦ с контроллером";
            }


            // =========================================================================
            // ЦЕПОЧКА СБОРКИ ДВИГАТЕЛЯ
            // =========================================================================

            // Шаг 1: Коленвал
            if (!be.hasCrankshaft && heldItem.is(net.minecraft.world.item.Items.IRON_INGOT)) {
                be.hasCrankshaft = true;
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§6[ДВС] §aКоленчатый вал успешно установлен!"), true);
                return finishStep(player, heldItem, level, pos, net.minecraft.sounds.SoundEvents.ANVIL_PLACE, be);
            }

            // Шаг 2: Поршни (Автоматически подставит нужный металл!)
            if (be.hasCrankshaft && be.installedPistons < be.maxPistons && heldItem.is(requiredPiston)) {
                be.installedPistons++;
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§6[ДВС] §eУстановлено поршней: §a" + be.installedPistons + "§7/§a" + be.maxPistons), true);
                return finishStep(player, heldItem, level, pos, net.minecraft.sounds.SoundEvents.ANVIL_PLACE, be);
            }

            // --- Шаг 3: ГБЦ и Контроллеры ---
            if (be.installedPistons == be.maxPistons) {

                // Ветка для многоцилиндровых двигателей (W16 и V8)
                if (be.engineType.equals("w16") || be.engineType.equals("v8")) {

                    // Этап А: Сначала устанавливаем ГБЦ до максимума
                    if (be.installedGBC < be.maxGBC) {
                        if (heldItem.is(requiredGBC)) {
                            be.installedGBC++;
                            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ANVIL_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 0.5f, 1.0f);
                            if (!player.isCreative()) heldItem.shrink(1);
                            be.setChanged();
                            level.sendBlockUpdated(pos, state, state, 3);

                            // Сразу показываем прогресс после успешной установки
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§6[ДВС] §eГБЦ установлена! Прогресс: §a(" + be.installedGBC + "/" + be.maxGBC + ")"), true);
                        } else {
                            // Подсказка, если игрок тыкает не тем предметом
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§6[ДВС] §eТребуется ГБЦ, Установлено: §a(" + be.installedGBC + "/" + be.maxGBC + ")"), true);
                            level.playSound(null, pos, com.simibubi.create.AllSoundEvents.DENY.getMainEvent(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                        }
                    }
                    // Этап Б: Все ГБЦ на месте, теперь ставим контроллер (мозги)
                    else if (!be.installedBrain) {
                        // Временный технологичный предмет вместо золота
                        net.minecraft.world.item.Item temporaryBrainItem = ModItems.CONTROLLER.get();

                        if (heldItem.is(temporaryBrainItem)) {
                            be.installedBrain = true;
                            if (!player.isCreative()) heldItem.shrink(1);

                            // Финал сборки
                            String finalEngineName = be.engineMaterial + "_" + be.engineType + "_engine";
                            net.minecraft.world.level.block.Block finalEngineBlock = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(
                                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", finalEngineName)
                            );

                            if (finalEngineBlock != net.minecraft.world.level.block.Blocks.AIR) {
                                level.setBlock(pos, finalEngineBlock.defaultBlockState(), 3);
                                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.NETHERITE_BLOCK_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§aДвигатель " + be.engineType.toUpperCase() + " успешно собран!"), true);
                            } else {
                                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cОшибка: Блок " + finalEngineName + " не зарегистрирован!"), true);
                            }
                        } else {
                            // Подсказка о том, что ГБЦ уже готовы и нужен контроллер
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§6[ДВС] §aТребуется контролер!"), true);
                            level.playSound(null, pos, com.simibubi.create.AllSoundEvents.DENY.getMainEvent(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                        }
                    }
                }
                // Старая ветка для I4 и R32
                else {
                    if (be.installedGBC < be.maxGBC) {
                        boolean isFinalClick = (be.installedGBC == be.maxGBC - 1);
                        boolean needsBrain = isFinalClick && be.requiredBrain;

                        net.minecraft.world.item.Item targetItemForThisStep = needsBrain ? requiredBrainGBC : requiredGBC;

                        if (heldItem.is(targetItemForThisStep)) {
                            be.installedGBC++;
                            if (!player.isCreative()) heldItem.shrink(1);

                            if (be.installedGBC == be.maxGBC) {
                                String finalEngineName = be.engineMaterial + "_" + be.engineType + "_engine";
                                net.minecraft.world.level.block.Block finalEngineBlock = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(
                                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("create_additional_energy_sourses", finalEngineName)
                                );
                                if (finalEngineBlock != net.minecraft.world.level.block.Blocks.AIR) {
                                    level.setBlock(pos, finalEngineBlock.defaultBlockState(), 3);
                                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ANVIL_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 0.5f, 1.0f);
                                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§aДвигатель " + be.engineType.toUpperCase() + " успешно собран!"), true);
                                }
                            } else {
                                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§aГБЦ установлена! Прогресс: (" + be.installedGBC + "/" + be.maxGBC + ")"), true);
                            }
                            be.setChanged();
                            level.sendBlockUpdated(pos, state, state, 3);
                        } else {
                            String missingItemName = needsBrain ? "ГБЦ с контроллером" : "ГБЦ";
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cТребуется " + missingItemName + "! Прогресс: (" + be.installedGBC + "/" + be.maxGBC + ")"), true);
                        }
                    }
                }

                return net.minecraft.world.InteractionResult.CONSUME;
            }

            // =========================================================================
            // СИСТЕМА ДИАГНОСТИКИ ОШИБОК И ЗВУК "DENY" CREATE
            // =========================================================================
            level.playSound(null, pos, com.simibubi.create.AllSoundEvents.DENY.getMainEvent(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);

            if (!be.hasCrankshaft) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c⚠ Установите §6Коленчатый вал §7(Железный слиток)"), true);
            } else if (be.installedPistons < be.maxPistons) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c⚠ Требуется: " + pistonName + " §7(" + be.installedPistons + "/" + be.maxPistons + ")"), true);
            } else if (be.installedGBC < be.maxGBC) {
                boolean isFinalBrainStep = (be.installedGBC == be.maxGBC - 1) && (be.engineType.equals("r32") || be.engineType.equals("w16") || be.engineType.equals("v8"));
                String expectedGbc = isFinalBrainStep ? brainGbcName : gbcName;

                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c⚠ Требуется: " + expectedGbc + " §7(" + be.installedGBC + "/" + be.maxGBC + ")"), true);
            }

            return net.minecraft.world.InteractionResult.CONSUME;
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    private InteractionResult finishStep(Player player, ItemStack heldItem, Level level, BlockPos pos, net.minecraft.sounds.SoundEvent sound, EngineCarterBlockEntity be) {
        if (!player.isCreative()) heldItem.shrink(1);
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0f, 1.0f);
        be.setChanged();
        level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
        return InteractionResult.CONSUME;
    }
}
