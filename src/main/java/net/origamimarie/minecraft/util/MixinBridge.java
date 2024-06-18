package net.origamimarie.minecraft.util;

import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class MixinBridge {
    public static final Map<BlockPos, Pair<Integer, Long>> CONDUIT_BLOCKS = new HashMap<>();

    public static boolean isLocationWithinAnyConduitRanges(BlockPos pos) {
        for (Map.Entry<BlockPos, Pair<Integer, Long>> entry : CONDUIT_BLOCKS.entrySet()) {
            if (pos.isWithinDistance(entry.getKey(), entry.getValue().getLeft())) {
                return true;
            }
        }
        return false;
    }
}
