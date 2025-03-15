package net.origamimarie.minecraft.rainbow_crystal;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AmethystBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.origamimarie.minecraft.util.UnderscoreColors;

import java.util.Map;

import static net.origamimarie.minecraft.rainbow_crystal.RainbowCrystalClusterBlock.*;
import static net.origamimarie.minecraft.util.RegistrationMethods.registerBlock;
import static net.origamimarie.minecraft.util.UnderscoreColors.*;

public class BuddingRainbowCrystalBlock extends AmethystBlock {
    public static final MapCodec<BuddingRainbowCrystalBlock> CODEC = BuddingRainbowCrystalBlock.createCodec(BuddingRainbowCrystalBlock::new);
    public static final int GROW_CHANCE = 5;
    public static final Direction[] DIRECTIONS = Direction.values();
    // strength similar to deepslate cobble, which should make it slow enough to mine that we don't do it accidentally
    private static final BuddingRainbowCrystalBlock LIGHT_BLOCK = registerBlock("budding_rainbow_crystal", BuddingRainbowCrystalBlock::new,
            Settings.copy(Blocks.AMETHYST_BLOCK).ticksRandomly().strength(3.5f, 6.0f).mapColor(MapColor.TERRACOTTA_WHITE),
            true);
    private static final DimBuddingRainbowCrystalBlock DIM_BLOCK = registerBlock("dim_budding_rainbow_crystal", DimBuddingRainbowCrystalBlock::new,
            Settings.copy(Blocks.AMETHYST_BLOCK).ticksRandomly().strength(3.5f, 6.0f).mapColor(MapColor.BLACK),
            true);

    public static void registerAll() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> content.addAfter(Items.AMETHYST_CLUSTER, LIGHT_BLOCK));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> content.addAfter(Items.AMETHYST_CLUSTER, DIM_BLOCK));
    }

    public MapCodec<BuddingRainbowCrystalBlock> getCodec() {
        return CODEC;
    }

    public BuddingRainbowCrystalBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        performRandomTick(world, pos, SMALL_RAINBOW_CRYSTAL_BUD_MAP, random);
    }

    protected void performRandomTick(ServerWorld world, BlockPos pos, Map<UnderscoreColors, RainbowCrystalClusterBlock> crystalsToSelectFrom, Random random) {
        if (random.nextInt(GROW_CHANCE) != 0) {
            return;
        }
        Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos blockPos = pos.offset(direction);
        BlockState blockState = world.getBlockState(blockPos);
        Block block = null;
        if (BuddingRainbowCrystalBlock.canGrowIn(blockState)) {
            int crystalColorIndex = Math.abs(("" + pos.hashCode()).hashCode() & 31);
            UnderscoreColors color = RAINBOW_PSEUDO_RANDOM.get(crystalColorIndex);
            block = crystalsToSelectFrom.get(color);
        } else if (RainbowCrystalClusterBlock.RAINBOW_CRYSTAL_GROW_MAP.containsKey(blockState.getBlock()) && blockState.get(RainbowCrystalClusterBlock.FACING) == direction) {
            block = RainbowCrystalClusterBlock.RAINBOW_CRYSTAL_GROW_MAP.get(blockState.getBlock());
        }
        if (block != null) {
            BlockState blockState2 = block.getDefaultState().with(RainbowCrystalClusterBlock.FACING, direction).with(RainbowCrystalClusterBlock.WATERLOGGED, blockState.getFluidState().getLevel() == 8);
            world.setBlockState(blockPos, blockState2);
        }
    }

    public static boolean canGrowIn(BlockState state) {
        return state.isAir() || state.isOf(Blocks.WATER);
    }

    private static class DimBuddingRainbowCrystalBlock extends BuddingRainbowCrystalBlock {
        public DimBuddingRainbowCrystalBlock(Settings settings) {
            super(settings);
        }

        @Override
        public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
            performRandomTick(world, pos, DIM_SMALL_RAINBOW_CRYSTAL_BUD_MAP, random);
        }

    }
}
