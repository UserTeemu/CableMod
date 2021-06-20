package dev.userconor.cablesandpipes.block.cable;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.Material;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

public abstract class AbstractRedstoneTransmissionBlock extends Block {
    public static final DirectionProperty FACING = FacingBlock.FACING;
    public static final BooleanProperty POWERED = Properties.POWERED;

    public AbstractRedstoneTransmissionBlock() {
        super(FabricBlockSettings.of(Material.DECORATION).breakInstantly().sounds(BlockSoundGroup.WOOD)); // same settings as redstone repeaters
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(POWERED, false));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerLookDirection());
    }
}
