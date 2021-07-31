package dev.userteemu.cablemod.block.cable;

import dev.userteemu.cablemod.CableMod;
import dev.userteemu.cablemod.CableRoute;
import dev.userteemu.cablemod.block.transmitter.TransmitterBlock;
import dev.userteemu.cablemod.utils.CableTracer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags.PICKAXES;

public class CableBlock extends Block implements Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final EnumProperty<CableShape> CABLE_SHAPE = EnumProperty.of("cable_shape", CableShape.class);

    public CableBlock() {
        super(FabricBlockSettings.of(Material.METAL).strength(2F, 6F).nonOpaque().breakByTool(PICKAXES));
        this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, false).with(CABLE_SHAPE, CableShape.NORTH_SOUTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, CABLE_SHAPE);
    }

    public static boolean isConnectedTo(Direction direction, BlockState state) {
        return state.get(CABLE_SHAPE).connectsTo(direction);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(CABLE_SHAPE).cableShape;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!world.isClient && !state.isOf(newState.getBlock())) {
            CompletableFuture.runAsync(() -> {
                BlockPos transmitterPos = CableTracer.tracePairedTransmitter(pos, state, world);
                if (transmitterPos != null) {
                    CableMod.markTransmitterToBeDisposed(world, transmitterPos);
                }
            });
        }

        super.onStateReplaced(state, world, pos, newState, false);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        boolean waterlogged = ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER;
        CableShape shape = getPlacementShape(ctx);
        return this.getDefaultState().with(WATERLOGGED, waterlogged).with(CABLE_SHAPE, shape);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClient && world.getBlockState(pos).isOf(this)) {
            attachToOtherCables(world, pos, state);
        }
    }

    private void attachToOtherCables(World world, BlockPos pos, BlockState state) {
        CableShape currentShape = state.get(CABLE_SHAPE);
        boolean beginningConnected = cableOrTransmitterIsConnectedInDirection(world, pos.offset(currentShape.from), currentShape.from.getOpposite(), this);
        boolean endConnected = cableOrTransmitterIsConnectedInDirection(world, pos.offset(currentShape.to), currentShape.to.getOpposite(), this);

        if (!beginningConnected) {
            state = state.with(CABLE_SHAPE, attachCable(world, pos, currentShape, true));
        }
        if (!endConnected) {
            state = state.with(CABLE_SHAPE, attachCable(world, pos, currentShape, false));
        }
        if (!beginningConnected || !endConnected) {
            world.setBlockState(pos, state);
        }
    }

    private CableShape attachCable(World world, BlockPos pos, CableShape currentShape, boolean beginning) {
        for (Direction direction : Direction.values()) {
            if (currentShape.connectsTo(direction)) continue;

            BlockState tempState = world.getBlockState(pos.offset(direction));
            if (tempState.isOf(this) && tempState.get(CABLE_SHAPE).connectsTo(direction.getOpposite())) {
                return CableShape.shapeOf(beginning ? direction : currentShape.from, beginning ? currentShape.to : direction);
            }
        }

        return currentShape;
    }

    private CableShape getPlacementShape(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();

        Direction[] possibleDirections = ctx.getPlacementDirections();
        Direction firstDirection = ctx.getSide().getOpposite();
        Direction secondDirection = ctx.getPlayerLookDirection().getOpposite();

        for (Direction direction : possibleDirections) {
            if (direction == firstDirection) continue;
            BlockPos tempPos = pos.offset(direction);

            if (cableOrTransmitterIsConnectedInDirection(world, tempPos, direction.getOpposite(), this)) {
                secondDirection = direction;
                break;
            }
        }

        return CableShape.shapeOf(firstDirection, secondDirection);
    }

    public static boolean cableIsConnectedInDirection(BlockState state, Direction direction, CableBlock cableType) {
        return state.isOf(cableType) && state.get(CABLE_SHAPE).connectsTo(direction);
    }

    public static boolean transmitterIsConnectedInDirection(BlockState state, Direction direction) {
        return state.isOf(CableMod.TRANSMITTER_BLOCK) && state.get(TransmitterBlock.FACING) == direction;
    }

    public static boolean cableOrTransmitterIsConnectedInDirection(World world, BlockPos pos, Direction direction, CableBlock cableType) {
        BlockState state = world.getBlockState(pos);
        return cableIsConnectedInDirection(state, direction, cableType) || transmitterIsConnectedInDirection(state, direction);
    }
}
