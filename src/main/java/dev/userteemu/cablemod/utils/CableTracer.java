package dev.userteemu.cablemod.utils;

import dev.userteemu.cablemod.CableMod;
import dev.userteemu.cablemod.CableRoute;
import dev.userteemu.cablemod.block.cable.CableBlock;
import dev.userteemu.cablemod.block.cable.CableShape;
import dev.userteemu.cablemod.block.cable.CableType;
import dev.userteemu.cablemod.block.transmitter.TransmitterBlock;
import dev.userteemu.cablemod.block.transmitter.TransmitterBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.userteemu.cablemod.block.cable.CableBlock.CABLE_SHAPE;
import static dev.userteemu.cablemod.block.cable.CableType.COPPER;
import static dev.userteemu.cablemod.block.cable.CableType.FIBER;

/**
 * Traces cable blocks to find a transmitter in the other end.
 */
public class CableTracer {
    /**
     * Tries to trace a paired transmitter that this cable is connected to.
     * @param beginningPos position of a cable block
     * @return traced transmitter's position, null if no transmitter is found
     */
    @Nullable
    public static BlockPos tracePairedTransmitter(@NotNull BlockPos beginningPos, BlockState state, @NotNull WorldAccess world) {
        BlockPos pos = beginningPos;
        CableShape lastShape = state.get(CABLE_SHAPE);
        Direction lastDirection = lastShape.to; // either "from" or "to" direction of the shape, doesn't matter which, because if there isn't a paired transmitter in one end, the one in the other end can't be paired either.

        CableType cableType = state.isOf(COPPER.cableBlock) ? COPPER : state.isOf(FIBER.cableBlock) ? FIBER : null;
        if (cableType == null) return null;

        while (true) {
            Direction direction = lastShape.getOtherDirection(lastDirection.getOpposite());
            BlockPos tempPos = pos.offset(direction);
            BlockState tempState = world.getBlockState(tempPos);
            if (tempState.isOf(cableType.cableBlock)) {
                pos = tempPos;
                lastShape = tempState.get(CABLE_SHAPE);
                lastDirection = direction;
            } else if (tempState.isOf(CableMod.TRANSMITTER_BLOCK) && tempState.get(TransmitterBlock.FACING) == direction.getOpposite() && tempState.get(TransmitterBlock.READY)) {
                return tempPos;
            }
        }
    }

    /**
     * @param beginningPos known position of one end of the route
     * @param world world of the route
     * @param beginningTransmitterFacing direction that the transmitter in beginningPos is facing
     * @return an instance of CableRoute for this route, null if there is no possible route
     */
    @Nullable
    public static CableRoute createCableRoute(@NotNull BlockPos beginningPos, @NotNull World world, @NotNull Direction beginningTransmitterFacing) {
        BlockPos pos = beginningPos.offset(beginningTransmitterFacing);

        CableType cableType;
        BlockState cableState = world.getBlockState(pos);
        if (cableState.isOf(COPPER.cableBlock) && CableBlock.isConnectedTo(beginningTransmitterFacing.getOpposite(), cableState)) cableType = COPPER;
        else if (cableState.isOf(FIBER.cableBlock) && CableBlock.isConnectedTo(beginningTransmitterFacing.getOpposite(), cableState)) cableType = FIBER;
        else return null;

        CableShape lastShape = cableState.get(CABLE_SHAPE);
        Direction lastDirection = beginningTransmitterFacing;
        int routeLength = 1;

        BlockPos otherTransmitterPos;
        while (true) {
            Direction direction = lastShape.getOtherDirection(lastDirection.getOpposite());
            pos = pos.offset(direction);
            BlockState tempState = world.getBlockState(pos);
            if (tempState.isOf(cableType.cableBlock) && CableBlock.isConnectedTo(direction.getOpposite(), tempState)) {
                lastShape = tempState.get(CABLE_SHAPE);
                lastDirection = direction;
                routeLength++;
            } else if (tempState.isOf(CableMod.TRANSMITTER_BLOCK) && tempState.get(TransmitterBlock.FACING) == direction.getOpposite()) {
                otherTransmitterPos = pos;
                break;
            } else {
                return null;
            }
        }

        return new CableRoute(beginningPos, otherTransmitterPos, cableType, routeLength);
    }
}
