package dev.userteemu.cablemod;

import dev.userteemu.cablemod.block.cable.CableType;
import dev.userteemu.cablemod.block.transmitter.TransmitterBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.NotNull;

public record CableRoute(@NotNull BlockPos transmitter1Pos, @NotNull BlockPos transmitter2Pos, @NotNull CableType cableType, int routeLength) {
    public void setup(World world, BlockPos senderPos) {
        if (senderPos != transmitter1Pos && senderPos != transmitter2Pos) throw new IllegalArgumentException("SenderPos must be one of the transmitter positions.");

        setupEnd(world, transmitter1Pos, transmitter1Pos.equals(senderPos));
        setupEnd(world, transmitter2Pos, transmitter2Pos.equals(senderPos));
    }

    private void setupEnd(World world, BlockPos transmitterPos, boolean isSender) {
        TransmitterBlockEntity blockEntity = ((TransmitterBlockEntity)world.getBlockEntity(transmitterPos));
        if (blockEntity == null) return;
        blockEntity.setupRoute(world, this, isSender);
    }

    public BlockPos getOther(BlockPos thisPos) {
        if (transmitter1Pos.equals(thisPos)) return transmitter2Pos;
        else if (transmitter2Pos.equals(thisPos)) return transmitter1Pos;
        else throw new IllegalArgumentException("SenderPos must be one of the transmitter positions.");
    }

    public void dispose(WorldAccess world) {
        disposeEnd(world, transmitter1Pos, true);
        disposeEnd(world, transmitter2Pos, true);
    }

    /**
     * Dispose, but a specific transmitter position is given special arguments (state and canSetBlockState)
     */
    public void dispose(WorldAccess world, BlockPos pos, BlockState state, boolean canSetBlockState) {
        if (transmitter1Pos.equals(pos)) {
            disposeEnd(world, state, transmitter1Pos, canSetBlockState);
            disposeEnd(world, transmitter2Pos, true);
        } else if (transmitter2Pos.equals(pos)) {
            disposeEnd(world, transmitter1Pos, true);
            disposeEnd(world, state, transmitter2Pos, canSetBlockState);
        } else {
            dispose(world);
        }
    }

    private void disposeEnd(WorldAccess world, BlockPos transmitterPos, boolean canSetBlockState) {
        disposeEnd(world, world.getBlockState(transmitterPos), transmitterPos, canSetBlockState);
    }

    private void disposeEnd(WorldAccess world, BlockState state, BlockPos transmitterPos, boolean canSetBlockState) {
        TransmitterBlockEntity blockEntity = ((TransmitterBlockEntity)world.getBlockEntity(transmitterPos));
        if (blockEntity != null) blockEntity.onRouteDisposed(world, state, transmitterPos, canSetBlockState);
    }

    public NbtCompound toNBTCompound() {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.put("Transmitter1Pos", NbtHelper.fromBlockPos(transmitter1Pos));
        nbtCompound.put("Transmitter2Pos", NbtHelper.fromBlockPos(transmitter2Pos));
        nbtCompound.putString("CableType", cableType.asString());
        nbtCompound.putInt("RouteLength", routeLength);
        return nbtCompound;
    }

    public static CableRoute fromNBTCompound(NbtCompound nbtCompound) {
        return new CableRoute(
                NbtHelper.toBlockPos(nbtCompound.getCompound("Transmitter1Pos")),
                NbtHelper.toBlockPos(nbtCompound.getCompound("Transmitter2Pos")),
                switch (nbtCompound.getString("CableType")) {
                    case "COPPER" -> CableType.COPPER;
                    case "FIBER" -> CableType.FIBER;
                    default -> throw new IllegalStateException("Unexpected cable type: " + nbtCompound.getString("CableType"));
                },
                nbtCompound.getInt("RouteLength")
        );
    }
}
