package net.origamimarie.minecraft.rainbow_crystal;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.*;
import net.minecraft.fluid.Fluids;
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

import static net.origamimarie.minecraft.OrigamiMarieMod.ORIGAMIMARIE_MOD;

public class BuddingRainbowCrystalBlock extends AmethystBlock {
    public static final MapCodec<BuddingRainbowCrystalBlock> CODEC = BuddingAmethystBlock.createCodec(BuddingRainbowCrystalBlock::new);
    public static final int GROW_CHANCE = 5;
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final List<String> COLOR_ORDER = List.of("red", "lime", "yellow", "blue", "purple", "blue", "orange", "lime", "magenta", "cyan", "orange", "magenta", "orange", "red", "cyan", "purple", "lime", "cyan", "blue", "purple", "blue", "red", "yellow", "cyan", "yellow", "magenta", "orange", "yellow", "red", "lime", "magenta", "purple");

    public static void registerAll() {
        // strength similar to deepslate cobble, which should make it slow enough to mine that we don't do it accidentally
        BuddingRainbowCrystalBlock buddingRainbowCrystalBlock = new BuddingRainbowCrystalBlock(Settings.copy(Blocks.BUDDING_AMETHYST).strength(3.5f, 6.0f));
        Registry.register(Registries.BLOCK, new Identifier(ORIGAMIMARIE_MOD, "budding_rainbow_crystal"), buddingRainbowCrystalBlock);
        Item buddingRainbowCrystalItem = new BlockItem(buddingRainbowCrystalBlock, new FabricItemSettings());
        Registry.register(Registries.ITEM, new Identifier(ORIGAMIMARIE_MOD, "budding_rainbow_crystal"), buddingRainbowCrystalItem);
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
            block = RainbowCrystalClusterBlock.SMALL_RAINBOW_CRYSTAL_BUD_MAP.get(color);
        } else if (RainbowCrystalClusterBlock.RAINBOW_CRYSTAL_TRANSITION_MAP.containsKey(blockState.getBlock()) && blockState.get(RainbowCrystalClusterBlock.FACING) == direction) {
            block = RainbowCrystalClusterBlock.RAINBOW_CRYSTAL_TRANSITION_MAP.get(blockState.getBlock());
        }
        if (block != null) {
            BlockState blockState2 = block.getDefaultState().with(RainbowCrystalClusterBlock.FACING, direction).with(RainbowCrystalClusterBlock.WATERLOGGED, blockState.getFluidState().getFluid() == Fluids.WATER);
            world.setBlockState(blockPos, blockState2);
        }
    }

    public static boolean canGrowIn(BlockState state) {
        return state.isAir() || state.isOf(Blocks.WATER) && state.getFluidState().getLevel() == 8;
    }
}
