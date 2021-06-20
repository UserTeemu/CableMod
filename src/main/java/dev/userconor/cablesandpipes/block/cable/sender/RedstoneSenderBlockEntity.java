package dev.userconor.cablesandpipes.block.cable.sender;

import dev.userconor.cablesandpipes.CablesAndPipesMod;
import dev.userconor.cablesandpipes.block.cable.ReceiverLocator;
import dev.userconor.cablesandpipes.block.cable.RedstoneReceiverBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static dev.userconor.cablesandpipes.CablesAndPipesMod.REDSTONE_RECEIVER_BLOCK;
import static dev.userconor.cablesandpipes.CablesAndPipesMod.REDSTONE_SENDER_BLOCK;
import static dev.userconor.cablesandpipes.block.cable.AbstractRedstoneTransmissionBlock.POWERED;
import static dev.userconor.cablesandpipes.block.cable.sender.RedstoneSenderBlock.READY;

public class RedstoneSenderBlockEntity extends BlockEntity {
    @Nullable
    public BlockPos receiverPos = null;

    public RedstoneSenderBlockEntity(BlockPos pos, BlockState state) {
        super(CablesAndPipesMod.REDSTONE_SENDER_BLOCK_ENTITY, pos, state);
    }

    public void pairReceiver(BlockPos pos, World world) { // todo figure out a way to get the sender paired
        CompletableFuture.runAsync(() -> {
            BlockPos receiverLocation = new ReceiverLocator(pos, world).traceReceiver();
            if (receiverLocation != null) {
                receiverPos = receiverLocation;
                REDSTONE_SENDER_BLOCK.setReady(world.getBlockState(pos), world, pos);
            }
        });
    }

    public void sendSignal(BlockState state, World world) {
        if (!state.get(READY) || receiverPos == null) {
            pairReceiver(pos, world);
            return;
        }

        BlockState receiverState = world.getBlockState(receiverPos);
        if (receiverState.getBlock() instanceof RedstoneReceiverBlock) {
            REDSTONE_RECEIVER_BLOCK.receive(receiverState, world, receiverPos, state.get(POWERED));
        }
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        if (tag.contains("ReceiverPos")) receiverPos = NbtHelper.toBlockPos(tag.getCompound("ReceiverPos"));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        if (receiverPos != null) tag.put("ReceiverPos", NbtHelper.fromBlockPos(receiverPos));
        return tag;
    }
}