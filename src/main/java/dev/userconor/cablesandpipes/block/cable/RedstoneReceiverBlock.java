package dev.userconor.cablesandpipes.block.cable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class RedstoneReceiverBlock extends AbstractRedstoneTransmissionBlock {
    public void receive(BlockState state, World world, BlockPos pos, boolean signalState) {
        world.setBlockState(pos, state.with(POWERED, signalState), Block.NOTIFY_LISTENERS);
        world.updateNeighborsAlways(pos, this);
    }

    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.getWeakRedstonePower(world, pos, direction);
    }

    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWERED) && state.get(FACING).getOpposite() == direction ? 15 : 0;
    }

    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }
}
