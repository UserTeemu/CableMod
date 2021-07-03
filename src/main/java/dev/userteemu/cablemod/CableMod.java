package dev.userteemu.cablemod;

import dev.userteemu.cablemod.block.cable.CableBlock;
import dev.userteemu.cablemod.block.cable.CableShape;
import dev.userteemu.cablemod.block.transmitter.TransmitterBlock;
import dev.userteemu.cablemod.block.transmitter.TransmitterBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CableMod implements ModInitializer {
	public static final String modid = "cablemod";
	public static Logger LOGGER = LogManager.getLogger();

	public static final CableBlock FIBER_CABLE_BLOCK = new CableBlock();
	public static final CableBlock COPPER_CABLE_BLOCK = new CableBlock();
	public static final TransmitterBlock TRANSMITTER_BLOCK = new TransmitterBlock();
	public static BlockEntityType<TransmitterBlockEntity> REDSTONE_SENDER_BLOCK_ENTITY;

	@Override
	public void onInitialize() {
		CableShape.createShapes();

		registerBlockWithItem("fiber_cable", FIBER_CABLE_BLOCK, ItemGroup.TRANSPORTATION);
		registerBlockWithItem("copper_cable", COPPER_CABLE_BLOCK, ItemGroup.TRANSPORTATION);
		registerBlockWithItem("transmitter", TRANSMITTER_BLOCK, ItemGroup.REDSTONE);

		REDSTONE_SENDER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(modid, "transmitter"), FabricBlockEntityTypeBuilder.create(TransmitterBlockEntity::new, TRANSMITTER_BLOCK).build(null));
	}

	private void registerBlockWithItem(String name, Block block, ItemGroup itemGroup) {
		Registry.register(Registry.BLOCK, new Identifier(modid, name), block);
		Registry.register(Registry.ITEM, new Identifier(modid, name), new BlockItem(block, new FabricItemSettings().group(itemGroup)));
	}
}
