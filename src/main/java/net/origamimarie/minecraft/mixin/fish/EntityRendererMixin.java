package net.origamimarie.minecraft.mixin.fish;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(method = "getBlockLight", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/client/render/entity/EntityRenderer;getBlockLight(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;)I"))
    protected void getBlockLight(T entity, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (entity instanceof TropicalFishEntity || entity instanceof AxolotlEntity) {
            cir.setReturnValue(15);
            cir.cancel();
        }
    }
}
