package net.origamimarie.minecraft.mixin;


import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    /*
     * This hijacks the start of this method in AbstractBlock, allowing child classes to inject their code.
     */
    @Inject(method = "onUseWithItem", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/block/AbstractBlock;onUseWithItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ItemActionResult;"))
    protected void onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
    }
}
