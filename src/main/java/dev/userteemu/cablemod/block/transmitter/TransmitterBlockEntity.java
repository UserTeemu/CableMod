package dev.userteemu.cablemod.block.transmitter;

import dev.userteemu.cablemod.CableMod;
import dev.userteemu.cablemod.CableRoute;
import dev.userteemu.cablemod.utils.CableTracer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static dev.userteemu.cablemod.CableMod.TRANSMITTER_BLOCK;
import static dev.userteemu.cablemod.block.transmitter.TransmitterBlock.*;

public class TransmitterBlockEntity extends BlockEntity {
    @Nullable
    public CableRoute cableRoute = null;

    public TransmitterBlockEntity(BlockPos pos, BlockState state) {
        super(CableMod.REDSTONE_SENDER_BLOCK_ENTITY, pos, state);
    }

    public void createRoute(BlockPos pos, World world) {
        if (world.isClient()) throw new IllegalStateException("Routes should not be created in client worlds!");
        CableRoute route = CableTracer.createCableRoute(pos, world, world.getBlockState(pos).get(HorizontalFacingBlock.FACING));
        if (route != null) {
            route.setup(world, pos);
        }
    }

    public void sendSignal(boolean powered, World world) {
        if (cableRoute == null) return;

        BlockPos otherTransmitterPos = cableRoute.getOther(pos);
        BlockState receiverBlockState = world.getBlockState(otherTransmitterPos);
        if (receiverBlockState == null || !receiverBlockState.isOf(TRANSMITTER_BLOCK)) return;
        TransmitterBlockEntity receiver = getBlockEntity(otherTransmitterPos, world);
        if (receiver != null) {
            receiver.receive(receiverBlockState, powered, world);
        }
    }

    public void receive(BlockState state, boolean signalState, World world) {
        if (state.get(IS_SENDER)) throw new IllegalStateException("Senders may not receive signals!");
        world.setBlockState(pos, state.with(POWERED, signalState), Block.NOTIFY_LISTENERS);
        world.updateNeighborsAlways(pos, TRANSMITTER_BLOCK);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        if (tag.contains("CableRoute")) {
            cableRoute = CableRoute.fromNBTCompound(tag);
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
            world.setBlockState(pos, state.with(READY, false).with(POWERED, state.get(IS_SENDER) ? state.get(POWERED) : false), Block.NOTIFY_LISTENERS);
        }
    }

    public void setupRoute(World world, CableRoute cableRoute, boolean isSender) {
        if (this.cableRoute != null) this.cableRoute.dispose(world);
        this.cableRoute = cableRoute;

        BlockState state = world.getBlockState(pos);
        if (state != null) {
            world.setBlockState(pos, state.with(READY, true).with(IS_SENDER, isSender), Block.NOTIFY_LISTENERS);
        }

        world.playSound(null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.5F);
    }
}