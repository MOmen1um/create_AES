package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;

public class V8EngineBlockEntity extends GeneratingKineticBlockEntity {

    // Наши новые чистые переменные
    public float currentSpeed = 0.0f;
    public float targetSpeed = 0.0f;
    public float temperature = 600.0f; // Стартуем с рабочей температуры

    // Параметры конкретного Титанового блока
    public final float baseSpeed = 7447.27f;
    public float quality = 100.0f; // Видимое качество (можно сделать рандомным при создании)
    public float luck = 1.2f;      // Скрытая удача (+20%)

    public boolean hasSupercharger = false; // Нагнетатель
    public String currentFuel = "none";     // Тип топлива

    public V8EngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // ЭТОТ МЕТОД ЗАСТАВИТ ДВИГАТЕЛЬ ДВИГАТЬ В СЕТИ CREATE!
    @Override
    public float getGeneratedSpeed() {
        return this.currentSpeed;
    }

    @Override
    public void tick() {
        super.tick();

        // Проверка типа топлива для определения нагрева и скорости
        if (!level.isClientSide) {
            float fuelMultiplier;
            switch (this.currentFuel) {
                case "gasoline" -> fuelMultiplier = 2.2f;
                case "diesel" -> fuelMultiplier = 1.6f;
                case "biodiesel" -> fuelMultiplier = 0.6f;
                default -> fuelMultiplier = 1.0f;
            }


            float superchatgerMultiplier = this.hasSupercharger ? 2.0f : 1.0f;

            float maxSafeSpeed = this.baseSpeed * (this.quality / 100.0f) * this.luck * fuelMultiplier * superchatgerMultiplier;
            float dynamicModifier = 0.05f / (1.0f + (this.currentSpeed / 10000.0f));
            this.currentSpeed = this.currentSpeed + (this.targetSpeed - this.currentSpeed) * dynamicModifier;

            this.updateGeneratedRotation();

            if (this.currentSpeed > maxSafeSpeed) {
                float overspeed = this.currentSpeed - maxSafeSpeed;
                this.temperature += (overspeed / 100.0f) * 1.5f;
            } else {
                this.temperature = 600.0f + (float)Math.sin(this.level.getGameTime() * 0.1f) * 3.0f;
            }

            if (this.temperature >= 1200.0f) {
                this.level.explode(null, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), 4.0f, true, Level.ExplosionInteraction.BLOCK);
            }

            this.notifyUpdate();
            this.setChanged();
        }
    }
}





