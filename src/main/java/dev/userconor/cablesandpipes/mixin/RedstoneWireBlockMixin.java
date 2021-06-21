package dev.userconor.cablesandpipes.mixin;

import dev.userconor.cablesandpipes.CablesAndPipesMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.block.FacingBlock.FACING;

@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin {
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;emitsRedstonePower()Z"), method = "connectsTo(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z", cancellable = true)
	private static void connectsTo(BlockState state, Direction dir, CallbackInfoReturnable<Boolean> cir) {
		if (state.isOf(CablesAndPipesMod.REDSTONE_RECEIVER_BLOCK)) {
			cir.setReturnValue(state.get(FACING).getOpposite() == dir);
		} else if (state.isOf(CablesAndPipesMod.REDSTONE_SENDER_BLOCK)) {
			cir.setReturnValue(state.get(FACING) == dir);
		}
	}
}
