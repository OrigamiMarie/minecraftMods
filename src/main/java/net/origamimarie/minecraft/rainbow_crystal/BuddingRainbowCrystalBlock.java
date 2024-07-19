package net.origamimarie.minecraft.rainbow_crystal;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AmethystBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BuddingAmethystBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import java.util.List;
import java.util.Map;

import static net.origamimarie.minecraft.OrigamiMarieMod.ORIGAMIMARIE_MOD;
import static net.origamimarie.minecraft.rainbow_crystal.RainbowCrystalClusterBlock.*;

public class BuddingRainbowCrystalBlock extends AmethystBlock {
    public static final MapCodec<BuddingRainbowCrystalBlock> CODEC = BuddingAmethystBlock.createCodec(BuddingRainbowCrystalBlock::new);
    public static final int GROW_CHANCE = 5;
    public static final Direction[] DIRECTIONS = Direction.values();
    private static final List<String> COLOR_ORDER = List.of(RED, LIME, YELLOW, BLUE, PURPLE, BLUE, ORANGE, LIME, MAGENTA, CYAN, ORANGE, MAGENTA, ORANGE, RED, CYAN, PURPLE, LIME, CYAN, BLUE, PURPLE, BLUE, RED, YELLOW, CYAN, YELLOW, MAGENTA, ORANGE, YELLOW, RED, LIME, MAGENTA, PURPLE);
    private static final BuddingRainbowCrystalBlock LIGHT_BLOCK = new BuddingRainbowCrystalBlock(Settings.copy(Blocks.BUDDING_AMETHYST).strength(3.5f, 6.0f));
    private static final DimBuddingRainbowCrystalBlock DIM_BLOCK = new DimBuddingRainbowCrystalBlock(Settings.copy(Blocks.BUDDING_AMETHYST).strength(3.5f, 6.0f));

    public static void registerAll() {
        // strength similar to deepslate cobble, which should make it slow enough to mine that we don't do it accidentally
        Registry.register(Registries.BLOCK, Identifier.of(ORIGAMIMARIE_MOD, "budding_rainbow_crystal"), LIGHT_BLOCK);
        Item buddingRainbowCrystalItem = new BlockItem(LIGHT_BLOCK, new Item.Settings());
        Registry.register(Registries.ITEM, Identifier.of(ORIGAMIMARIE_MOD, "budding_rainbow_crystal"), buddingRainbowCrystalItem);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> content.addAfter(Items.AMETHYST_CLUSTER, buddingRainbowCrystalItem));

        Registry.register(Registries.BLOCK, Identifier.of(ORIGAMIMARIE_MOD, "dim_budding_rainbow_crystal"), DIM_BLOCK);
        Item dimBuddingRainbowCrystalItem = new BlockItem(DIM_BLOCK, new Item.Settings());
        Registry.register(Registries.ITEM, Identifier.of(ORIGAMIMARIE_MOD, "dim_budding_rainbow_crystal"), dimBuddingRainbowCrystalItem);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> content.addAfter(Items.AMETHYST_CLUSTER, buddingRainbowCrystalItem));
    }

    public MapCodec<BuddingRainbowCrystalBlock> getCodec() {
        return CODEC;
    }

    public BuddingRainbowCrystalBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        performRandomTick(world, pos, SMALL_RAINBOW_CRYSTAL_BUD_MAP, random);
    }

    protected void performRandomTick(ServerWorld world, BlockPos pos, Map<String, RainbowCrystalClusterBlock> crystalsToSelectFrom, Random random) {
        if (random.nextInt(GROW_CHANCE) != 0) {
            return;
        }
        Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos blockPos = pos.offset(direction);
        BlockState blockState = world.getBlockState(blockPos);
        Block block = null;
        if (BuddingRainbowCrystalBlock.canGrowIn(blockState)) {
            int crystalColorIndex = Math.abs(("" + pos.hashCode()).hashCode() & 31);
            String color = COLOR_ORDER.get(crystalColorIndex);
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
        public DimBuddingRainbowCrystalBlock(AbstractBlock.Settings settings) {
            super(settings);
        }

        @Override
        public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
            performRandomTick(world, pos, DIM_SMALL_RAINBOW_CRYSTAL_BUD_MAP, random);
        }

    }
}
