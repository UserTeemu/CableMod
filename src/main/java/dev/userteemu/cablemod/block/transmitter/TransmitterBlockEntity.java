package dev.userteemu.cablemod.block.transmitter;

import dev.userteemu.cablemod.CableMod;
import dev.userteemu.cablemod.block.cable.CableType;
import dev.userteemu.cablemod.utils.CableTracer;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static dev.userteemu.cablemod.CableMod.TRANSMITTER_BLOCK;

public class TransmitterBlockEntity extends BlockEntity {
    @Nullable
    public BlockPos otherTransmitterPos = null;

    @Nullable
    public CableType cableType = null;

    public TransmitterBlockEntity(BlockPos pos, BlockState state) {
        super(CableMod.REDSTONE_SENDER_BLOCK_ENTITY, pos, state);
    }

    public void pairReceiver(BlockPos pos, World world) {
        if (cableType == null) return;
        CompletableFuture.runAsync(() -> {
            BlockPos transmitterLocation = CableTracer.traceOtherTransmitter(pos, world, cableType.cableBlock);
            if (transmitterLocation != null) {
                TRANSMITTER_BLOCK.setReady(world, otherTransmitterPos = transmitterLocation, false);
                TRANSMITTER_BLOCK.setReady(world, pos, true);
            }
        });
    }

    public void sendSignal(BlockState state, World world) {
        BlockState receiverState = world.getBlockState(otherTransmitterPos);
        if (receiverState.isOf(TRANSMITTER_BLOCK)) {
            TRANSMITTER_BLOCK.receive(receiverState, world, otherTransmitterPos, state.get(TransmitterBlock.POWERED));
        }
    }

    public void unpair(World world, boolean notifyOtherEnd) {
        if (notifyOtherEnd && otherTransmitterPos != null) {
            TRANSMITTER_BLOCK.setNotReady(world, otherTransmitterPos, false);
        }
        otherTransmitterPos = null;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        if (tag.contains("OtherTransmitterPos")) otherTransmitterPos = NbtHelper.toBlockPos(tag.getCompound("OtherTransmitterPos"));

        cableType = switch (tag.getString("CableType")) {
            case "COPPER" -> CableType.COPPER;
            case "FIBER" -> CableType.FIBER;
            default -> null;
        };
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        if (otherTransmitterPos != null) tag.put("OtherTransmitterPos", NbtHelper.fromBlockPos(otherTransmitterPos));
        tag.putString("CableType", cableType == null ? "null" : cableType.asString());
        return tag;
    }
}