package net.origamimarie.minecraft.mixin.itemframe;

import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.origamimarie.minecraft.WrenchItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameEntity.class)
public abstract class ItemFrameEntityMixin {

    @Inject(method = "interact", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/entity/decoration/ItemFrameEntity;interact(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"))
    public void interactInterruption(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        // Alright, so the compiler thinks that 'this' can't be any of those things, because we don't inherit from them.
        // That's sensible, but wrong, because this code is going to be transcribed into the ItemFrameEntity code before compilation.
        // That'll take care of the problem, but all the checking before that is going to be kind of clueless.
        Object tempThis = this;
        ItemFrameEntity frame = (ItemFrameEntity) tempThis;
        BlockAttachedEntityAccessor selfEntityAccessor = (BlockAttachedEntityAccessor) tempThis;
        boolean invisible = frame.isInvisible();
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.getItem() instanceof WrenchItem) {
            frame.setInvisible(!invisible);
            updateBoundingBox(invisible ? 12 : 4, selfEntityAccessor, frame);
            cir.setReturnValue(ActionResult.SUCCESS);
        } else {
            if (invisible) {
                cir.setReturnValue(ActionResult.PASS);
            }
        }
    }


    @Unique
    private static void updateBoundingBox(double size, BlockAttachedEntityAccessor blockAttachedEntityAccessor, ItemFrameEntity itemFrame) {
        Direction facing = itemFrame.getFacing();
        BlockPos attachmentPos = blockAttachedEntityAccessor.getAttachedBlockPos();
        if (facing != null) {
            double e = (double)attachmentPos.getX() + 0.5 - (double)facing.getOffsetX() * 0.46875;
            double f = (double)attachmentPos.getY() + 0.5 - (double)facing.getOffsetY() * 0.46875;
            double g = (double)attachmentPos.getZ() + 0.5 - (double)facing.getOffsetZ() * 0.46875;
            double h = size;
            double i = size;
            double j = size;
            Direction.Axis axis = facing.getAxis();
            switch (axis) {
                case X -> h = 1.0;
                case Y -> i = 1.0;
                case Z -> j = 1.0;
            }

            h /= 32.0;
            i /= 32.0;
            j /= 32.0;
            itemFrame.setBoundingBox(new Box(e - h, f - i, g - j, e + h, f + i, g + j));
        }
    }

}
