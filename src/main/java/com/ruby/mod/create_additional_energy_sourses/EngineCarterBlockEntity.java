package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EngineCarterBlockEntity extends BlockEntity {

    // Переменные этапов сборки
    public boolean hasCrankshaft = false;
    public int installedPistons = 0;
    public int installedGBC = 0;

    // Параметры компоновки, которые фабрика определит сама
    public int maxPistons = 8;
    public int maxGBC = 2;
    public String engineMaterial = "iron";
    public String engineType = "v8";
    public boolean installedBrain = false;
    public boolean hasUnitedController = false;
    public boolean isModular;

    public EngineCarterBlockEntity(BlockPos pos, BlockState state) {
        // Привязываем сущность к нашему общему регистратору картеров в ModBlocks
        super(ModBlocks.ENGINE_CARTER_ENTITY.get(), pos, state);

        // --- УМНАЯ АВТОМАТИЧЕСКАЯ ФАБРИКА ПАРСИНГА ИМЕНИ ---
        // Получаем текстовый ID установленного блока (например, "create_additional_energy_sourses:titanium_r32_carter")
        String blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().toLowerCase();

        // Разрезаем строку по нижнему подчёркиванию "_"
        String[] parts = blockId.split("_");
        if (parts.length >= 2) {
            this.engineMaterial = parts[0]; // Вытаскиваем материал: iron, aluminum или titanium
            this.engineType = parts[1];     // Вытаскиваем компоновку: i4, v8, w16, r32

            // Динамически выставляем лимиты поршней и голов на основе компоновки!
            switch (this.engineType) {
                case "r32" -> {
                    this.maxPistons = 32;
                    this.maxGBC = 8;
                    this.hasUnitedController = true;
                }
                case "w16" -> {
                    this.maxPistons = 16;
                    this.maxGBC = 2;
                }
                case "v8" -> {
                    this.maxPistons = 8;
                    this.maxGBC = 2;
                }
                case "i4" -> {
                    this.maxPistons = 4;
                    this.maxGBC = 1;
                    this.hasUnitedController = true;
                }
            }
            // Внутри конструктора EngineCarterBlockEntity
            if (blockId.contains("modular")) {
                this.isModular = true;
            }
        }
    }

    // Сохранение данных сборки в файл мира (NBT)
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("HasCrankshaft", this.hasCrankshaft);
        tag.putInt("InstalledPistons", this.installedPistons);
        tag.putInt("InstalledGBC", this.installedGBC);
        tag.putString("EngineMaterial", this.engineMaterial);
        tag.putString("EngineType", this.engineType);
        tag.putBoolean("InstalledBrain", this.installedBrain);
    }

    // Загрузка данных сборки при чтении чанка (NBT)
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        // Читаем этапы сборки из памяти мира без лишних проверок
        this.hasCrankshaft = tag.getBoolean("HasCrankshaft");
        this.installedPistons = tag.getInt("InstalledPistons");
        this.installedGBC = tag.getInt("InstalledGBC");
        this.installedBrain = tag.getBoolean("InstalledBrain");

        // Читаем строки компоновки, если они сохранились
        if (tag.contains("EngineMaterial")) this.engineMaterial = tag.getString("EngineMaterial");
        if (tag.contains("EngineType")) this.engineType = tag.getString("EngineType");
    }

    // Сетевой пакет синхронизации для плавного отображения поршней у клиента
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putBoolean("HasCrankshaft", this.hasCrankshaft);
        tag.putInt("InstalledPistons", this.installedPistons);
        tag.putInt("InstalledGBC", this.installedGBC);
        tag.putBoolean("InstalledBrain", this.installedBrain);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}