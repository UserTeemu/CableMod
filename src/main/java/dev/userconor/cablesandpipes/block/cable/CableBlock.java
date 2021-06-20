package dev.userconor.cablesandpipes.block.cable;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class CableBlock extends Block {
    public CableBlock() {
        super(FabricBlockSettings.of(Material.METAL).strength(4F));
    }

    public boolean isConnectedTo(Direction opposite, BlockState state) {
        return true; // todo add directions for the cable
    }
}
