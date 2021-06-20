package dev.userconor.cablesandpipes.block.cable;

import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static dev.userconor.cablesandpipes.CablesAndPipesMod.CABLE_BLOCK;
import static net.minecraft.util.math.Direction.Axis.*;

public class ReceiverLocator {
    private final BlockPos senderPos;
    private final World world;
    private final Direction senderFacing;

    public ReceiverLocator(BlockPos senderPos, World world, Direction senderFacing) {
        this.senderPos = senderPos;
        this.world = world;
        this.senderFacing = senderFacing;
    }

    public ReceiverLocator(BlockPos senderPos, World world) {
        this(senderPos, world, world.getBlockState(senderPos).get(FacingBlock.FACING));
    }

    @Nullable
    public BlockPos traceReceiver() {
        BlockPos pos = this.senderPos;
        Direction lastDirection = senderFacing;
        while (true) {
            boolean newCableBlockFound = false;
            for (Direction direction : getDirections(lastDirection)) {
                BlockPos tempPos = pos.offset(direction);
                BlockState tempState = world.getBlockState(tempPos);
                if (tempState.getBlock() instanceof CableBlock && CABLE_BLOCK.isConnectedTo(direction.getOpposite(), tempState)) {
                    pos = tempPos;
                    lastDirection = direction;
                    newCableBlockFound = true;
                    break;
                }
            }
            if (!newCableBlockFound) {
                for (Direction direction : getDirections(lastDirection)) {
                    BlockPos tempPos = pos.offset(direction);
                    if (world.getBlockState(tempPos).getBlock() instanceof RedstoneReceiverBlock) {
                        return tempPos;
                    }
                }
                return null;
            }
        }
    }

    /**
     * @return an array of directions, where argument is the first (for optimization purposes). Arguments opposite direction is missing also for optimization purposes.
     */
    private static Direction[] getDirections(Direction direction) {
        Direction[] array = new Direction[5];
        array[0] = direction;
        switch (direction.getAxis()) {
            case X -> {
                array[1] = Direction.from(Y, Direction.AxisDirection.NEGATIVE);
                array[2] = Direction.from(Y, Direction.AxisDirection.POSITIVE);
                array[3] = Direction.from(Z, Direction.AxisDirection.NEGATIVE);
                array[4] = Direction.from(Z, Direction.AxisDirection.POSITIVE);
            }
            case Y -> {
                array[1] = Direction.from(X, Direction.AxisDirection.NEGATIVE);
                array[2] = Direction.from(X, Direction.AxisDirection.POSITIVE);
                array[3] = Direction.from(Z, Direction.AxisDirection.NEGATIVE);
                array[4] = Direction.from(Z, Direction.AxisDirection.POSITIVE);
            }
            case Z -> {
                array[1] = Direction.from(X, Direction.AxisDirection.NEGATIVE);
                array[2] = Direction.from(X, Direction.AxisDirection.POSITIVE);
                array[3] = Direction.from(Y, Direction.AxisDirection.NEGATIVE);
                array[4] = Direction.from(Y, Direction.AxisDirection.POSITIVE);
            }
        }
        return array;
    }
}
