package dev.userteemu.cablemod.mixin;

import dev.userteemu.cablemod.CableMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.block.HorizontalFacingBlock.FACING;

@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin {
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;emitsRedstonePower()Z"), method = "connectsTo(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z", cancellable = true)
	private static void connectsTo(BlockState state, Direction dir, CallbackInfoReturnable<Boolean> cir) {
		if (dir != null && state.isOf(CableMod.TRANSMITTER_BLOCK)) {
			cir.setReturnValue(state.get(FACING) == dir);
		}
	}
}
