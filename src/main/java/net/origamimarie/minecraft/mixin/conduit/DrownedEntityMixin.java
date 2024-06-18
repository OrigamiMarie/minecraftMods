package net.origamimarie.minecraft.mixin.conduit;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.origamimarie.minecraft.util.MixinBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DrownedEntity.class)
public class DrownedEntityMixin {

    @Inject(method = "canSpawn(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)Z",
            cancellable = true,
            at = @At(value = "HEAD", target = "Lnet/minecraft/entity/mob/DrownedEntity;canSpawn(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)Z"))
    private static void startOfCanSpawn(EntityType<DrownedEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir) {
        if (MixinBridge.isLocationWithinAnyConduitRanges(pos)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
