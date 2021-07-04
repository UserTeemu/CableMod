package dev.userteemu.cablemod.block.cable;

import dev.userteemu.cablemod.CableMod;
import net.minecraft.util.StringIdentifiable;

import java.util.function.Consumer;

public enum CableType implements StringIdentifiable {
    COPPER(CableMod.COPPER_CABLE_BLOCK),
    FIBER(CableMod.FIBER_CABLE_BLOCK);

    public final CableBlock cableBlock;

    CableType(CableBlock cableBlock) {
        this.cableBlock = cableBlock;
    }

    public String toString() {
        return this.name();
    }

    public String asString() {
        return this.name();
    }
}
