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

public class CableBlock extends Block implements Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final EnumProperty<CableShape> CABLE_SHAPE = EnumProperty.of("cable_shape", CableShape.class);

    public CableBlock() {
        super(FabricBlockSettings.of(Material.METAL).strength(4F).nonOpaque());
        this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, false).with(CABLE_SHAPE, CableShape.NORTH_SOUTH));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, CABLE_SHAPE);
    }

    public static boolean isConnectedTo(Direction direction, BlockState state) {
        return state.get(CABLE_SHAPE).connectsTo(direction);
    }

    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(CABLE_SHAPE).cableShape;
    }

    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!world.isClient && !state.isOf(newState.getBlock())) {
            CompletableFuture.runAsync(() -> {
                CableRoute route = CableTracer.getCableRouteOfCable(pos, state, world);
                if (route != null) route.disposalScheduled = true;
            });
        }

        super.onStateReplaced(state, world, pos, newState, false);
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        boolean waterlogged = ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER;
        CableShape shape = getPlacementShape(ctx);
        return this.getDefaultState().with(WATERLOGGED, waterlogged).with(CABLE_SHAPE, shape);
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClient && world.getBlockState(pos).isOf(this)) {
            attachToOtherCables(world, pos, state);
        }
    }

    private void attachToOtherCables(World world, BlockPos pos, BlockState state) {
        CableShape currentShape = state.get(CABLE_SHAPE);
        boolean beginningConnected = cableIsConnectedInDirection(world, pos.offset(currentShape.from), currentShape.from.getOpposite(), this);
        boolean endConnected = cableIsConnectedInDirection(world, pos.offset(currentShape.to), currentShape.to.getOpposite(), this);

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

    private CableShape getPlacementShape(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();

        Direction[] possibleDirections = ctx.getPlacementDirections();
        Direction firstDirection = ctx.getSide().getOpposite();
        Direction secondDirection = ctx.getPlayerLookDirection().getOpposite();

        for (Direction direction : possibleDirections) {
            if (direction == firstDirection) continue;
            BlockState tempState = world.getBlockState(pos.offset(direction));
            if (
                    (tempState.isOf(this) && !cableIsConnected(world, pos, tempState, this)) ||
                    (tempState.isOf(CableMod.TRANSMITTER_BLOCK) && tempState.get(TransmitterBlock.FACING).getOpposite() == direction)
            ) {
                secondDirection = direction;
                break;
            }
        }

        return CableShape.shapeOf(firstDirection, secondDirection);
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

    public static boolean cableIsConnected(World world, BlockPos pos, BlockState state, CableBlock cableType) {
        CableShape shape = state.get(CABLE_SHAPE);
        return
                cableIsConnectedInDirection(world, pos, shape.from, cableType) &&
                cableIsConnectedInDirection(world, pos, shape.to, cableType);
    }

    public static boolean cableIsConnectedInDirection(World world, BlockPos pos, Direction direction, CableBlock cableType) {
        BlockState fromState = world.getBlockState(pos);
        return fromState.isOf(cableType) && fromState.get(CABLE_SHAPE).connectsTo(direction);
    }
}
