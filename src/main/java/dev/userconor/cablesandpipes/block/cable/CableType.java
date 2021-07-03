package dev.userconor.cablesandpipes.block.cable;

import dev.userconor.cablesandpipes.CablesAndPipesMod;
import net.minecraft.util.StringIdentifiable;

public enum CableType implements StringIdentifiable {
    COPPER(CablesAndPipesMod.COPPER_CABLE_BLOCK),
    FIBER(CablesAndPipesMod.FIBER_CABLE_BLOCK);

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
