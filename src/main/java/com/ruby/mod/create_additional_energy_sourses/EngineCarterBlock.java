package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

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

            // 1. НАСТРОЙКА ПОРШНЕЙ ПО МАТЕРИАЛАМ
            net.minecraft.world.item.Item requiredPiston = net.minecraft.world.item.Items.PISTON; // Дефолт (Чугун/Железо)
            String pistonName = "§7Поршень";

            if (be.engineMaterial.equals("titanium")) {
                // Измени на свой предмет, когда зарегистрируешь:
                // requiredPiston = ModItems.TITANIUM_PISTON.get();
                pistonName = "§bТитановый поршень";
            } else if (be.engineMaterial.equals("aluminum")) {
                // Измени на свой предмет, когда зарегистрируешь:
                // requiredPiston = ModItems.ALUMINUM_PISTON.get();
                pistonName = "§fАлюминиевый поршень";
            }

            // 2. НАСТРОЙКА ГБЦ ПО МАТЕРИАЛАМ И КОМПОНОВКАМ
            net.minecraft.world.item.Item requiredGBC = net.minecraft.world.item.Item.byBlock(net.minecraft.world.level.block.Blocks.IRON_BLOCK); // Дефолт
            // Проверяем, не R16 ли это, чтобы динамически изменить текст подсказки
            String gbcName = be.engineType.equals("r16") ? "§6Сдвоенную ГБЦ" : "§7ГБЦ (Железный блок)";

            if (be.engineMaterial.equals("titanium")) {
                // requiredGBC = be.engineType.equals("r16") ? ModItems.TITANIUM_R16_GBC.get() : ModItems.TITANIUM_GBC.get();
                gbcName = be.engineType.equals("r16") ? "§bТитановую сдвоенную ГБЦ" : "§bТитановую ГБЦ";
            } else if (be.engineMaterial.equals("aluminum")) {
                // requiredGBC = be.engineType.equals("r16") ? ModItems.ALUMINUM_R16_GBC.get() : ModItems.ALUMINUM_GBC.get();
                gbcName = be.engineType.equals("r16") ? "§fАлюминиевую сдвоенную ГБЦ" : "§fАлюминиевую ГБЦ";
            } else if (be.engineMaterial.equals("iron")) {
                // requiredGBC = be.engineType.equals("r16") ? ModItems.IRON_R16_GBC.get() : ...
            }

            // 3. НАСТРОЙКА КОНТРОЛЛЕРОВ / ГБЦ С МОЗГАМИ
            net.minecraft.world.item.Item requiredBrainGBC = net.minecraft.world.item.Items.GOLD_INGOT; // Временная заглушка (Золото)
            String brainGbcName = be.engineType.equals("r16") ? "§eСдвоенную ГБЦ с контроллером" : "§eКонтроллер двигателя";

            if (be.engineMaterial.equals("titanium")) {
                // requiredBrainGBC = be.engineType.equals("r16") ? ModItems.TITANIUM_R16_BRAIN_GBC.get() : ModItems.TITANIUM_BRAIN_GBC.get();
                brainGbcName = be.engineType.equals("r16") ? "§dТитановую сдвоенную ГБЦ с контроллером" : "§dТитановую ГБЦ с контроллером";
            } else if (be.engineMaterial.equals("iron")) {
                // requiredBrainGBC = be.engineType.equals("r16") ? ModItems.IRON_R16_BRAIN_GBC.get() : ModItems.IRON_BRAIN_GBC.get();
                brainGbcName = be.engineType.equals("r16") ? "§6Чугунную sдвоенную ГБЦ с контроллером" : "§eЧугунную ГБЦ с контроллером";
            } else if (be.engineMaterial.equals("aluminum")) {
                // requiredBrainGBC = be.engineType.equals("r16") ? ModItems.ALUMINUM_R16_BRAIN_GBC.get() : ModItems.ALUMINUM_BRAIN_GBC.get();
                brainGbcName = be.engineType.equals("r16") ? "§7Алюминиевую сдвоенную ГБЦ с контроллером" : "§7Алюминиевую ГБЦ с контроллером";
            }


            // =========================================================================
            // ЦЕПОЧКА СБОРКИ ДВИГАТЕЛЯ
            // =========================================================================

            // 1. НАСТРОЙКА ПОРШНЕЙ ПО МАТЕРИАЛАМ (БЭКЭНД ГОТОВ)
            net.minecraft.world.item.Item requiredPiston = net.minecraft.world.item.Items.PISTON; // Дефолт (Чугун)
            String pistonName = "§7Поршень";

            if (be.engineMaterial.equals("titanium")) {
                requiredPiston = ModItems.TITANIUM_PISTON.get(); // СТЕРЛИ net.minecraft.world.item.Item
                pistonName = "§bТитановый поршень";               // СТЕРЛИ String
            } else if (be.engineMaterial.equals("aluminum")) {
                requiredPiston = ModItems.ALUMINUM_PISTON.get(); // СТЕРЛИ net.minecraft.world.item.Item
                pistonName = "§fАлюминиевый поршень";             // СТЕРЛИ String
            }

            // 2. НАСТРОЙКА ГБЦ ПО МАТЕРИАЛАМ И КОМПОНОВКАМ
            net.minecraft.world.item.Item requiredGBC = net.minecraft.world.item.Item.byBlock(net.minecraft.world.level.block.Blocks.IRON_BLOCK); // Дефолт (Чугун)
            String gbcName = be.engineType.equals("r16") ? "§6Сдвоенную ГБЦ" : "§7ГБЦ (Железный блок)";

            if (be.engineMaterial.equals("titanium")) {
                requiredGBC = be.engineType.equals("r16") ? ModItems.TITANIUM_R16_GBC.get() : ModItems.TITANIUM_GBC.get();
                gbcName = be.engineType.equals("r16") ? "§bТитановую сдвоенную ГБЦ" : "§bТитановую ГБЦ";
            } else if (be.engineMaterial.equals("aluminum")) {
                requiredGBC = be.engineType.equals("r16") ? ModItems.ALUMINUM_R16_GBC.get() : ModItems.ALUMINUM_GBC.get();
                gbcName = be.engineType.equals("r16") ? "§fАлюминиевую сдвоенную ГБЦ" : "§fАлюминиевую ГБЦ";
            } else if (be.engineMaterial.equals("iron")) {
                if (be.engineType.equals("r16")) {
                    requiredGBC = ModItems.IRON_R16_GBC.get();
                }
            }

            // 3. НАСТРОЙКА КОНТРОЛЛЕРОВ / ГБЦ С МОЗГАМИ
            net.minecraft.world.item.Item requiredBrainGBC = ModItems.IRON_BRAIN_GBC.get(); // Дефолт (Чугун)
            String brainGbcName = be.engineType.equals("r16") ? "§6Чугунную сдвоенную ГБЦ с контроллером" : "§eЧугунную ГБЦ с контроллером";

            if (be.engineMaterial.equals("titanium")) {
                requiredBrainGBC = be.engineType.equals("r16") ? ModItems.TITANIUM_R16_BRAIN_GBC.get() : ModItems.TITANIUM_BRAIN_GBC.get();
                brainGbcName = be.engineType.equals("r16") ? "§dTитановую сдвоенную ГБЦ с контроллером" : "§dТитановую ГБЦ с контроллером";
            } else if (be.engineMaterial.equals("aluminum")) {
                requiredBrainGBC = be.engineType.equals("r16") ? ModItems.ALUMINUM_R16_BRAIN_GBC.get() : ModItems.ALUMINUM_BRAIN_GBC.get();
                brainGbcName = be.engineType.equals("r16") ? "§7Алюминиевую сдвоенную ГБЦ с контроллером" : "§7Алюминиевую ГБЦ с контроллером";
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
                boolean isFinalBrainStep = (be.installedGBC == be.maxGBC - 1) && (be.engineType.equals("r32") || be.engineType.equals("r16") || be.engineType.equals("v8"));
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
