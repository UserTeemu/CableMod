package dev.userconor.cablesandpipes.utils;

import dev.userconor.cablesandpipes.CablesAndPipesMod;
import dev.userconor.cablesandpipes.block.cable.CableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Traces cable blocks to find a transmitter in the other end.
 */
public class CableTracer {
    public static BlockPos traceOtherTransmitter(@NotNull BlockPos beginningPos, @NotNull World world, @NotNull CableBlock cableType) {
        return traceOtherTransmitter(beginningPos, world, world.getBlockState(beginningPos).get(HorizontalFacingBlock.FACING), cableType);
    }

    @Nullable
    public static BlockPos traceOtherTransmitter(@NotNull BlockPos beginningPos, @NotNull World world, @NotNull Direction senderFacing, @NotNull CableBlock cableType) {
        BlockPos pos = beginningPos;
        Direction lastDirection = senderFacing;
        while (true) {
            boolean newCableBlockFound = false;
            for (Direction direction : getDirections(lastDirection)) {
                BlockPos tempPos = pos.offset(direction);
                BlockState tempState = world.getBlockState(tempPos);
                if (tempState.isOf(cableType) && cableType.isConnectedTo(direction.getOpposite(), tempState)) {
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
                    if (pos != beginningPos && tempState.isOf(CablesAndPipesMod.TRANSMITTER_BLOCK) && tempState.get(HorizontalFacingBlock.FACING) == direction.getOpposite()) {
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
