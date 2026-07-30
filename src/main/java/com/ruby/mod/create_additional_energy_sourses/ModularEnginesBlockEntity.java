package com.ruby.mod.create_additional_energy_sourses;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import java.util.List;

public class ModularEnginesBlockEntity extends NonModularEnginesBlockEntity {

    private boolean isController = true;
    private int currentEngineLength = 1;

    // 1. SMART CONSTRUCTOR
    public ModularEnginesBlockEntity(BlockPos pos, BlockState state, boolean isController) {
        super(ModBlocks.MODULAR_ENGINE_ENTITY.get(), pos, state);
        this.isController = isController;

        // Automatically determine the exact engine configurations from its block ID name
        String blockId = state.getBlock().toString().toLowerCase();

        if (blockId.contains("inline2") || blockId.contains("i2")) {
            this.engineType = "I";
            this.pistonCount = 2;
        } else if (blockId.contains("w8")) {
            this.engineType = "W";
            this.pistonCount = 8;
        } else if (blockId.contains("radial16") || blockId.contains("r16")) {
            this.engineType = "Radial";
            this.pistonCount = 16;
        } else {
            // Default configuration for a standard modular block is a V4!
            this.engineType = "V";
            this.pistonCount = 4;
        }
    }

    // 2. STANDARD GETTERS AND SETTERS
    public boolean isController() {
        return this.isController;
    }

    public void setController(boolean controller) {
        this.isController = controller;
        this.setChanged();
    }

    public int getCurrentEngineLength() {
        return this.currentEngineLength;
    }

    // 3. MULTI-BLOCK STRUCTURAL LINKS
    public ModularEnginesBlockEntity getController() {
        if (this.isController()) {
            return this;
        }

        BlockState state = this.getBlockState();
        if (!state.hasProperty(ModularEnginesBlock.HORIZONTAL_FACING)) {
            return this;
        }

        Direction facing = state.getValue(ModularEnginesBlock.HORIZONTAL_FACING);
        BlockPos behindPos = this.worldPosition.relative(facing.getOpposite());

        if (this.level != null && this.level.getBlockEntity(behindPos) instanceof ModularEnginesBlockEntity neighbor) {
            return neighbor.getController(); // Recursively trace back to the front block
        }

        this.setController(true); // Secure fallback
        return this;
    }

    public void updateEngineStructure() {
        if (!this.isController()) return;

        BlockState state = this.getBlockState();
        if (this.level == null || !state.hasProperty(ModularEnginesBlock.HORIZONTAL_FACING)) return;

        Direction facing = state.getValue(ModularEnginesBlock.HORIZONTAL_FACING);
        int length = 1;

        // Scan backwards to trace out the total connected module size
        for (int i = 1; i < 10; i++) {
            BlockPos checkPos = this.worldPosition.relative(facing.getOpposite(), i);

            if (this.level.getBlockEntity(checkPos) instanceof ModularEnginesBlockEntity neighbor) {
                if (neighbor.getBlockState().getValue(ModularEnginesBlock.HORIZONTAL_FACING) == facing) {
                    length++;
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        this.currentEngineLength = length;
    }

    // Helper method to scan radiator pipes
    private int getRadiatorRowWaterAndCount(BlockPos startPos, Direction scanDir, boolean checkWater) {
        if (this.level == null) return 0;

        int connectedInRow = 0;
        int totalWaterInRow = 0;

        for (int i = 0; i < 4; i++) {
            BlockPos currentPos = startPos.relative(scanDir, i);

            if (this.level.getBlockEntity(currentPos) instanceof BaseRadiatorBlockEntity radiator) {
                connectedInRow++;
                if (checkWater && radiator.waterTank != null) {
                    totalWaterInRow += radiator.waterTank.getFluidAmount();
                }
            } else {
                break; // Pipe row broke down
            }
        }

        return checkWater ? totalWaterInRow : connectedInRow;
    }
}