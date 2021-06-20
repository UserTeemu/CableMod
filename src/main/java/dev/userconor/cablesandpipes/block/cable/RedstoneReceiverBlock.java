package dev.userconor.cablesandpipes.block.cable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RedstoneReceiverBlock extends AbstractRedstoneTransmissionBlock {
    public boolean signalState = false;

    public void receive(BlockState state, World world, BlockPos pos, boolean signalState) {
        this.signalState = signalState;
        world.setBlockState(pos, state.with(POWERED, signalState), Block.NOTIFY_LISTENERS);
        // todo actually cause the output of redstone, line below was just a quick test
        // world.setBlockState(pos.west(), Blocks.REDSTONE_LAMP.getDefaultState().with(RedstoneLampBlock.LIT, signalState));
    }
}
