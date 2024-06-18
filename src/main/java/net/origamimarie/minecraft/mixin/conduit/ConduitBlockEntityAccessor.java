package net.origamimarie.minecraft.mixin.conduit;

import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ConduitBlockEntity.class)
public interface ConduitBlockEntityAccessor {
    @Accessor
    List<BlockPos> getActivatingBlocks();
}
