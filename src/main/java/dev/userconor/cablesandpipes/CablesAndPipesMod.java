package dev.userconor.cablesandpipes;

import dev.userconor.cablesandpipes.block.cable.cable.CableBlock;
import dev.userconor.cablesandpipes.block.cable.RedstoneReceiverBlock;
import dev.userconor.cablesandpipes.block.cable.cable.CableShape;
import dev.userconor.cablesandpipes.block.cable.sender.RedstoneSenderBlock;
import dev.userconor.cablesandpipes.block.cable.sender.RedstoneSenderBlockEntity;
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

public class CablesAndPipesMod implements ModInitializer {
	public static final String modid = "cablesandpipes";
	public static Logger LOGGER = LogManager.getLogger();

	public static final CableBlock CABLE_BLOCK = new CableBlock();
	public static final RedstoneSenderBlock REDSTONE_SENDER_BLOCK = new RedstoneSenderBlock();
	public static final RedstoneReceiverBlock REDSTONE_RECEIVER_BLOCK = new RedstoneReceiverBlock();
	public static BlockEntityType<RedstoneSenderBlockEntity> REDSTONE_SENDER_BLOCK_ENTITY;

	@Override
	public void onInitialize() {
		CableShape.createShapes();

		registerBlockWithItem("cable", CABLE_BLOCK, ItemGroup.TRANSPORTATION);
		registerBlockWithItem("cable_sender", REDSTONE_SENDER_BLOCK, ItemGroup.REDSTONE);
		registerBlockWithItem("cable_receiver", REDSTONE_RECEIVER_BLOCK, ItemGroup.REDSTONE);

		REDSTONE_SENDER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(modid, "cable_sender"), FabricBlockEntityTypeBuilder.create(RedstoneSenderBlockEntity::new, REDSTONE_SENDER_BLOCK).build(null));
	}

	private void registerBlockWithItem(String name, Block block, ItemGroup itemGroup) {
		Registry.register(Registry.BLOCK, new Identifier(modid, name), block);
		Registry.register(Registry.ITEM, new Identifier(modid, name), new BlockItem(block, new FabricItemSettings().group(itemGroup)));
	}
}
