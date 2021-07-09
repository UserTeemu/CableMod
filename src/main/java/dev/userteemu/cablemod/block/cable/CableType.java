package dev.userteemu.cablemod.block.cable;

import dev.userteemu.cablemod.CableMod;
import net.minecraft.util.StringIdentifiable;

public enum CableType implements StringIdentifiable {
    COPPER(CableMod.COPPER_CABLE_BLOCK),
    FIBER(CableMod.FIBER_CABLE_BLOCK);

    public final CableBlock cableBlock;

    CableType(CableBlock cableBlock) {
        this.cableBlock = cableBlock;
    }

    @Override
    public String toString() {
        return this.name();
    }

    @Override
    public String asString() {
        return this.name();
    }
}
