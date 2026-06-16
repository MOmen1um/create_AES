package com.ruby.mod.create_additional_energy_sourses;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class V8EngineBlockEntity extends GeneratingKineticBlockEntity {

    // Создаем встроенный топливный бак на 4000 mB (4 ведра)
    public final FluidTank fuelTank = new FluidTank(4000);

    // Переменная, которая помнит, сколько тиков двигателю еще работать на текущей порции топлива
    private int burnTimeRemaining = 0;
    // Текущая скорость, которую выдает мотор
    private float currentSpeed = 0;

    public V8EngineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.V8_ENGINE_ENTITY.get(), pos, state);
    }




    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) return;

        // Если двигатель работает, уменьшаем время горения
        if (burnTimeRemaining > 0) {
            burnTimeRemaining--;
            // Если топливо горит, плавно разгоняем двигатель до максимума
            if (currentSpeed < 256f) currentSpeed += 4f;
        } else {
            // Если горение кончилось, пробуем "глотнуть" еще солярки из бака
            if (fuelTank.getFluidAmount() >= 100) { // Потребляем по 100 mB (0.1 ведра)
                Fluid fluidInTank = fuelTank.getFluid().getFluid();
                float fuelEfficiency = getFuelBurnTime(fluidInTank);

                if (fuelEfficiency > 0) {
                    // Тратим 100 mB топлива из бака
                    fuelTank.drain(100, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                    // Задаем время горения (например, 200 тиков = 10 секунд работы)
                    burnTimeRemaining = (int) (200 * (fuelEfficiency / 100f));
                    setChanged(); // Сообщаем Майнкрафту, что данные внутри блока обновились
                }
            } else {
                // Если бак пуст, двигатель плавно глохнет и останавливается
                if (currentSpeed > 0) currentSpeed -= 2f;
                if (currentSpeed < 0) currentSpeed = 0;
            }
        }

        // Каждые пару секунд обновляем генерацию сети Create, чтобы скорость применилась
        if (burnTimeRemaining % 20 == 0) {
            updateGeneratedRotation();
        }
    }

    @Override
    public float getGeneratedSpeed() {
        return currentSpeed;
    }

    // Наш секретный радар жидкостей дизельного мода по текстовым ID!
    private float getFuelBurnTime(Fluid fluid) {
        String fluidId = net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(fluid).toString();

        if (fluidId.equals("createdieselgenerators:diesel")) return 100.0f;     // Обычный дизель (базовый)
        if (fluidId.equals("createdieselgenerators:biodiesel")) return 80.0f;  // Биодизель (чуть слабее)
        if (fluidId.equals("createdieselgenerators:gasoline")) return 160.0f;   // Бензин (дает супер-эффект!)
        if (fluidId.equals("createdieselgenerators:ethanol")) return 60.0f;    // Этанол

        return 0; // Вода или лава двигатель не заведут
    }

    @Override
    public float calculateAddedStressCapacity() {
        // Наш V8 безумно мощный! Умножаем его скорость на 8.0 (выдаст тонну SU!)
        if (currentSpeed <= 0) return 0;
        return currentSpeed * 8.0f;
    }
}
