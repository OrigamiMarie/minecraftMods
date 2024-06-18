package net.origamimarie.minecraft.mixin;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.origamimarie.minecraft.CandlePadBlock;
import net.origamimarie.minecraft.OrigamiMarieMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlintAndSteelItem.class)
public class FlintAndSteelItemMixin {
    @Inject(method = "useOnBlock", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/item/FlintAndSteelItem;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"))
    public void replaceFlintAndSteelUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity playerEntity = context.getPlayer();
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        if (CandlePadBlock.canBeLit(blockState)) {
            world.playSound(playerEntity, blockPos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
            world.setBlockState(blockPos, blockState.with(Properties.LIT, true), 11);
            world.emitGameEvent(playerEntity, GameEvent.BLOCK_CHANGE, blockPos);
            if (playerEntity != null) {
                context.getStack().damage(1, playerEntity, (p) -> {
                    p.sendToolBreakStatus(context.getHand());
                });
            }
            cir.setReturnValue(ActionResult.success(world.isClient()));
        }
    }
}
