package net.origamimarie.minecraft.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.SpreadableBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Grass and mycelium survive and spread under up to 7 layers of snow.
@Mixin(SpreadableBlock.class)
public class SpreadableBlockMixin {

    @Inject(method = "canSurvive", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/block/SpreadableBlock;canSurvive(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;)Z"))
    private static void canSurvive(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockPos blockPos = pos.up();
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.isOf(Blocks.SNOW) && blockState.get(SnowBlock.LAYERS) < 8) {
            cir.setReturnValue(true);
        } else if (blockState.getFluidState().getLevel() == 8) {
            cir.setReturnValue(false);
        } else {
            int i = ChunkLightProvider.getRealisticOpacity(state, blockState, Direction.UP, blockState.getOpacity());
            cir.setReturnValue(i < 15);
        }
    }
}
