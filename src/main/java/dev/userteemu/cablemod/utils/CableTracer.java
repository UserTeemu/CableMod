package dev.userteemu.cablemod.utils;

import dev.userteemu.cablemod.CableMod;
import dev.userteemu.cablemod.CableRoute;
import dev.userteemu.cablemod.block.cable.CableBlock;
import dev.userteemu.cablemod.block.cable.CableShape;
import dev.userteemu.cablemod.block.cable.CableType;
import dev.userteemu.cablemod.block.transmitter.TransmitterBlock;
import dev.userteemu.cablemod.block.transmitter.TransmitterBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
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
     * Tries to retrieve a cable route that this cable is in by tracing a transmitter and getting its cable route.
     * If a transmitter isn't found in one end, there can't (or at least shouldn't) be a cable route anyway, so there is no point in trying the other way. In such case, null is returned.
     * @param beginningPos position of a cable block
     * @return traced transmitter's CableRoute, null if there is no transmitter or the route is null
     */
    @Nullable
    public static CableRoute getCableRouteOfCable(@NotNull BlockPos beginningPos, BlockState state, @NotNull WorldAccess world) {
        BlockPos pos = beginningPos;
        CableShape lastShape = state.get(CABLE_SHAPE);
        Direction lastDirection = lastShape.to; // either "from" or "to" direction of the shape, doesn't matter which

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
            } else if (tempState.isOf(CableMod.TRANSMITTER_BLOCK) && tempState.get(HorizontalFacingBlock.FACING) == direction.getOpposite()) {
                TransmitterBlockEntity blockEntity = TransmitterBlock.getBlockEntity(tempPos, world);
                return blockEntity != null ? blockEntity.cableRoute : null;
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
            } else if (tempState.isOf(CableMod.TRANSMITTER_BLOCK) && tempState.get(HorizontalFacingBlock.FACING) == direction.getOpposite()) {
                otherTransmitterPos = pos;
                break;
            } else {
                return null;
            }
        }

        return new CableRoute(beginningPos, otherTransmitterPos, cableType, routeLength);
    }
}
