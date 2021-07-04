package dev.userteemu.cablemod.block.transmitter;

import dev.userteemu.cablemod.CableRoute;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import static dev.userteemu.cablemod.CableMod.COPPER_CABLE_BLOCK;
import static dev.userteemu.cablemod.CableMod.FIBER_CABLE_BLOCK;

public class TransmitterBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    public static final BooleanProperty READY = BooleanProperty.of("ready");
    public static final BooleanProperty IS_SENDER = BooleanProperty.of("is_sender");
    public static final BooleanProperty POWERED = Properties.POWERED;

    public TransmitterBlock() {
        super(FabricBlockSettings.of(Material.DECORATION).breakInstantly().sounds(BlockSoundGroup.WOOD)); // same settings as redstone repeaters
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(POWERED, false).with(IS_SENDER, false).with(READY, false));
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        if (!world.isClient()) {
            TransmitterBlockEntity blockEntity = getBlockEntity(pos, world);
            if (blockEntity != null && blockEntity.cableRoute != null) {
                blockEntity.cableRoute.dispose(world);
            }
        }
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = null;
        int directionsWithCables = 0;
        for (Direction tempDirection : ctx.getPlacementDirections()) {
            if (tempDirection.getAxis() == Direction.Axis.Y) continue;

            BlockState state = ctx.getWorld().getBlockState(ctx.getBlockPos().offset(tempDirection));
            if (state.isOf(FIBER_CABLE_BLOCK) || state.isOf(COPPER_CABLE_BLOCK)) {
                direction = tempDirection;
                directionsWithCables++;
            }
        }

        return this.getDefaultState().with(FACING, directionsWithCables == 1 ? direction : ctx.getPlayerFacing());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (Hitboxes.isHittingButton(state.get(FACING), hit.getPos().subtract(pos.getX(), pos.getY(), pos.getZ()), 0.03D)) {
            if (!world.isClient()) {
                TransmitterBlockEntity blockEntity = getBlockEntity(pos, world);
                if (blockEntity == null) return ActionResult.FAIL;

                if (blockEntity.cableRoute != null) {
                    blockEntity.cableRoute.dispose(world);
                }
                blockEntity.createRoute(pos, world);
                world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
            }
            return ActionResult.success(world.isClient);
        } else {
            return super.onUse(state, world, pos, player, hand, hit);
        }
    }

    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!world.isClient && !state.isOf(newState.getBlock())) {
            TransmitterBlockEntity blockEntity = getBlockEntity(pos, world);
            if (blockEntity != null && blockEntity.cableRoute != null) {
                blockEntity.cableRoute.dispose(world, pos, state, false);
            }
        }

        super.onStateReplaced(state, world, pos, newState, false);
    }

    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClient) {
            TransmitterBlockEntity blockEntity = state.get(READY) ? getBlockEntity(pos, world) : null; // if not ready, it's null to save resources (because the field isn't actually used)
            CableRoute cableRoute = blockEntity != null ? blockEntity.cableRoute : null;
            if (cableRoute != null) {
                checkCableChanges(cableRoute, state, world, pos);
            }

            if (state.get(IS_SENDER) && state.get(POWERED) != getsRedstonePower(pos, state, world)) {
                world.setBlockState(pos, state = state.cycle(POWERED), Block.NOTIFY_LISTENERS);
                if (blockEntity != null) {
                    blockEntity.sendSignal(state.get(POWERED), world);
                }
            }
        }
    }

    private void checkCableChanges(CableRoute cableRoute, BlockState state, World world, BlockPos pos) {
        Block block = world.getBlockState(pos.offset(state.get(FACING))).getBlock();

        if (block != cableRoute.cableType().cableBlock) {
            cableRoute.dispose(world);
        }
    }

    private boolean getsRedstonePower(BlockPos pos, BlockState state, World world) {
        Direction direction = state.get(FACING).getOpposite();
        return world.getEmittedRedstonePower(pos.offset(direction), direction) > 0;
    }

    public static TransmitterBlockEntity getBlockEntity(BlockPos pos, BlockView world) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof TransmitterBlockEntity ? (TransmitterBlockEntity)blockEntity : null;
    }

    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TransmitterBlockEntity(pos, state);
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction facing = state.get(FACING);
        return VoxelShapes.union(
                Hitboxes.BASE_SHAPE,
                Hitboxes.getShapeInDirection(facing),
                Hitboxes.getButtonBoxInDirection(facing)
        );
    }

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.getWeakRedstonePower(world, pos, direction);
    }

    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (state.get(IS_SENDER)) return 0;
        return state.get(POWERED) && state.get(FACING) == direction ? 15 : 0;
    }

    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(READY, FACING, IS_SENDER, POWERED);
    }

    private static class Hitboxes {
        protected static final VoxelShape BASE_SHAPE = Block.createCuboidShape(0D, 0D, 0D, 16D, 5D, 16D);

        protected static final VoxelShape NORTH_SHAPE = createShape(5, 5, 0);
        protected static final VoxelShape SOUTH_SHAPE = createShape(5, 5, 10);
        protected static final VoxelShape EAST_SHAPE = createShape(10, 5, 5);
        protected static final VoxelShape WEST_SHAPE = createShape(0, 5, 5);

        @SuppressWarnings("SameParameterValue")
        private static VoxelShape createShape(int minX, int minY, int minZ) {
            return Block.createCuboidShape(minX, minY, minZ, minX + 6, minY + 6, minZ + 6);
        }

        protected static VoxelShape getShapeInDirection(Direction direction) {
            return switch (direction) {
                default -> NORTH_SHAPE;
                case SOUTH -> SOUTH_SHAPE;
                case EAST -> EAST_SHAPE;
                case WEST -> WEST_SHAPE;
            };
        }

        protected static final VoxelShape NORTH_BUTTON_SHAPE = createButtonShape(12, 5, 2);
        protected static final VoxelShape SOUTH_BUTTON_SHAPE = createButtonShape(2, 5, 12);
        protected static final VoxelShape EAST_BUTTON_SHAPE = createButtonShape(12, 5, 12);
        protected static final VoxelShape WEST_BUTTON_SHAPE = createButtonShape(2, 5, 2);

        private static VoxelShape createButtonShape(int minX, @SuppressWarnings("SameParameterValue") int minY, int minZ) {
            return Block.createCuboidShape(minX, minY, minZ, minX + 2, minY + 1, minZ + 2);
        }

        protected static VoxelShape getButtonBoxInDirection(Direction direction) {
            return switch (direction) {
                default -> NORTH_BUTTON_SHAPE;
                case SOUTH -> SOUTH_BUTTON_SHAPE;
                case EAST -> EAST_BUTTON_SHAPE;
                case WEST -> WEST_BUTTON_SHAPE;
            };
        }

        protected static boolean isHittingButton(Direction direction, Vec3d rayTraceHit, @SuppressWarnings("SameParameterValue") double threshold) {
            VoxelShape buttonShape = getButtonBoxInDirection(direction);
            boolean hitting = true;

            for (Direction.Axis axis : Direction.Axis.VALUES) {
                double hit = rayTraceHit.getComponentAlongAxis(axis);
                double min = buttonShape.getMin(axis);
                double max = buttonShape.getMax(axis);

                hitting = hit >= min - threshold && hit < max + threshold;
                if (!hitting) break;
            }
            return hitting;
        }
    }
}
