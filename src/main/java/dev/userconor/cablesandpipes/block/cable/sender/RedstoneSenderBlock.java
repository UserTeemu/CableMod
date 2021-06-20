package dev.userconor.cablesandpipes.block.cable.sender;

import dev.userconor.cablesandpipes.block.cable.AbstractRedstoneTransmissionBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import static dev.userconor.cablesandpipes.CablesAndPipesMod.CABLE_BLOCK;

public class RedstoneSenderBlock extends AbstractRedstoneTransmissionBlock implements BlockEntityProvider {
    public static final BooleanProperty READY = BooleanProperty.of("ready");

    public RedstoneSenderBlock() {
        super();
        this.setDefaultState(getDefaultState().with(READY, false));
    }

    @SuppressWarnings("deprecation") // not actually deprecated for overriding
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClient) {
            if (state.get(POWERED) != getsRedstonePower(pos, state, world)) {
                world.setBlockState(pos, state.cycle(POWERED), Block.NOTIFY_LISTENERS);
                RedstoneSenderBlockEntity blockEntity = getBlockEntity(pos, world);
                if (blockEntity != null) blockEntity.sendSignal(state, world);
            }

            if (state.get(READY)) checkIfAttachedToCable(state, world, pos);
        }
    }

    private boolean getsRedstonePower(BlockPos pos, BlockState state, World world) {
        Direction direction = state.get(FACING).getOpposite();
        return world.getEmittedRedstonePower(pos.offset(direction), direction) > 0;
    }

    private void checkIfAttachedToCable(BlockState state, World world, BlockPos pos) {
        BlockPos expectedCablePos = pos.offset(state.get(FACING));
        if (!world.getBlockState(expectedCablePos).isOf(CABLE_BLOCK)) {
            world.setBlockState(pos, state.with(READY, false), Block.NOTIFY_LISTENERS);
        }
    }

    public void setReady(BlockState state, World world, BlockPos pos) {
        world.setBlockState(pos, state.with(READY, true), Block.NOTIFY_LISTENERS);
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder.add(READY));
    }

    private RedstoneSenderBlockEntity getBlockEntity(BlockPos pos, World world) {
        return ((RedstoneSenderBlockEntity)world.getBlockEntity(pos));
    }

    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RedstoneSenderBlockEntity(pos, state);
    }
}
