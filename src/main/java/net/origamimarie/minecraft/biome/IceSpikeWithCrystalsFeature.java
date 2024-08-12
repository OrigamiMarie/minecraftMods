package net.origamimarie.minecraft.biome;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.origamimarie.minecraft.rainbow_crystal.BuddingRainbowCrystalBlock;
import net.origamimarie.minecraft.rainbow_crystal.RainbowCrystalClusterBlock;
import net.origamimarie.minecraft.util.UnderscoreColors;

import java.util.LinkedList;
import java.util.List;

public class IceSpikeWithCrystalsFeature extends Feature<DefaultFeatureConfig> {
    public static final Identifier ICE_SPIKE_WITH_CRYSTALS_FEATURE_ID = Identifier.of("origamimarie_mod", "ice_spike_with_crystals");
    public static final IceSpikeWithCrystalsFeature ICE_SPIKE_WITH_CRYSTALS_FEATURE = new IceSpikeWithCrystalsFeature(DefaultFeatureConfig.CODEC);
    private static final UnderscoreColors CRYSTAL_COLOR = UnderscoreColors.BLUE_;
    private static final List<RainbowCrystalClusterBlock> CRYSTAL_OPTIONS = List.of(
            RainbowCrystalClusterBlock.getCrystal(CRYSTAL_COLOR, RainbowCrystalClusterBlock.WAXED, RainbowCrystalClusterBlock.DIM, RainbowCrystalClusterBlock.SMALL),
            RainbowCrystalClusterBlock.getCrystal(CRYSTAL_COLOR, RainbowCrystalClusterBlock.WAXED, RainbowCrystalClusterBlock.DIM, RainbowCrystalClusterBlock.MEDIUM),
            RainbowCrystalClusterBlock.getCrystal(CRYSTAL_COLOR, RainbowCrystalClusterBlock.WAXED, RainbowCrystalClusterBlock.DIM, RainbowCrystalClusterBlock.LARGE),
            RainbowCrystalClusterBlock.getCrystal(CRYSTAL_COLOR, RainbowCrystalClusterBlock.WAXED, RainbowCrystalClusterBlock.DIM, RainbowCrystalClusterBlock.CLUSTER)
    );
    private static final int CRYSTAL_COUNT = CRYSTAL_OPTIONS.size();

    public IceSpikeWithCrystalsFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    public static void register() {
        Registry.register(Registries.FEATURE, ICE_SPIKE_WITH_CRYSTALS_FEATURE_ID, ICE_SPIKE_WITH_CRYSTALS_FEATURE);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        int l;
        int k;
        BlockPos blockPos = context.getOrigin();
        Random random = context.getRandom();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        // Start from the sky and find the first solid block.
        while (structureWorldAccess.isAir(blockPos) && blockPos.getY() > structureWorldAccess.getBottomY() + 2) {
            blockPos = blockPos.down();
        }
        // Are we a snow block?  Only proceed if yes.
        if (!structureWorldAccess.getBlockState(blockPos).isOf(Blocks.SNOW_BLOCK)) {
            return false;
        }
        // Choose a random block anywhere from the snow block to 3 blocks up from it.
        blockPos = blockPos.up(random.nextInt(4));
        int i = random.nextInt(4) + 7;
        int j = i / 4 + random.nextInt(2);
        if (j > 1 && random.nextInt(60) == 0) {
            blockPos = blockPos.up(10 + random.nextInt(30));
        }
        // This is the base of the top cap.
        // It may be the same location as the first random block, or it may be quite a bit farther up.
        BlockPos tempBlockPos;
        List<BlockPos> allPlacedBlocks = new LinkedList<>();
        for (k = 0; k < i; ++k) {
            float f = (1.0f - (float)k / (float)i) * (float)j;
            l = MathHelper.ceil(f);
            for (int m = -l; m <= l; ++m) {
                float g = (float)MathHelper.abs(m) - 0.25f;
                for (int n = -l; n <= l; ++n) {
                    float h = (float)MathHelper.abs(n) - 0.25f;
                    if ((m != 0 || n != 0) && g * g + h * h > f * f || (m == -l || m == l || n == -l || n == l) && random.nextFloat() > 0.75f) continue;
                    BlockState blockState = structureWorldAccess.getBlockState(blockPos.add(m, k, n));
                    if (blockState.isAir() || IceSpikeWithCrystalsFeature.isSoil(blockState) || blockState.isOf(Blocks.SNOW_BLOCK) || blockState.isOf(Blocks.ICE)) {
                        tempBlockPos = blockPos.add(m, k, n);
                        allPlacedBlocks.add(tempBlockPos);
                        this.setBlockState(structureWorldAccess, tempBlockPos, Blocks.PACKED_ICE.getDefaultState());
                    }
                    if (k == 0 || l <= 1 || !(blockState = structureWorldAccess.getBlockState(blockPos.add(m, -k, n))).isAir() && !IceSpikeWithCrystalsFeature.isSoil(blockState) && !blockState.isOf(Blocks.SNOW_BLOCK) && !blockState.isOf(Blocks.ICE)) continue;
                    tempBlockPos = blockPos.add(m, -k, n);
                    allPlacedBlocks.add(tempBlockPos);
                    this.setBlockState(structureWorldAccess, tempBlockPos, Blocks.PACKED_ICE.getDefaultState());
                }
            }
        }
        k = j - 1;
        if (k < 0) {
            k = 0;
        } else if (k > 1) {
            k = 1;
        }
        for (int o = -k; o <= k; ++o) {
            for (l = -k; l <= k; ++l) {
                BlockState blockState2;
                BlockPos blockPos2 = blockPos.add(o, -1, l);
                int p = 50;
                if (Math.abs(o) == 1 && Math.abs(l) == 1) {
                    p = random.nextInt(5);
                }
                while (blockPos2.getY() > 50 && ((blockState2 = structureWorldAccess.getBlockState(blockPos2)).isAir() || IceSpikeWithCrystalsFeature.isSoil(blockState2) || blockState2.isOf(Blocks.SNOW_BLOCK) || blockState2.isOf(Blocks.ICE) || blockState2.isOf(Blocks.PACKED_ICE))) {
                    allPlacedBlocks.add(blockPos2);
                    this.setBlockState(structureWorldAccess, blockPos2, Blocks.PACKED_ICE.getDefaultState());
                    blockPos2 = blockPos2.down();
                    if (--p > 0) continue;
                    blockPos2 = blockPos2.down(random.nextInt(5) + 1);
                    p = random.nextInt(5);
                }
            }
        }

        for (BlockPos placedBlock : allPlacedBlocks) {
            placeCrystalOnTop(structureWorldAccess, placedBlock, CRYSTAL_OPTIONS.get(random.nextInt(CRYSTAL_COUNT)));
        }

        return true;
    }

    private void placeCrystalOnTop(StructureWorldAccess structureWorldAccess, BlockPos pos, Block crystal) {
        placeTopCrystalIfPossible(structureWorldAccess, pos, crystal);
    }

    private void placeTopCrystalIfPossible(StructureWorldAccess structureWorldAccess, BlockPos pos, Block crystal) {
        BlockPos crystalPos = pos.offset(Direction.UP);
        BlockState seedBlockPos = structureWorldAccess.getBlockState(crystalPos);
        BlockState crystalBlockPos = crystal.getDefaultState().with(RainbowCrystalClusterBlock.FACING, Direction.UP).with(RainbowCrystalClusterBlock.WATERLOGGED, seedBlockPos.getFluidState().getFluid() == Fluids.WATER);
        if (BuddingRainbowCrystalBlock.canGrowIn(seedBlockPos)) {
            this.setBlockState(structureWorldAccess, crystalPos, crystalBlockPos);
        }
    }
}
