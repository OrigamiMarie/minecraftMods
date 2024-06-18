package net.origamimarie.minecraft.firtree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.block.enums.Instrument;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.origamimarie.minecraft.azalea.ModdedAzaleaBlock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import static net.origamimarie.minecraft.OrigamiMarieMod.ORIGAMIMARIE_MOD;


public class FirTree {

    private static final int MIN_TRUNK_HEIGHT = 8;
    private static final int MAX_TRUNK_HEIGHT = 32;
    private static final int UNDER_TRUNK_HEIGHT_THRESHOLD = 16;
    private static final int MIN_BRANCH_LENGTH = 2;
    private static final int MAX_BRANCH_LENGTH = 6;
    private static final double WIDEST_BRANCH_SPACING = 0.45; //0.5
    private static final double CLOSEST_BRANCH_SPACING = 0.2;
    private static final double PHI = ((1 + Math.sqrt(5)) / 2);
    private static final double ROTATION_ANGLE = (2 * Math.PI) - (2 * Math.PI / PHI);
    public static final LeavesBlock FIR_LEAVES = new LeavesBlock(AbstractBlock.Settings.copy(Blocks.SPRUCE_LEAVES));
    public static final Map<Integer, FirSaplingBlock> FIR_SAPLINGS = new HashMap<>();
    public static final Map<Integer, FlowerPotBlock> POTTED_FIR_SAPLINGS = new HashMap<>();
    public static final PillarBlock FIR_LOG = createLogBlock(MapColor.PALE_YELLOW, MapColor.OFF_WHITE);
    public static final PillarBlock FIR_WOOD = new PillarBlock(AbstractBlock.Settings.copy(Blocks.BIRCH_WOOD));

    private static final HashSet<Block> AIR_AND_LEAF_BLOCKS = new HashSet<>(ImmutableList.of(
            Blocks.AIR, Blocks.ACACIA_LEAVES, Blocks.BIRCH_LEAVES, Blocks.OAK_LEAVES, Blocks.DARK_OAK_LEAVES,
            Blocks.JUNGLE_LEAVES, Blocks.MANGROVE_LEAVES, Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES,
            Blocks.SPRUCE_LEAVES, Blocks.CHERRY_LEAVES, FIR_LEAVES, ModdedAzaleaBlock.PURPLE_AZALEA_LEAVES, ModdedAzaleaBlock.WHITE_AZALEA_LEAVES, ModdedAzaleaBlock.YELLOW_AZALEA_LEAVES));

    private static final Map<Integer, BlockState> LIT_CANDLE_STATES = ImmutableMap.of(
            1, Blocks.CANDLE.getDefaultState().with(CandleBlock.LIT, true).with(CandleBlock.CANDLES, 1),
            2, Blocks.CANDLE.getDefaultState().with(CandleBlock.LIT, true).with(CandleBlock.CANDLES, 2),
            3, Blocks.CANDLE.getDefaultState().with(CandleBlock.LIT, true).with(CandleBlock.CANDLES, 3),
            4, Blocks.CANDLE.getDefaultState().with(CandleBlock.LIT, true).with(CandleBlock.CANDLES, 4));

    public static void registerFirTree() {
        registerLeaves();
        registerSaplingsAndPottedSaplings();
        registerLog();
    }

