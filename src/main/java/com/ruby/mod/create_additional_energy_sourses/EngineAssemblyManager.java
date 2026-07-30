package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EngineAssemblyManager {

    // Все переменные сборки инкапсулированы внутри менеджера
    public boolean hasCrankshaft = false;
    public int installedPistons = 0;
    public int installedGBC = 0;
    public boolean hasForgottenPart = false;
    public boolean hasTurbo = false;

    // Конкретные лимиты для этой сборки
    private final int maxPistons;
    private final int maxGBC;

    public EngineAssemblyManager(int maxPistons, int maxGBC) {
        this.maxPistons = maxPistons;
        this.maxGBC = maxGBC;
    }

    // --- ИНКАПСУЛЯЦИЯ СХРАНЕНИЯ (NBT) ---
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

    // --- ИНКАПСУЛЯЦИЯ КЛИКОВ (СБОРКА / РАЗБОРКА) ---
    public InteractionResult handleInteraction(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, Runnable onChanged) {
        ItemStack heldItem = player.getItemInHand(hand);

        // Клик специальным инструментом — РАЗБОРКА в обратном порядке
        if (heldItem.is(Items.WARPED_FUNGUS_ON_A_STICK)) { // ЗАМЕНИ на свой спецключ!
            return handleDisassembly(level, pos, player, onChanged);
        }

        // Обычный клик деталями — СБОРКА по цепочке

        // 1. Коленвал
        if (!hasCrankshaft && heldItem.is(Items.IRON_INGOT)) { // ЗАМЕНИ на свой коленвал
            this.hasCrankshaft = true;
            return finishStep(player, heldItem, SoundEvents.ANVIL_PLACE, 1.0f, onChanged);
        }

        // 2. Поршни по одному
        if (hasCrankshaft && installedPistons < maxPistons && heldItem.is(Items.PISTON)) {
            this.installedPistons++;
            player.displayClientMessage(Component.literal("Установлено поршней: " + installedPistons + "/" + maxPistons), true);
            return finishStep(player, heldItem, SoundEvents.ARMOR_EQUIP_IRON.value(), 1.0f, onChanged);
        }

        // 3. ГБЦ
        if (installedPistons == maxPistons && installedGBC < maxGBC && heldItem.is(Items.IRON_BLOCK)) { // ЗАМЕНИ на ГБЦ
            this.installedGBC++;
            return finishStep(player, heldItem, SoundEvents.COPPER_PLACE, 1.0f, onChanged);
        }

        // 4. Латунная forgottenPart
        if (installedGBC == maxGBC && !hasForgottenPart && heldItem.is(Items.GOLD_INGOT)) { // ЗАМЕНИ на forgottenPart
            this.hasForgottenPart = true;
            return finishStep(player, heldItem, SoundEvents.NOTE_BLOCK_CHIME.value(), 1.0f, onChanged);
        }

        // 5. Турбо (Блокируется без латунной детали!)
        if (heldItem.is(Items.DIAMOND)) { // ЗАМЕНИ на Турбину
            if (!hasForgottenPart) {
                player.displayClientMessage(Component.literal("Невозможно поставить турбо без латунного контроллера!"), true);
                return InteractionResult.FAIL;
            }
            if (!hasTurbo) {
                this.hasTurbo = true;
                return finishStep(player, heldItem, SoundEvents.BEACON_ACTIVATE, 1.0f, onChanged);
            }
        }

        return InteractionResult.PASS;
    }

    private InteractionResult handleDisassembly(Level level, BlockPos pos, Player player, Runnable onChanged) {
        if (hasTurbo) {
            hasTurbo = false;
            giveItemBack(player, new ItemStack(Items.DIAMOND)); // Вернуть турбину
            return finishDisassemblyStep(level, pos, SoundEvents.BEACON_DEACTIVATE, onChanged);
        }
        if (hasForgottenPart) {
            hasForgottenPart = false;
            giveItemBack(player, new ItemStack(Items.GOLD_INGOT)); // Вернуть forgottenPart
            return finishDisassemblyStep(level, pos, SoundEvents.NOTE_BLOCK_BASS.value(), onChanged);
        }
        if (installedGBC > 0) {
            installedGBC--;
            giveItemBack(player, new ItemStack(Items.IRON_BLOCK)); // Вернуть ГБЦ
            return finishDisassemblyStep(level, pos, SoundEvents.COPPER_BREAK, onChanged);
        }
        if (installedPistons > 0) {
            installedPistons--;
            giveItemBack(player, new ItemStack(Items.PISTON)); // Вернуть поршень
            return finishDisassemblyStep(level, pos, SoundEvents.ARMOR_EQUIP_IRON.value(), onChanged);
        }
        if (hasCrankshaft) {
            hasCrankshaft = false;
            giveItemBack(player, new ItemStack(Items.IRON_INGOT)); // Вернуть коленвал
            return finishDisassemblyStep(level, pos, SoundEvents.ANVIL_BREAK, onChanged);
        }
        return InteractionResult.PASS;
    }

    private InteractionResult finishStep(Player player, ItemStack heldItem, net.minecraft.sounds.SoundEvent sound, float pitch, Runnable onChanged) {
        if (!player.isCreative()) heldItem.shrink(1);
        onChanged.run(); // Запускаем обновление BlockEntity
        return InteractionResult.CONSUME;
    }

    private InteractionResult finishDisassemblyStep(Level level, BlockPos pos, net.minecraft.sounds.SoundEvent sound, Runnable onChanged) {
        onChanged.run();
        return InteractionResult.CONSUME;
    }

    private void giveItemBack(Player player, ItemStack item) {
        if (!player.getInventory().add(item)) {
            player.drop(item, false);
        }
    }
}