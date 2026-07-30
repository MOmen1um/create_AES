package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EngineAssemblyManager {
    public boolean hasCrankshaft = false;
    public int installedPistons = 0;
    public int installedGBC = 0;
    public boolean hasForgottenPart = false;
    public boolean hasTurbo = false;

    // Ссылка на родительский класс сущности (будет работать и для V8, и для всех детей!)
    private final V8EngineBlockEntity engine;

    // ИСПРАВЛЕННЫЙ КОНСТРУКТОР: принимает базовое ядро ДВС
    public EngineAssemblyManager(V8EngineBlockEntity engine) {
        this.engine = engine;
    }

    // Динамический перебор: поршни берутся из живой переменной ДВС прямо во время клика!
    public int getMaxPistons() {
        return this.engine.pistonCount > 0 ? this.engine.pistonCount : 8; // Защита: если в детях еще не определен, ставим 8
    }

    public int getMaxGBC() {
        // Логика из твоей мысли: для R32 (32 поршня) - 1 общая голова, для остальных - по две!
        return getMaxPistons() == 32 ? 1 : 2;
    }

    public void writeNBT(CompoundTag tag) {
        tag.putBoolean("HasCrankshaft", hasCrankshaft);
        tag.putInt("InstalledPistons", installedPistons);
        tag.putInt("InstalledGBC", installedGBC);
        tag.putBoolean("HasForgottenPart", hasForgottenPart);
        tag.putBoolean("HasTurbo", hasTurbo);
    }

    public void readNBT(CompoundTag tag) {
        this.hasCrankshaft = tag.getBoolean("HasCrankshaft");
        this.installedPistons = tag.getInt("InstalledPistons");
        this.installedGBC = tag.getInt("InstalledGBC");
        this.hasForgottenPart = tag.getBoolean("HasForgottenPart");
        this.hasTurbo = tag.getBoolean("HasTurbo");
    }

    public InteractionResult handleInteraction(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, Runnable onChanged) {
        ItemStack heldItem = player.getItemInHand(hand);

        // Инструмент разборки (наш будущий разборщик)
        if (heldItem.is(Items.WARPED_FUNGUS_ON_A_STICK)) {
            return handleDisassembly(level, pos, player, onChanged);
        }

        // 1. Ставим коленвал
        if (!hasCrankshaft && heldItem.is(Items.IRON_INGOT)) {
            this.hasCrankshaft = true;
            return finishStep(player, heldItem, SoundEvents.ANVIL_PLACE, onChanged);
        }

        // 2. Ставим поршни по одному (Лимит подтягивается динамически: 4, 8, 16, 32!)
        if (hasCrankshaft && installedPistons < getMaxPistons() && heldItem.is(Items.PISTON)) {
            this.installedPistons++;
            player.displayClientMessage(Component.literal("Установлено поршней: " + installedPistons + "/" + getMaxPistons()), true);
            return finishStep(player, heldItem, SoundEvents.ARMOR_EQUIP_IRON.value(), onChanged);
        }

        // 3. Ставим ГБЦ
        if (installedPistons == getMaxPistons() && installedGBC < getMaxGBC() && heldItem.is(Items.IRON_BLOCK)) {
            this.installedGBC++;
            return finishStep(player, heldItem, SoundEvents.COPPER_PLACE, onChanged);
        }

        // 4. Латунная forgottenPart (Финальный клик сборки)
        if (installedGBC == getMaxGBC() && !hasForgottenPart && heldItem.is(Items.GOLD_INGOT)) { // ЗАМЕНИ на forgottenPart
            this.hasForgottenPart = true;

            // МАГИЯ СЛИЯНИЯ: Проверяем, если это картер, меняем его на рабочий двигатель из креатива
            // Вытаскивает чистый текстовый ID (например, "create_additional_energy_sourses:titanium_r32_carter")
            String blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().toLowerCase();
            if (blockId.contains("carter")) {
                // Сохраняем направление вращения и сторону блока
                net.minecraft.core.Direction facing = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);

                // Проверяем, какой именно картер стоит, и ставим соответствующий собранный движок
                net.minecraft.world.level.block.Block finalEngine = ModBlocks.TITANIUM_R32.get(); // По умолчанию R32
                if (blockId.contains("v8")) {
                    finalEngine = ModBlocks.IRON_V8.get(); // Если V8, то ставим V8
                }

                // Ставим блок готового двигателя с сохранением поворота в мире!
                level.setBlock(pos, finalEngine.defaultBlockState().setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING, facing), 3);
            }

            return finishStep(player, heldItem, SoundEvents.NOTE_BLOCK_CHIME.value(), onChanged);
        }

        // 5. Установка Турбонаддува из Create Diesel Generators!
        net.minecraft.resources.ResourceLocation turboId = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("createdieselgenerators", "engine_turbocharger");
        net.minecraft.world.item.Item turboItem = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(turboId);

        if (turboItem != null && heldItem.is(turboItem)) {
            if (!hasForgottenPart) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("Невозможно установить турбо без латунного контроллера!"), true);
                return InteractionResult.FAIL;
            }
            if (!hasTurbo) {
                this.hasTurbo = true;
                // Передаем флаг надува в ядро двигателя (то, что было v8.isTurboCharged = true)
                this.engine.isTurboCharged = true;

                // Воспроизводим твой сочный звук закрытия поршней из Create!
                level.playSound(null,
                        pos,
                        net.minecraft.sounds.SoundEvents.IRON_TRAPDOOR_CLOSE,
                        net.minecraft.sounds.SoundSource.BLOCKS,
                        1.0f,
                        1.2f);

                onChanged.run();
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    private InteractionResult handleDisassembly(Level level, BlockPos pos, Player player, Runnable onChanged) {
        if (hasTurbo) { hasTurbo = false; giveItem(player, Items.DIAMOND); return finishDisassembly(onChanged); }
        if (hasForgottenPart) { hasForgottenPart = false; giveItem(player, Items.GOLD_INGOT); return finishDisassembly(onChanged); }
        if (installedGBC > 0) { installedGBC--; giveItem(player, Items.IRON_BLOCK); return finishDisassembly(onChanged); }
        if (installedPistons > 0) { installedPistons--; giveItem(player, Items.PISTON); return finishDisassembly(onChanged); }
        if (hasCrankshaft) { hasCrankshaft = false; giveItem(player, Items.IRON_INGOT); return finishDisassembly(onChanged); }
        return InteractionResult.PASS;
    }

    private InteractionResult finishStep(Player player, ItemStack heldItem, net.minecraft.sounds.SoundEvent sound, Runnable onChanged) {
        if (!player.isCreative()) heldItem.shrink(1);
        onChanged.run();
        return InteractionResult.CONSUME;
    }

    private InteractionResult finishDisassembly(Runnable onChanged) {
        onChanged.run();
        return InteractionResult.CONSUME;
    }

    private void giveItem(Player player, net.minecraft.world.level.ItemLike item) {
        ItemStack stack = new ItemStack(item);
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }
}