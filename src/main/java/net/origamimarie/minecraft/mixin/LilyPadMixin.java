package net.origamimarie.minecraft.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.origamimarie.minecraft.CandlePadBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(AbstractBlock.AbstractBlockState.class)
public class LilyPadMixin {
    /*
     * This hijacks the call from AbstractBlock.AbstractBlockState.onUse() to Block.onUse().
     * Then it uses it for LilyPadBlock purposes when it's a LilyPadBlock,
     * and otherwise just passes it back to where it was originally destined to go.
     */
    @Redirect(method = "onUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"))
    public ActionResult replaceLilyPadOnUse(Block instance, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (instance instanceof LilyPadBlock) {
            return lilyPadOnUse(state, world, pos, player, hand, hit);
        } else {
            return instance.onUse(state, world, pos, player, hand, hit);
        }
    }

    private ActionResult lilyPadOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getStackInHand(hand);
        Item item = itemStack.getItem();
        if (itemStack.isIn(ItemTags.CANDLES)) {
            Block block = Block.getBlockFromItem(item);
            if (block instanceof CandleBlock) {
                if (!player.isCreative()) {
                    itemStack.decrement(1);
                }
                world.playSound(null, pos, SoundEvents.BLOCK_LILY_PAD_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.setBlockState(pos, CandlePadBlock.getCandlePadFromCandle(block));
                world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                player.incrementStat(Stats.USED.getOrCreateStat(item));
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        }
        return ActionResult.PASS;
    }

}

























