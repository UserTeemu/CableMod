package dev.userteemu.cablemod.block.transmitter;

import dev.userteemu.cablemod.CableMod;
import dev.userteemu.cablemod.CableRoute;
import dev.userteemu.cablemod.utils.CableTracer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static dev.userteemu.cablemod.CableMod.TRANSMITTER_BLOCK;
import static dev.userteemu.cablemod.block.transmitter.TransmitterBlock.*;

public class TransmitterBlockEntity extends BlockEntity {
    @Nullable
    public CableRoute cableRoute = null;

    @Nullable
    public CompletableFuture<CableRoute> cableRouteTrace = null;

    public TransmitterBlockEntity(BlockPos pos, BlockState state) {
        super(CableMod.TRANSMITTER_BLOCK_ENTITY, pos, state);
    }

    public void createRoute(BlockPos pos, World world) {
        if (world.isClient()) throw new IllegalStateException("Routes should not be created in client worlds!");

        cableRouteTrace = CompletableFuture.supplyAsync(() ->
                CableTracer.createCableRoute(pos, world, world.getBlockState(pos).get(FACING))
        );
    }

    public void serverTick(World world, BlockPos pos, BlockState state) {
        if (cableRouteTrace != null && cableRouteTrace.isDone()) {
            try {
                CableRoute result = cableRouteTrace.get();
                if (result != null) (cableRoute = result).setup(world);
            } catch (InterruptedException | ExecutionException e) {
                CableMod.LOGGER.warn("Error while setting up cable route result from cable tracing operation", e);
            }
            cableRouteTrace = null;
        }

        if (cableRoute != null && cableRoute.disposalScheduled) {
            cableRoute.dispose(world);
        }
    }

    public void sendSignal(int inputPower, World world, BlockState state) {
        if (cableRoute == null) return;
        world.setBlockState(pos, state.with(POWER, inputPower), Block.NOTIFY_LISTENERS);
        BlockPos otherTransmitterPos = cableRoute.getOther(pos);
        BlockState receiverBlockState = world.getBlockState(otherTransmitterPos);
        if (receiverBlockState == null || !receiverBlockState.isOf(TRANSMITTER_BLOCK)) return;
        TransmitterBlockEntity receiver = getBlockEntity(otherTransmitterPos, world);
        if (receiver != null) {
            int receiverPower = cableRoute.getRedstoneSignalStrength(inputPower);
            receiver.receive(receiverBlockState, receiverPower, world);
        }
    }

    public void receive(BlockState state, int power, World world) {
        if (state.get(IS_SENDER)) throw new IllegalStateException("Senders may not receive signals!");
        world.setBlockState(pos, state.with(POWER, power), Block.NOTIFY_LISTENERS | Block.NOTIFY_NEIGHBORS);
        world.updateNeighborsAlways(pos.offset(state.get(FACING).getOpposite()), TRANSMITTER_BLOCK);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        if (tag.contains("CableRoute")) {
            cableRoute = CableRoute.fromNBTCompound(tag.getCompound("CableRoute"));
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        if (cableRoute != null) tag.put("CableRoute", cableRoute.toNBTCompound());
        return tag;
    }

    public void onRouteDisposed(WorldAccess world, BlockState state, BlockPos pos, boolean canSetBlockState) {
        cableRoute = null;
        if (state != null && canSetBlockState) {
            world.setBlockState(pos, state.with(READY, false).with(POWER, state.get(IS_SENDER) ? state.get(POWER) : 0), Block.NOTIFY_LISTENERS | Block.NOTIFY_NEIGHBORS);
            world.updateNeighbors(pos.offset(state.get(FACING).getOpposite()), state.getBlock());
        }
    }

    public void setupRoute(World world, CableRoute cableRoute, boolean isSender) {
        if (this.cableRoute != null) this.cableRoute.dispose(world);
        this.cableRoute = cableRoute;

        BlockState state = world.getBlockState(pos);
        if (state != null) {
            world.setBlockState(pos, state = state.with(READY, true).with(IS_SENDER, isSender), Block.NOTIFY_LISTENERS);
            world.updateNeighbors(pos.offset(state.get(FACING).getOpposite()), state.getBlock());
        }

        world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.5F);

        if (isSender && state != null) {
            sendSignal(getGottenRedstonePower(world, pos, state), world, state);
        }
    }
}