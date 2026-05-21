package net.origamimarie.minecraft.mixin.itemframe;

import net.minecraft.entity.decoration.BlockAttachedEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockAttachedEntity.class)
public interface BlockAttachedEntityAccessor {
    @Accessor
    BlockPos getAttachedBlockPos();
}
