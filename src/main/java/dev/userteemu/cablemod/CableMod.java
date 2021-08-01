package dev.userteemu.cablemod;

import dev.userteemu.cablemod.block.cable.CableBlock;
import dev.userteemu.cablemod.block.cable.CableShape;
import dev.userteemu.cablemod.block.transmitter.TransmitterBlock;
import dev.userteemu.cablemod.block.transmitter.TransmitterBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class CableMod implements ModInitializer {
	public static final String modid = "cablemod";
	public static final Logger LOGGER = LogManager.getLogger();

	public static final CableBlock FIBER_CABLE_BLOCK = new CableBlock();
	public static final CableBlock COPPER_CABLE_BLOCK = new CableBlock();
	public static final TransmitterBlock TRANSMITTER_BLOCK = new TransmitterBlock();
	public static BlockEntityType<TransmitterBlockEntity> TRANSMITTER_BLOCK_ENTITY;

	/**
	 * Cable tracing async cannot access block entities, so that must be done on the main thread. This acts as a bridge in between.
	 */
	public static final Map<World, Set<BlockPos>> transmittersToBeDisposed = Collections.synchronizedMap(new HashMap<>());

	@Override
	public void onInitialize() {
		CableShape.createShapes();

		registerBlockWithItem("fiber_cable", FIBER_CABLE_BLOCK, ItemGroup.TRANSPORTATION);
		registerBlockWithItem("copper_cable", COPPER_CABLE_BLOCK, ItemGroup.TRANSPORTATION);
		registerBlockWithItem("transmitter", TRANSMITTER_BLOCK, ItemGroup.REDSTONE);

		TRANSMITTER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(modid, "transmitter"), FabricBlockEntityTypeBuilder.create(TransmitterBlockEntity::new, TRANSMITTER_BLOCK).build(null));

		ServerTickEvents.START_WORLD_TICK.register(this::disposeDisposableTransmitters);
	}

	private void registerBlockWithItem(String name, Block block, ItemGroup itemGroup) {
		Registry.register(Registry.BLOCK, new Identifier(modid, name), block);
		Registry.register(Registry.ITEM, new Identifier(modid, name), new BlockItem(block, new FabricItemSettings().group(itemGroup)));
	}

	public void disposeDisposableTransmitters(World world) {
		Set<BlockPos> blocks = transmittersToBeDisposed.get(world);
		if (blocks != null) {
			for (BlockPos pos : blocks) {
				BlockState state = world.getBlockState(pos);
				if (state.hasBlockEntity() && state.isOf(TRANSMITTER_BLOCK)) {
					TransmitterBlockEntity blockEntity = TransmitterBlock.getBlockEntity(pos, world);
					if (blockEntity != null && blockEntity.cableRoute != null) {
						blockEntity.cableRoute.dispose(world);
					}
				}
				blocks.remove(pos);
			}
		}
	}

	public static void markTransmitterToBeDisposed(World world, BlockPos transmitterPos) {
		CableMod.transmittersToBeDisposed.computeIfAbsent(world, key -> Collections.synchronizedSet(new HashSet<>())).add(transmitterPos);
	}
}
