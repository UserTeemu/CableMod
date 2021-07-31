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

public class CableRoute {
    @NotNull
    private final BlockPos senderPos;
    @NotNull
    private final BlockPos receiverPos;
    @NotNull
    public final CableType cableType;
    private final int routeLength;

    public boolean disposalScheduled = false;

    public CableRoute(@NotNull BlockPos senderPos, @NotNull BlockPos receiverPos, @NotNull CableType cableType, int routeLength) {
        this.senderPos = senderPos;
        this.receiverPos = receiverPos;
        this.cableType = cableType;
        this.routeLength = routeLength;
    }

    public void setup(World world) {
        TransmitterBlockEntity receiverBlockEntity = ((TransmitterBlockEntity)world.getBlockEntity(receiverPos));
        TransmitterBlockEntity senderBlockEntity = ((TransmitterBlockEntity)world.getBlockEntity(senderPos));
        if (receiverBlockEntity == null || senderBlockEntity == null) return;

        if (receiverBlockEntity.cableRoute != null) receiverBlockEntity.cableRoute.dispose(world);
        if (senderBlockEntity.cableRoute != null) senderBlockEntity.cableRoute.dispose(world);

        receiverBlockEntity.setupRoute(world, this, false);
        senderBlockEntity.setupRoute(world, this, true);
    }

    public BlockPos getOther(BlockPos thisPos) {
        if (senderPos.equals(thisPos)) return receiverPos;
        else if (receiverPos.equals(thisPos)) return senderPos;
        else throw new IllegalArgumentException("Argument must be one of the transmitter positions.");
    }

    public void dispose(WorldAccess world) {
        disposeEnd(world, receiverPos, true);
        disposeEnd(world, senderPos, true);
    }

    /**
     * Dispose, but a specific transmitter position is given special arguments (state and canSetBlockState)
     */
    public void dispose(WorldAccess world, BlockPos pos, BlockState state, boolean canSetBlockState) {
        if (senderPos.equals(pos)) {
            disposeEnd(world, state, senderPos, canSetBlockState);
            disposeEnd(world, receiverPos, true);
        } else if (receiverPos.equals(pos)) {
            disposeEnd(world, senderPos, true);
            disposeEnd(world, state, receiverPos, canSetBlockState);
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

    public int getRedstoneSignalStrength(int inputPower) {
        return Math.min(15, Math.max(0, switch (cableType) {
            case FIBER -> inputPower; // signal is sustained forever
            case COPPER -> inputPower - (routeLength / 4); // signal is sustained for 60 blocks if input is maximum
        }));
    }

    public NbtCompound toNBTCompound() {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.put("SenderPos", NbtHelper.fromBlockPos(senderPos));
        nbtCompound.put("ReceiverPos", NbtHelper.fromBlockPos(receiverPos));
        nbtCompound.putString("CableType", cableType.asString());
        nbtCompound.putInt("RouteLength", routeLength);
        return nbtCompound;
    }

    public static CableRoute fromNBTCompound(NbtCompound nbtCompound) {
        return new CableRoute(
                NbtHelper.toBlockPos(nbtCompound.getCompound("SenderPos")),
                NbtHelper.toBlockPos(nbtCompound.getCompound("ReceiverPos")),
                switch (nbtCompound.getString("CableType")) {
                    case "COPPER" -> CableType.COPPER;
                    case "FIBER" -> CableType.FIBER;
                    default -> throw new IllegalStateException("Unexpected cable type: " + nbtCompound.getString("CableType"));
                },
                nbtCompound.getInt("RouteLength")
        );
    }
}