    private static void registerLeaves() {
        Registry.register(Registries.BLOCK, new Identifier(ORIGAMIMARIE_MOD, "fir_leaves"), FIR_LEAVES);
        Registry.register(Registries.ITEM, new Identifier(ORIGAMIMARIE_MOD, "fir_leaves"), new BlockItem(FIR_LEAVES, new FabricItemSettings()));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> content.addAfter(Items.SPRUCE_LEAVES, FIR_LEAVES));
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> 0x80bb55, FIR_LEAVES);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> 0x80bb55, FIR_LEAVES);
        FlammableBlockRegistry.getDefaultInstance().add(FIR_LEAVES, 30, 60);
        CompostingChanceRegistry.INSTANCE.add(FIR_LEAVES, 0.3f);
    }

    private static void registerSaplingsAndPottedSaplings() {
        // Yes these are backwards.  It's the simplest way to put them in order in the creative block palette.
        FirSaplingBlock sapling = new FirSaplingBlock.FirFourCandleSaplingBlock();
        FlowerPotBlock pottedSapling = makePottedSapling(sapling);
        registerSaplingAndPottedSapling(sapling, pottedSapling, "fir_four_candle_sapling", 4);
        sapling = new FirSaplingBlock.FirThreeCandleSaplingBlock();
        pottedSapling = makePottedSapling(sapling);
        registerSaplingAndPottedSapling(sapling, pottedSapling, "fir_three_candle_sapling", 3);
        sapling = new FirSaplingBlock.FirTwoCandleSaplingBlock();
        pottedSapling = makePottedSapling(sapling);
        registerSaplingAndPottedSapling(sapling, pottedSapling, "fir_two_candle_sapling", 2);
        sapling = new FirSaplingBlock.FirOneCandleSaplingBlock();
        pottedSapling = makePottedSapling(sapling);
        registerSaplingAndPottedSapling(sapling, pottedSapling, "fir_one_candle_sapling", 1);
        sapling = new FirSaplingBlock();
        pottedSapling = makePottedSapling(sapling);
        registerSaplingAndPottedSapling(sapling, pottedSapling, "fir_sapling", 0);
    }

    private static FlowerPotBlock makePottedSapling(FirSaplingBlock sapling) {
        return new FlowerPotBlock(sapling, AbstractBlock.Settings.copy(Blocks.FLOWER_POT).luminance((state) -> sapling.getDefaultState().getLuminance()));
    }

    private static void registerSaplingAndPottedSapling(FirSaplingBlock sapling, FlowerPotBlock pottedSapling, String path, int candleCount) {
        FIR_SAPLINGS.put(candleCount, sapling);
        POTTED_FIR_SAPLINGS.put(candleCount, pottedSapling);
        Registry.register(Registries.BLOCK, new Identifier(ORIGAMIMARIE_MOD, path), sapling);
        Item saplingItem = new BlockItem(sapling, new FabricItemSettings());
        Registry.register(Registries.ITEM, new Identifier(ORIGAMIMARIE_MOD, path), saplingItem);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> content.addAfter(Items.SPRUCE_SAPLING, sapling));
        BlockRenderLayerMap.INSTANCE.putBlock(sapling, RenderLayer.getCutout());
        CompostingChanceRegistry.INSTANCE.add(sapling, 0.3f);
        Registry.register(Registries.BLOCK, new Identifier(ORIGAMIMARIE_MOD, "potted_" + path), pottedSapling);
        BlockRenderLayerMap.INSTANCE.putBlock(pottedSapling, RenderLayer.getCutout());
    }

    private static void registerLog() {
        Registry.register(Registries.BLOCK, new Identifier(ORIGAMIMARIE_MOD, "fir_log"), FIR_LOG);
        Item logItem = new BlockItem(FIR_LOG, new FabricItemSettings());
        Registry.register(Registries.ITEM, new Identifier(ORIGAMIMARIE_MOD, "fir_log"), logItem);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> content.addAfter(Items.SPRUCE_LOG, logItem));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> content.addAfter(Items.SPRUCE_BUTTON, logItem));
        FlammableBlockRegistry.getDefaultInstance().add(FIR_LOG, 5, 5);
        Registry.register(Registries.BLOCK, new Identifier(ORIGAMIMARIE_MOD, "fir_wood"), FIR_WOOD);
        Item woodItem = new BlockItem(FIR_WOOD, new FabricItemSettings());
        Registry.register(Registries.ITEM, new Identifier(ORIGAMIMARIE_MOD, "fir_wood"), woodItem);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> content.addAfter(logItem, woodItem));
        FlammableBlockRegistry.getDefaultInstance().add(FIR_WOOD, 5, 5);
    }

    private static PillarBlock createLogBlock(MapColor topMapColor, MapColor sideMapColor) {
        return new PillarBlock(AbstractBlock.Settings.create().mapColor((state) -> state.get(PillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor).instrument(Instrument.BASS).strength(2.0F).sounds(BlockSoundGroup.WOOD).burnable());
    }


    public static boolean makeTree(World world, BlockPos blockPos, int candleCount) {
        Random random = new Random();
        double x = blockPos.getX() + 0.5;
        double y = blockPos.getY();
        double z = blockPos.getZ() + 0.5;
        int[] maxRadiusAndHeight = calculateMaxRadiusAndHeight(x, y, z, world);
        // We can't make a minimum height tree here
        if (maxRadiusAndHeight[1] < MIN_TRUNK_HEIGHT) {
            return false;
        }
        int trunkHeight = Math.min(random.nextInt(MIN_TRUNK_HEIGHT, MAX_TRUNK_HEIGHT), maxRadiusAndHeight[1]+1);
        double currentBranchLength = makeBranchLength(random, trunkHeight, maxRadiusAndHeight[0]);
        double branchSpacing = random.nextDouble(CLOSEST_BRANCH_SPACING, WIDEST_BRANCH_SPACING);
        int branchStartHeight = trunkHeight < UNDER_TRUNK_HEIGHT_THRESHOLD ? 1 : 2;
        int branchCount = (int)((trunkHeight - branchStartHeight) / branchSpacing);
        double taperPerBranch = currentBranchLength / branchCount;
        int branchNumber = 0;
        // Make the trunk below the branches
        world.setBlockState(blockPosFromDoubles(x, y, z), FIR_LOG.getDefaultState());
        world.setBlockState(blockPosFromDoubles(x, y+1, z), FIR_LOG.getDefaultState());
        // Make all the branches, and fill in the trunk after each branch (in case the branch makes leaves in the trunk block)
        for (double i = branchStartHeight; i < trunkHeight; i = i + branchSpacing) {
            boolean madeExtendedBranch = makeBranch(x, (int)Math.floor(y+i), z, currentBranchLength, ROTATION_ANGLE * branchNumber, candleCount, world);
            // If we actually made branches that stuck out, turn the trunk into wood.
            // If not, we are almost certainly at the point of the tree, and we want it to stay green.
            BlockPos trunkBlockPos = blockPosFromDoubles(x, y + i, z);
            Block trunkBlockLocationCurrentBlock = world.getBlockState(trunkBlockPos).getBlock();
            if (madeExtendedBranch && (trunkBlockLocationCurrentBlock.equals(FIR_LEAVES) || trunkBlockLocationCurrentBlock.equals(Blocks.CANDLE))) {
                world.setBlockState(trunkBlockPos, FIR_LOG.getDefaultState());
            }
            branchNumber++;
            currentBranchLength = currentBranchLength - taperPerBranch;
        }
        return true;
    }

    private static double makeBranchLength(Random random, int trunkHeight, int maxLength) {
        int maxBranchLength = Math.min(trunkHeight / 3, maxLength);
        int minBranchLength = Math.max(trunkHeight / 8, MIN_BRANCH_LENGTH);
        if (maxBranchLength < minBranchLength) {
            int temp = maxBranchLength;
            maxBranchLength = minBranchLength;
            minBranchLength = temp;
        } else if (maxBranchLength == minBranchLength) {
            maxBranchLength = minBranchLength + 1;
        }
        return random.nextDouble(minBranchLength, maxBranchLength);
    }

    // If the branch extends out past the trunk, return true.
    private static boolean makeBranch(double trunkX, double y, double trunkZ, double length, double angle, int candleCount, World world) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        int leafBlockX = (int)Math.floor(trunkX);
        int leafBlockZ = (int)Math.floor(trunkZ);
        int roundedTrunkX = (int)Math.floor(trunkX);
        int roundedTrunkZ = (int)Math.floor(trunkZ);
        boolean branchExtends = false;
        BlockPos leafBlockPos;
        for (double distance = 0; distance <= length; distance = distance + 0.05) {
            leafBlockX = (int)Math.floor(trunkX + cos * distance);
            leafBlockZ = (int)Math.floor(trunkZ + sin * distance);
            if (leafBlockX !=roundedTrunkX || leafBlockZ != roundedTrunkZ) {
                branchExtends = true;
            }
            leafBlockPos = blockPosFromDoubles(leafBlockX, y, leafBlockZ);
            Block leafBlockLocationCurrentBlock = world.getBlockState(leafBlockPos).getBlock();
            if (leafBlockLocationCurrentBlock.equals(Blocks.AIR) || leafBlockLocationCurrentBlock.equals(Blocks.CANDLE)) {
                world.setBlockState(leafBlockPos, FirTree.FIR_LEAVES.getDefaultState());
            }
        }
        if (candleCount > 0) {
            BlockPos candleBlockPos = blockPosFromDoubles(leafBlockX, y + 1, leafBlockZ);
            world.setBlockState(candleBlockPos, LIT_CANDLE_STATES.get(candleCount));
        }
        return branchExtends;
    }

    private static BlockPos blockPosFromDoubles(double x, double y, double z) {
        return new BlockPos((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
    }

    private static int[] calculateMaxRadiusAndHeight(double trunkX, double trunkY, double trunkZ, World world) {
        int maxDiameter = 2 * MAX_BRANCH_LENGTH + 1;
        boolean[][] emptyBlocks = new boolean[maxDiameter][maxDiameter];
        int roundedTrunkX = (int)Math.floor(trunkX);
        int roundedTrunkY = (int)Math.floor(trunkY);
        int roundedTrunkZ = (int)Math.floor(trunkZ);
        int minX = roundedTrunkX - MAX_BRANCH_LENGTH;
        int minZ = roundedTrunkZ - MAX_BRANCH_LENGTH;
        populateAirAndLeafArray(emptyBlocks, maxDiameter, maxDiameter, minX, roundedTrunkY, minZ, world);
        int goodRadius = 0;
        // Get the maximum clear radius at one above ground level
        for (int r = 1; r < MAX_BRANCH_LENGTH + 1; r++) {
            int diameter = r*2 + 1;
            if (arrayAreaIsTrue(emptyBlocks, MAX_BRANCH_LENGTH - r, MAX_BRANCH_LENGTH - r, diameter, diameter)) {
                goodRadius = r;
            } else {
                break;
            }
        }
        // Get the maximum clear height at that radius
        int goodDiameter = goodRadius * 2 + 1;
        emptyBlocks = new boolean[goodDiameter][goodDiameter];
        int goodHeight = 0;
        for (int y = 1; y < MAX_TRUNK_HEIGHT; y++) {
            populateAirAndLeafArray(emptyBlocks, goodDiameter, goodDiameter, roundedTrunkX - goodRadius, roundedTrunkY + y, roundedTrunkZ - goodRadius, world);
            if (arrayAreaIsTrue(emptyBlocks, 0, 0, goodDiameter, goodDiameter)) {
                goodHeight = y;
            } else {
                break;
            }
        }
        // There's an off-by-one error in the height that I don't feel like fixing.
        return new int[] {goodRadius, goodHeight + 1};
    }

    private static void populateAirAndLeafArray(boolean[][] array, int width, int height, int minX, int y, int minZ, World world) {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                Block block = world.getBlockState(new BlockPos(x + minX, y + 1, z + minZ)).getBlock();
                array[x][z] = AIR_AND_LEAF_BLOCKS.contains(block);
            }
        }
    }

    private static boolean arrayAreaIsTrue(boolean[][] array, int minI, int minJ, int width, int height) {
        for (int i = minI; i < minI + width; i++) {
            for (int j = minJ; j < minJ + height; j++) {
                if (!array[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
