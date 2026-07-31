package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import java.util.List;

public class V8EngineBlockEntity extends GeneratingKineticBlockEntity {

    public final FluidTank fuelTank = new FluidTank(1000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            // Просто сохраняем данные в Майнкрафте, без вызова цепных реакций сетей!
            V8EngineBlockEntity.this.setChanged();
        }
    };
    private boolean wasFuelEmpty = true;
    protected int burnTimeRemaining = 0;
    private float currentSpeed = 0;
    private float lastSentSpeed = 0f;

    protected String engineMaterial;
    public float engineTemperature = 20.0f;
    public boolean isTurboCharged = false;

    private int accelerationTicks = 0;
    private int overheatMeltingTimer = 0;
    protected float maxMeltingTemp;
    protected int pistonCount = 8;
    protected String engineType = "V";
    private boolean wasWaterEmpty;

    // Конструктор по умолчанию
    public V8EngineBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlocks.V8_ENGINE_ENTITY.get(), pos, state, "iron");
    }
    // Наш гибкий конструктор для детей перенаправляет вызов в твой главный рабочий конструктор на строке 51!
    public V8EngineBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> customType, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        // Передаем тип, позицию, стейт и дефолтный чугун, чтобы запустилась вся базовая инициализация баков!
        this(customType, pos, state, "iron");
    }

    // Главный конструктор
    public V8EngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, String material) {
        super(type, pos, state);
        this.engineMaterial = material;

        // УПРАВЛЕНИЕ РЕМЕНЕМ: Узнаем при создании всех двигателей базовую 100% термостойкость
        this.maxMeltingTemp = getMaterialMeltingPoint();

        // ВСТАВЛЯЙ СЮДА: Создаем наш менеджер сборки, когда все переменные уже применились!
        this.assembly = new EngineAssemblyManager(this);

        // ПРИНУДИТЕЛЬНЫЙ СБРОС ДЛЯ КАРТЕРОВ:
        this.assembly.hasCrankshaft = false;
        this.assembly.installedPistons = 0;
        this.assembly.installedGBC = 0;
        this.assembly.hasForgottenPart = false;
    }

    public EngineAssemblyManager assembly;

    public float getAmbientTemperature() {
        if (level == null) return 20.0f;
        float vanillaTemp = level.getBiome(worldPosition).value().getModifiedClimateSettings().temperature();
        if (vanillaTemp <= 0.0f) return -30.0f;
        if (vanillaTemp >= 1.5f) return 60.0f;
        return 20.0f;
    }

    public float getSafeEngineSpeed() {
        if (this.assembly == null || !this.assembly.hasForgottenPart) {
            return 0.0f;
        }
        // === 1. ВОССТАНОВЛЕННАЯ ЛОГИКА МАТЕРИАЛОВ И ТУРБИНЫ ===
        float baseLimit = 1024f;
        if (this.engineMaterial != null) {
            if (this.engineMaterial.equals("aluminum")) baseLimit = 4096f;
            if (this.engineMaterial.equals("titanium")) baseLimit = 8192f;
        }
        float baseSafeSpeed = baseLimit * (isTurboCharged ? 2.0f : 1.0f);

        // === 2. ИНТЕГРИРУЕМ РАДИАТОРЫ С ПРОВЕРКОЙ ВОДЫ (БЕЗ ДРАЙНА ТУТ) ===
        float currentMultiplier = 1.0f;
        net.minecraft.world.level.block.state.BlockState state = this.getBlockState();

        if (state.hasProperty(V8EngineBlock.HORIZONTAL_FACING)) {
            net.minecraft.core.Direction facing = state.getValue(V8EngineBlock.HORIZONTAL_FACING);
            net.minecraft.core.BlockPos frontPos = this.worldPosition.relative(facing);
            net.minecraft.world.level.block.entity.BlockEntity neighborBE = this.level.getBlockEntity(frontPos);

            if (neighborBE instanceof BaseRadiatorBlockEntity radiator && this.hasRadiatorConnected()) {
                String beName = net.minecraft.world.level.block.entity.BlockEntityType.getKey(radiator.getType()).toString();

                // Просто проверяем, что вода ЕСТЬ. Физически тратить её будем в тиках!
                if (!radiator.waterTank.isEmpty() && radiator.waterTank.getFluidAmount() > 0) {
                    if (beName.contains("copper")) currentMultiplier = 1.25f;
                    else if (beName.contains("steel")) currentMultiplier = 1.50f;
                    else if (beName.contains("brass")) currentMultiplier = 1.75f;
                    else if (beName.contains("titanium")) currentMultiplier = 2.00f;
                }
            }
        }
        this.setChanged();
        this.sendData();

        // Финальная скорость с учетом охлаждения и округления
        return Math.round((baseSafeSpeed * currentMultiplier) / 64.0f) * 64.0f;
    }




    private float getMaxEngineSpeed() {
        float safeSpeed = getSafeEngineSpeed();
        float fuelSpeedMultiplier = 1.0f;
        if (burnTimeRemaining > 0 && !fuelTank.isEmpty()) {
            String fluidId = BuiltInRegistries.FLUID.getKey(fuelTank.getFluid().getFluid()).toString();
            if (fluidId.equals("createdieselgenerators:ethanol")) fuelSpeedMultiplier = 0.6f;
            if (fluidId.equals("createdieselgenerators:biodiesel")) fuelSpeedMultiplier = 1.0f;
            if (fluidId.equals("createdieselgenerators:diesel")) fuelSpeedMultiplier = 1.4f;
            if (fluidId.equals("createdieselgenerators:gasoline")) fuelSpeedMultiplier = 2.0f;
        }
        return safeSpeed * fuelSpeedMultiplier;
    }

    public boolean hasRadiatorConnected() {
        if (this.level == null) return false;

        net.minecraft.world.level.block.state.BlockState state = this.getBlockState();
        if (!state.hasProperty(V8EngineBlock.HORIZONTAL_FACING)) return false;

        net.minecraft.core.Direction facing = state.getValue(V8EngineBlock.HORIZONTAL_FACING);
        net.minecraft.core.BlockPos frontPos = this.worldPosition.relative(facing);

        // 1. Сразу достаем BlockState перед капотом, чтобы узнать, как развернут блок
        net.minecraft.world.level.block.state.BlockState frontState = this.level.getBlockState(frontPos);
        net.minecraft.world.level.block.entity.BlockEntity neighborBE = this.level.getBlockEntity(frontPos);

        if (neighborBE instanceof BaseRadiatorBlockEntity radiator) {
            // 2. ПРОВЕРКА НАПРАВЛЕНИЯ РАДИАТОРА:
            // Проверяем, есть ли у радиатора свойство направления (на случай кастомных блоков)
            if (frontState.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
                net.minecraft.core.Direction radiatorFacing = frontState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);

                // Если радиатор развернут НЕ лицом к двигателю (не в противоположную сторону) — возвращаем false!
                if (radiatorFacing != facing.getOpposite()) {
                    return false;
                }
            }

            // 3. Твоя существующая проверка на воду в баке
            return !radiator.waterTank.isEmpty() && radiator.waterTank.getFluidAmount() > 0;
        }

        return false;
    }

    protected float getRadiatorCoolingEffect() {
        if (this.level == null) return 0.0f;

        net.minecraft.world.level.block.state.BlockState state = this.getBlockState();
        if (!state.hasProperty(V8EngineBlock.HORIZONTAL_FACING)) return 0.0f;

        net.minecraft.core.Direction facing = state.getValue(V8EngineBlock.HORIZONTAL_FACING);
        net.minecraft.core.BlockPos frontPos = this.worldPosition.relative(facing);
        net.minecraft.world.level.block.entity.BlockEntity neighborBE = this.level.getBlockEntity(frontPos);

        // 1. Заранее объявляем переменную эффективности охлаждения, чтобы Java её видела везде
        float efficiency = 0.0f;

        if (neighborBE instanceof BaseRadiatorBlockEntity radiator) {
            // Если бак пустой — радиатор физически не охлаждает
            if (!this.level.isClientSide) {
                if (!this.hasRadiatorConnected()) return 0.0f;

                // 2. ЖОР ВОДЫ НА СЕРВЕРЕ (когда мотор реально заведен и вращается)
                if (Math.abs(this.currentSpeed) > 1.0f && !this.level.isClientSide) {

                    // ФОКУС: Вода тратится строго один раз в секунду (каждый 20-й тик мира)!
                    // В остальные 19 тиков радиатор охлаждает бесплатно за счёт уже испарившейся порции.
                    if (this.level.getGameTime() % 20 == 0) {

                        // Твоя формула прогрессивного испарения в СЕКУНДУ:
                        float maxTemp = this.maxMeltingTemp > 0 ? this.maxMeltingTemp : 1200f;
                        int progressiveWaterUsage = (int)(3.0f * (this.engineTemperature / maxTemp * 100.0f));

                        // Защита от нулевого расхода
                        if (progressiveWaterUsage < 1) progressiveWaterUsage = 1;

                        // Защита от высасывания большего объема, чем осталось в баке радиатора
                        int finalDrain = Math.min(progressiveWaterUsage, radiator.waterTank.getFluidAmount());

                        // Сливаем воду из бака радиатора
                        radiator.waterTank.drain(finalDrain, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                        radiator.setChanged();

                        // Мгновенно обновляем полоску воды на клиенте
                        this.level.sendBlockUpdated(radiator.getBlockPos(), radiator.getBlockState(), radiator.getBlockState(), 3);
                    }
                }

                // Плавное обновление полоски воды у игрока (каждые 5 тиков)
                if (this.level.getGameTime() % 5 == 0) {
                    this.level.sendBlockUpdated(radiator.getBlockPos(), radiator.getBlockState(), radiator.getBlockState(), 3);
                }
            }

            // Возвращаем итоговое охлаждение по закону Ньютона (разница температур * эффективность радиатора)
            return (this.engineTemperature - 20f) * efficiency;
        }

        return 0.0f;
    }

    @Override
    public void addBehaviours(java.util.List<com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

        // Ограничиваем физическую шкалу ползунка, чтобы полоса была маленькой и аккуратной
        int sliderMaxSteps = 512;

        // Ручной трансформатор положения окошка
        com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform customTransform =
                new com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform() {
                    @Override
                    public net.minecraft.world.phys.Vec3 getLocalOffset(net.minecraft.world.level.LevelAccessor level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
                        // 1. Базовая дефолтная высота слайдера (1.01 — стандартный блок)
                        double targetY = 1.00D;

                        // 2. Вытаскиваем имя блока из стейта, чтобы определить тип мотора
                        String blockName = state.getBlock().toString().toLowerCase();

                        // 3. Твой switch-case для идеального выравнивания ползунка по высоте моделей!
                        // Поскольку один пиксель в Minecraft равен 1.0 / 16.0 = 0.0625, добавляем его к базе
                        if (blockName.contains("w16")) {
                            targetY = 1.00D + (1.0D / 16.0D); // Поднимаем на 1 пиксель (~1.0725)
                        } else if (blockName.contains("r32") || blockName.contains("radial")) {
                            targetY = 1.00D + (2.0D / 16.0D); // Поднимаем на 2 пикселя (~1.135)
                        }

                        // 4. Возвращаем вектор со смещением. Вектор автоматически учитывает повороты блока!
                        return new net.minecraft.world.phys.Vec3(0.5D, targetY, 0.5D);
                    }

                    @Override
                    public void rotate(net.minecraft.world.level.LevelAccessor level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state, com.mojang.blaze3d.vertex.PoseStack ms) {
                        // Поворачиваем плашку, чтобы она смотрела вверх на игрока
                        ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90f));
                    }
                };

        // Создаем ползунок Create
        com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour slider =
                new com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour(
                        net.minecraft.network.chat.Component.literal("Обороты двигателя (RPM)"),
                        this,
                        customTransform
                );

        // Задаем внутренние рамки шкалы (0-512)
        slider.between(0, sliderMaxSteps);

        // Коллбек: считываем значение (0-512) и умножаем на шаг 16, получая до 32768 RPM!
        slider.withCallback(value -> {
            this.targetSliderSpeed = (float) value;
            this.setChanged();
        });

        behaviours.add(slider);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        int createMaxSpeed = com.simibubi.create.infrastructure.config.AllConfigs.server().kinetics.maxRotationSpeed.get();
        if (createMaxSpeed < 32768) {
            currentSpeed = 0;
            return;
        }
        if (this.level != null && !this.level.isClientSide) {

            // Смотрим, есть ли топливо в баке
            boolean hasFuelNow = this.fuelTank.getFluidAmount() > 0;

            // Заводим флаг (переменную класса), чтобы мотор помнил своё прошлое состояние топлива
            // Добавь в в самый верх класса V8EngineBlockEntity!
            if (hasFuelNow && this.wasFuelEmpty) {
                // Мотор только что заправили! Мгновенно обновляем сеть без рекурсий
                this.updateGeneratedRotation();
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                this.wasFuelEmpty = false; // Запоминаем, что бак больше не пустой
            } else if (!hasFuelNow && !this.wasFuelEmpty) {
                // Топливо только что кончилось! Сбрасываем сеть в ноль
                this.updateGeneratedRotation();
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                this.wasFuelEmpty = true; // Запоминаем, что бак опустел
            }
            // Точно такая же проверка для воды в радиаторе!
            boolean hasWaterNow = this.hasRadiatorConnected();
            // Объяви private boolean wasWaterEmpty = true; в самом верху класса
            if (hasWaterNow != !this.wasWaterEmpty) {
                this.wasWaterEmpty = !hasWaterNow;
                this.setChanged();
                this.updateGeneratedRotation();
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }

        float ambientTemp = getAmbientTemperature();
        float meltingPoint = getMaterialMeltingPoint();
        float safeSpeed = getSafeEngineSpeed();
        float maxSpeed = getMaxEngineSpeed();
        float targetSpeed = Math.min(Math.abs(this.targetSliderSpeed * 64f), getMaxEngineSpeed());

        if (this.burnTimeRemaining > 0 && targetSpeed > 0) {
            int ticksToBurn = 1;
            this.burnTimeRemaining = Math.max(0, this.burnTimeRemaining - ticksToBurn);

            if (currentSpeed < targetSpeed) {
                if (accelerationTicks < 40) accelerationTicks++;
                float progress = (float) accelerationTicks / 40;
                currentSpeed = targetSpeed * (progress * progress);
            } else {
                currentSpeed = targetSpeed;
            }

            Fluid fluidInTank = fuelTank.getFluid().getFluid();

            float fuelHeat = getFuelHeatMultiplier(fluidInTank);


        } else {
            if (fuelTank.getFluidAmount() >= 100 && targetSpeed > 0) {
                Fluid fluidInTank = fuelTank.getFluid().getFluid();
                burnTimeRemaining = (int) (200 * (getFuelBurnTime(fluidInTank) / 100f));
                fuelTank.drain(100, FluidAction.EXECUTE);
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                setChanged();

                this.setChanged();
                this.sendData(); // Это заставит Create посылать точный объем топлива с сервера на твой экран!
            } else {
                if (accelerationTicks > 0) {
                    accelerationTicks--;
                    float progress = (float) accelerationTicks / 40;
                    currentSpeed = targetSpeed * (progress * progress);
                } else {
                    currentSpeed = 0;
                }
            }
        }


        // --- 💥 МЕХАНИКА АВАРИЙНОГО ВЗРЫВА ПРИ ПЕРЕГРЕВЕ ---
        // Убираем старое ограничение "2-х" от скорости — пусть Ньютоновский нагрев летит на максимум!
        if (this.engineTemperature >= this.maxMeltingTemp) {
            this.engineTemperature = this.maxMeltingTemp; // Удерживаем шкалу на пике

            // Если мотор работает на пределе плавления, запускаем таймер уничтожения
            if (this.currentSpeed > 0) {
                this.overheatMeltingTimer++;

                // Каждые полсекунды (10 тиков) пускаем грозные искры и дым из блока, предупреждая игрока!
                if (this.overheatMeltingTimer % 10 == 0) {
                    this.level.addParticle(net.minecraft.core.particles.ParticleTypes.LAVA,
                            this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.1, this.worldPosition.getZ() + 0.5, 0, 0.1, 0);
                    this.level.playSound(null, this.worldPosition, net.minecraft.sounds.SoundEvents.CAMPFIRE_CRACKLE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                }

                // 200 тиков = 10 секунд на максимальном жаре без воды. Время вышло — БАБАХ!
                if (this.overheatMeltingTimer >= 200) {
                    if (!this.level.isClientSide) {
                        // 1. Вычисляем базовую силу от материала
                        float basePower = switch (this.engineMaterial != null ? this.engineMaterial : "iron") {
                            case "aluminum" -> 12.0f;
                            case "titanium" -> 25.0f;
                            default -> 5.0f;
                        };

                        // 2. ОПРЕДЕЛЯЕМ ТИП ДВИГАТЕЛЯ И КРАТНО ВЫРАЩИВАЕМ СИЛУ ВЗРЫВА
                        float typeMultiplier = 1.0f; // По умолчанию для V8 оставляем х1

                        // Берем имя блока для точного определения конфигурации цилиндров
                        String blockName = this.getBlockState().getBlock().toString().toLowerCase();

                        if (blockName.contains("i4") || blockName.contains("inline")) {
                            typeMultiplier = 2f;   // Рядный — в 2 раза слабее
                        } else if (blockName.contains("w16")) {
                            typeMultiplier = 4.0f;   // W16 — в 2 раза сильнее!
                        } else if (blockName.contains("radial") || blockName.contains("r32")) {
                            typeMultiplier = 8.0f;   // Радиальный (звездообразный) — в 4 раза сильнее! 🌋
                        }

                        // Применяем множитель типа к базовой силе
                        float explosionPower = basePower * typeMultiplier;

                        // 3. Добавляем бонус от остатков топлива
                        if (!this.fuelTank.isEmpty()) {
                            explosionPower += ((float) this.fuelTank.getFluidAmount() / 100.0f);
                        }

                        int radius = (int) explosionPower;
                        int epiX = this.worldPosition.getX();
                        int epiY = this.worldPosition.getY();
                        int epiZ = this.worldPosition.getZ();
                        int radiusSq = radius * radius;
                        java.util.Random rnd = new java.util.Random(this.worldPosition.hashCode());
                        float phase1 = rnd.nextFloat() * 100f;
                        float phase2 = rnd.nextFloat() * 100f;

// Сила рваности. Для твоего масштаба ставим побольше!
// Кратер будет «гулять» внутрь и наружу примерно на 15-20 блоков.
                        float roughness = 18.0f;


                        for (int x = -radius; x <= radius; x++) {
                            for (int y = -radius; y <= radius; y++) {
                                for (int z = -radius; z <= radius; z++) {

                                    int distanceSq = x * x + y * y + z * z;

                                    float dist = (float) Math.sqrt(distanceSq);
                                    if (dist < 0.1f) dist = 0.1f;

// Направление вектора
                                    float nx = x / dist;
                                    float ny = y / dist;
                                    float nz = z / dist;

// Крупные, размашистые волны для гигантского радиуса
                                    float noise = (float) (Math.sin(nx * 1.5f + phase1) *
                                            Math.cos(ny * 1.2f + phase2) *
                                            Math.sin(nz * 1.7f));

// Добавляем мелкую текстуру краев поверх крупных волн
                                    float detailNoise = (float) (Math.cos(nx * 8.0f) * Math.sin(nz * 8.0f)) * 0.2f;

// Итоговый измененный радиус
                                    float modifiedRadius = (float) radius + ((noise + detailNoise) * roughness);
                                    float modifiedRadiusSq = modifiedRadius * modifiedRadius;

                                    if (distanceSq <= modifiedRadiusSq) {
                                        net.minecraft.core.BlockPos targetPos = new net.minecraft.core.BlockPos(epiX + x, epiY + y, epiZ + z);

                                        if (this.level.isInWorldBounds(targetPos)) {
                                            if (!this.level.isEmptyBlock(targetPos)) {
                                                this.level.setBlock(targetPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2 | 16);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        this.level.explode(null, epiX + 0.5, epiY + 0.5, epiZ + 0.5, 1.0f, Level.ExplosionInteraction.NONE);
                        // Удаляем останки ДВС
                        this.level.removeBlock(this.worldPosition, false);
                    }
                    return;
                }
            }
        } else {
            // Если игрок вовремя успел сбросить газ или залить воду — мотор начинает остывать, таймер сбрасывается
            if (this.overheatMeltingTimer > 0) {
                this.overheatMeltingTimer = Math.max(0, this.overheatMeltingTimer - 2); // Остывает износ постепенно
            }
        }

        // Меняем блок обновления вращения на безопасный:
        if (Math.abs(currentSpeed - lastSentSpeed) >= 16f) {
            // Защита: если мир только загрузился (gameTime маленький)
            // или блок ещё не валиден в мире, не пинаем сеть Create
            if (this.level != null && !this.isRemoved()) {
                updateGeneratedRotation();
            }
            lastSentSpeed = currentSpeed;
        } else if (currentSpeed == 0 && lastSentSpeed != 0) {
            if (this.level != null && !this.isRemoved()) {
                updateGeneratedRotation();
            }
            lastSentSpeed = 0;
        }

        if (level.getGameTime() % 20 == 0) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            setChanged();
        }

        float smokeChance = 0.05f + (engineTemperature / meltingPoint) * 0.75f;
        if (currentSpeed > 0 && level.random.nextFloat() < smokeChance && level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 1.1, worldPosition.getZ() + 0.5, 1, 0, 0.08, 0, 0);
        }
        // === ⚙️ ГЛОБАЛЬНАЯ ТЕРМОДИНАМИКА (Вынесена из условий топлива) ===
// 1. Защита от стартового нуля
        if (this.engineTemperature <= 0.0f) {
            this.engineTemperature = ambientTemp;
        }

// 2. Нагрев: идет ТОЛЬКО если мотор реально вращается (currentSpeed > 0)
        float baseHeat = 0.0f;
        float overspeedHeat = 0.0f;

        if (Math.abs(this.currentSpeed) > 1.0f) {
            // Вводим модификатор нагрева в зависимости от металла
            float materialHeatFactor = switch (this.engineMaterial != null ? this.engineMaterial : "cast_iron") {
                case "cast_iron" -> 1.0f;   // Чугун: стандарт
                case "aluminum"  -> 0.82f;  // АЛЮМИНИЙ: греется чуть слабее, чтобы стабилизироваться около 645-650°C без радиатора
                case "titanium"  -> 1.4f;   // Титан: греется сильнее, раскрывая свой огромный лимит
                default -> 1.0f;
            };

            // Мотор плавно греется на холостых (применяем наш коэффициент к базовому нагреву)
            baseHeat = (0.05f + (Math.abs(this.currentSpeed) / safeSpeed) * 3.35f) * materialHeatFactor;

            // Агрессивный перегрев, если зашли в красную зону превышения безопасного RPM
            if (Math.abs(this.currentSpeed) > safeSpeed) {
                overspeedHeat = ((Math.abs(this.currentSpeed) - safeSpeed) / safeSpeed) * 4.2f * materialHeatFactor;
            }
        }

// 3. Охлаждение по закону Ньютона (Радиатор считает true)
        float naturalCooling = 0.0f;
        float radiatorCooling = 0.0f;

        if (this.engineTemperature > ambientTemp) {
            // Вводим коэффициент естественной теплоотдачи металла на воздухе
            float materialCoolingFactor = switch (this.engineMaterial != null ? this.engineMaterial : "cast_iron") {
                case "cast_iron" -> 1.0f;   // Чугун остывает стандартно
                case "aluminum"  -> 1.25f;  // АЛЮМИНИЙ: высокая теплопроводность, остывает на воздухе быстрее!
                case "titanium"  -> 0.85f;  // Титан: неохотно отдает тепло в атмосферу
                default -> 1.0f;
            };

            // Металл остывает только от естественного воздуха вокруг
            naturalCooling = (this.engineTemperature - ambientTemp) * 0.005f * materialCoolingFactor;

            radiatorCooling = getRadiatorCoolingEffect();
        }

// Применяем тепловой баланс за тик
        this.engineTemperature += (baseHeat + overspeedHeat - naturalCooling - radiatorCooling);

// Фиксация, чтобы мотор пассивно нагревался/остывал до температуры биома
        if (this.engineTemperature < ambientTemp) {
            this.engineTemperature = ambientTemp;
        }

        if (this.level.getGameTime() % 20 == 0) {
            this.setChanged();
            this.sendData(); // Синхронизирует безопасную скорость и литры топлива с клиентом!
        }
        if (this.level != null && !this.level.isClientSide) {
            this.sendData(); // <--- ПИНАЕТ КЭШ FLYWHEEL И СИНХРОНИЗИРУЕТ ДАННЫЕ С КЛИЕНТОМ
            this.setChanged();
        }

    }

    private float getMaterialMeltingPoint() {
        if (engineMaterial.equals("aluminum")) return 660.0f;
        if (engineMaterial.equals("titanium")) return 1660.0f;
        return 1200.0f;
    }

    @Override
    public float getGeneratedSpeed() {
        return currentSpeed;
    }

    private float getFuelBurnTime(Fluid fluid) {
        String fluidId = BuiltInRegistries.FLUID.getKey(fluid).toString();
        if (fluidId.equals("createdieselgenerators:diesel")) return 100.0f;
        if (fluidId.equals("createdieselgenerators:biodiesel")) return 80.0f;
        if (fluidId.equals("createdieselgenerators:gasoline")) return 160.0f;
        if (fluidId.equals("createdieselgenerators:ethanol")) return 60.0f;
        return 0;
    }

    private float getFuelHeatMultiplier(Fluid fluid) {
        String fluidId = BuiltInRegistries.FLUID.getKey(fluid).toString();
        if (fluidId.equals("createdieselgenerators:ethanol")) return 0.5f;
        if (fluidId.equals("createdieselgenerators:biodiesel")) return 1.0f;
        if (fluidId.equals("createdieselgenerators:diesel")) return 1.5f;
        if (fluidId.equals("createdieselgenerators:gasoline")) return 2.5f;
        return 1.0f;
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (this.assembly == null || !this.assembly.hasForgottenPart) {
            return 0.0f;
        }
        if (currentSpeed <= 0) return 0;
        float materialMultiplier = engineMaterial.equals("iron") ? 15.0f : 10.0f;
        return currentSpeed * materialMultiplier;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        int createMaxSpeed = com.simibubi.create.infrastructure.config.AllConfigs.server().kinetics.maxRotationSpeed.get();
        if (createMaxSpeed < 32768) {
            tooltip.add(Component.literal("§c⚠ АВАРИЙНАЯ БЛОКИРОВКА!"));
            tooltip.add(Component.literal("§7Повысьте 'maxRotationSpeed' в конфиге Create до 32768!"));
            return true;
        }

        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.literal(""));

        String matColor = engineMaterial.equals("titanium") ? "§b" : (engineMaterial.equals("aluminum") ? "§7" : "§8");
        tooltip.add(Component.literal("§6Спецификация ДВС V8:"));
        tooltip.add(Component.literal(" §eМатериал блока: " + matColor + engineMaterial.toUpperCase()));

        String turboText = isTurboCharged ? "§aУСТАНОВЛЕН" : "§cОТСУТСТВУЕТ";
        tooltip.add(Component.literal(" §eТурбонаддув: " + turboText));

        tooltip.add(Component.literal(" §6Телеметрия температур:"));
        String tempColor = engineTemperature > (getMaterialMeltingPoint() - 200) ? "§c" : (engineTemperature > 90 ? "§6" : "§a");
        tooltip.add(Component.literal(" §eТемпература ядра: " + tempColor + String.format("%.1f", engineTemperature) + "°C / " + getMaterialMeltingPoint() + "°C"));
        tooltip.add(Component.literal(" §eБезопасная зона: §aдо " + String.format("%.0f", getSafeEngineSpeed()) + " RPM"));
        net.minecraft.world.level.block.state.BlockState blockState = this.getBlockState();
        if (blockState.hasProperty(V8EngineBlock.HORIZONTAL_FACING)) {
            net.minecraft.core.Direction facing = blockState.getValue(V8EngineBlock.HORIZONTAL_FACING);
            net.minecraft.core.BlockPos frontPos = this.worldPosition.relative(facing);
            net.minecraft.world.level.block.entity.BlockEntity neighborBE = this.level.getBlockEntity(frontPos);

            if (neighborBE instanceof BaseRadiatorBlockEntity radiator && this.hasRadiatorConnected()) {
                String beName = net.minecraft.world.level.block.entity.BlockEntityType.getKey(radiator.getType()).toString();
                String tierName = "ОБЫЧНЫЙ";
                String tierColor = "§7";
                String efficiency = "100%";

                if (beName.contains("copper")) { tierName = "МЕДНЫЙ"; tierColor = "§6"; efficiency = "+25%"; }
                else if (beName.contains("steel")) { tierName = "СТАЛЬНОЙ"; tierColor = "§f"; efficiency = "+50%"; }
                else if (beName.contains("brass")) { tierName = "ЛАТУННЫЙ"; tierColor = "§e"; efficiency = "+75%"; }
                else if (beName.contains("ultimate")) { tierName = "УЛЬТИМАТИВНЫЙ"; tierColor = "§b"; efficiency = "+100%"; }

                tooltip.add(Component.literal(""));
                tooltip.add(Component.literal("§6Состояние охлаждения V8:"));
                tooltip.add(Component.literal(" §eРадиатор: " + tierColor + tierName + " (§a" + efficiency + " RPM§e)"));

                if (!radiator.waterTank.isEmpty()) {
                    tooltip.add(Component.literal(" §eЗаполнение бака: §b" + radiator.waterTank.getFluidAmount() + " / " + radiator.waterTank.getCapacity() + " mB"));
                } else {
                    tooltip.add(Component.literal(" §c⚠ РАДИАТОР СУХОЙ (НЕТ ОХЛАЖДЕНИЯ!)"));
                }
            }
        }

        if (!fuelTank.isEmpty()) {
            tooltip.add(Component.literal(" §eТопливо: §7" + fuelTank.getFluid().getHoverName().getString() + " (" + fuelTank.getFluidAmount() + " mB)"));
        } else {
            tooltip.add(Component.literal(" §cБак пуст"));
        }
        return true;
    }

    @Override
    protected void write(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        // Честно сохраняем текущие градусы на диск и в сеть
        tag.putFloat("EngineTemperature", this.engineTemperature);

        tag.putBoolean("IsTurboCharged", this.isTurboCharged);
        //tag.putFloat("CurrentSpeed", this.currentSpeed);
        tag.putFloat("TargetSliderSpeed", this.targetSliderSpeed);

        net.minecraft.nbt.CompoundTag fluidTag = new net.minecraft.nbt.CompoundTag();
        this.fuelTank.writeToNBT(registries, fluidTag);
        tag.put("FuelTank", fluidTag);
    }


    @Override
    protected void read(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        // ГЕОМЕТРИЧЕСКОЕ ИСПРАВЛЕНИЕ: Считываем градусы из NBT ТОЛЬКО если они там реально были сохранены!
        if (tag.contains("EngineTemperature")) {
            this.engineTemperature = tag.getFloat("EngineTemperature");
        } else {
            // Если блок только что поставили и тега на диске нет — принудительно оставляем климат биома (20°C)!
            this.engineTemperature = getAmbientTemperature();
        }


        this.isTurboCharged = tag.getBoolean("IsTurboCharged");
        //this.currentSpeed = tag.getFloat("CurrentSpeed");
        this.targetSliderSpeed = tag.getFloat("TargetSliderSpeed");

        if (tag.contains("FuelTank")) {
            this.fuelTank.readFromNBT(registries, tag.getCompound("FuelTank"));
        }

        String blockPath = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .getKey(this.getBlockState().getBlock()).getPath().toLowerCase();

        if (blockPath.contains("carter")) {
            // ЛОГИКА ДЛЯ БЛОКА ЦИЛИНДРОВ (КАРТЕРА):
            if (tag.contains("HasCrankshaft")) {
                // Если игрок его уже строил — загружаем точные стадии поршней
                this.assembly.readNBT(tag);
            } else {
                // Если блок только что поставили на землю — он ГАРАНТИРОВАННО пустой
                this.assembly.hasCrankshaft = false;
                this.assembly.installedPistons = 0;
                this.assembly.installedGBC = 0;
                this.assembly.hasForgottenPart = false;
                this.assembly.hasTurbo = false;

                // ПРИНУДИТЕЛЬНО ОБНУЛЯЕМ ТЕЛЕМЕТРИЮ ДЛЯ НОВОГО КАРТЕРА:
                // Чтобы пустая железка на земле не имела топлива и была комнатной температуры!
                this.engineTemperature = getAmbientTemperature();
                this.isTurboCharged = false;
                this.targetSliderSpeed = 0.0f;
                // Очищаем бак через встроенный метод Create (или просто обнуляем жидкость, если у тебя кастомный класс)
                this.fuelTank.setFluid(net.neoforged.neoforge.fluids.FluidStack.EMPTY);
            }
        } else {
            // ЛОГИКА ДЛЯ ТВОИХ ОРИГИНАЛЬНЫХ 16 ДВИГАТЕЛЕЙ ИЗ КРЕАТИВА:
            // Говорим менеджеру, что они изначально на 100% собраны.
            // Все считанные выше баки, настройки ползунков скорости и температуры ОСТАЮТСЯ ЦЕЛЫМИ!
            this.assembly.hasForgottenPart = true;
            this.assembly.hasCrankshaft = true;
            this.assembly.installedPistons = this.pistonCount;
            this.assembly.installedGBC = this.pistonCount == 32 ? 1 : 2;
        }
    }


    // --- 2. СИНХРОНИЗАЦИЯ ПАКЕТОВ ДЛЯ ОЧКОВ ИНЖЕНЕРА (ОБНОВЛЕНИЕ БАКА НА ЭКРАНЕ) ---
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putString("EngineMaterial", engineMaterial);
        tag.putFloat("EngineTemperature", engineTemperature);
        tag.putBoolean("IsTurboCharged", isTurboCharged);
        tag.putFloat("SliderSpeed", targetSliderSpeed);

        // Упаковываем бак в сетевой тег обновления с поддержкой провайдера реестров
        CompoundTag fluidTag = new CompoundTag();
        this.fuelTank.writeToNBT(registries, fluidTag);
        tag.put("FuelTank", fluidTag);
        return tag;
    }
    @Override
    public void onLoad() {
        super.onLoad();

        // Как только блок загрузился в мир — жестко приказываем Create:
        // "Перепроверь и очисти старый кэш этой кинетической ветки!"
        if (this.level != null && !this.level.isClientSide) {
            // Вызываем обновление вращения, чтобы сбросить фантомные SU из кэша сохранения
            this.updateGeneratedRotation();
        }
    }


    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // Предоставляем бак для NeoForge BlockCapability системы труб
    public net.neoforged.neoforge.fluids.capability.IFluidHandler getFluidTank() {
        return this.fuelTank;
    }

    public float targetSliderSpeed = 0f;
}