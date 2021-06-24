package dev.userconor.cablesandpipes.block.cable;

import dev.userconor.cablesandpipes.utils.DirectionUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static dev.userconor.cablesandpipes.CablesAndPipesMod.CABLE_BLOCK;
import static dev.userconor.cablesandpipes.CablesAndPipesMod.REDSTONE_RECEIVER_BLOCK;

/**
 * Traces cable blocks to find a receiver. Trace operation starts from the sender.
 */
public class CableTracer {
    private final BlockPos senderPos;
    private final World world;
    private final Direction senderFacing;

    public CableTracer(BlockPos senderPos, World world, Direction senderFacing) {
        this.senderPos = senderPos;
        this.world = world;
        this.senderFacing = senderFacing;
    }

    public CableTracer(BlockPos senderPos, World world) {
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
                if (tempState.isOf(CABLE_BLOCK) && CABLE_BLOCK.isConnectedTo(direction.getOpposite(), tempState)) {
                    pos = tempPos;
                    lastDirection = direction;
                    newCableBlockFound = true;
                    break;
                }
            }
            if (!newCableBlockFound) {
                for (Direction direction : getDirections(lastDirection)) {
                    BlockPos tempPos = pos.offset(direction);
                    BlockState tempState = world.getBlockState(tempPos);
                    if (tempState.isOf(REDSTONE_RECEIVER_BLOCK) && tempState.get(FacingBlock.FACING) == direction) {
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
        System.arraycopy(DirectionUtil.getNeighborDirections(direction), 0, array, 1, 4);
        return array;
    }
}
