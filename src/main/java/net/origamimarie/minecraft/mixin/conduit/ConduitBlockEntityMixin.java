package net.origamimarie.minecraft.mixin.conduit;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static net.origamimarie.minecraft.util.MixinBridge.CONDUIT_BLOCKS;

@Mixin(ConduitBlockEntity.class)
public abstract class ConduitBlockEntityMixin {

    private static final Long SCRUB_INTERVAL_MILLIS = 10 * 1000L;
    private static final AtomicLong lastScrubbedTime = new AtomicLong(System.currentTimeMillis());

    @Inject(method = "givePlayersEffects",
            at = @At(value = "HEAD", target = "Lnet/minecraft/block/entity/ConduitBlockEntity;givePlayersEffects(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/List;)V")
    )
    private static void givePlayersEffectsInject(World world, BlockPos pos, List<BlockPos> activatingBlocks, CallbackInfo ci) {
        // Add / update this block to the list of current conduits
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ConduitBlockEntity) {
            ConduitBlockEntityAccessor conduitBlockEntity = (ConduitBlockEntityAccessor) blockEntity;
            CONDUIT_BLOCKS.put(pos, Pair.of(16 *conduitBlockEntity.getActivatingBlocks().size() / 7, System.currentTimeMillis()));
        }

        // Scrub old conduits out of the list of current conduits
        long currentTime = System.currentTimeMillis();
        if (currentTime > lastScrubbedTime.getAndUpdate(x -> x + SCRUB_INTERVAL_MILLIS > currentTime ? x : currentTime) + SCRUB_INTERVAL_MILLIS) {
            Set<BlockPos> oldConduits = new HashSet<>();
            for (Map.Entry<BlockPos, Pair<Integer, Long>> entry : CONDUIT_BLOCKS.entrySet()) {
                if (entry.getValue().getRight() + SCRUB_INTERVAL_MILLIS < currentTime) {
                    oldConduits.add(entry.getKey());
                }
            }
            for (BlockPos conduit : oldConduits) {
                CONDUIT_BLOCKS.remove(conduit);
            }
        }
    }
}
