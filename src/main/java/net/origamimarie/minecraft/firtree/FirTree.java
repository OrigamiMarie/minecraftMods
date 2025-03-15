package net.origamimarie.minecraft.firtree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.origamimarie.minecraft.azalea.ModdedAzaleaBlock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import static net.origamimarie.minecraft.util.RegistrationMethods.registerBlock;

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
    private static final String FIR_LOG = "fir_log";
    private static final String FIR_WOOD = "fir_wood";
    private static final String FIR_LEAVES = "fir_leaves";
    public static final Block FIR_LEAVES_BLOCK = registerBlock(FIR_LEAVES, LeavesBlock::new, Settings.copy(Blocks.SPRUCE_LEAVES), true);
    public static final PillarBlock FIR_LOG_BLOCK = registerBlock(FIR_LOG, PillarBlock::new, createLogSettings(MapColor.PALE_YELLOW, MapColor.OFF_WHITE, BlockSoundGroup.WOOD), true);
    public static final PillarBlock FIR_WOOD_BLOCK = registerBlock(FIR_WOOD, PillarBlock::new, Settings.copy(Blocks.BIRCH_WOOD), true);

    public static final Map<Integer, FirSaplingBlock> FIR_SAPLINGS = new HashMap<>();
    public static final Map<Integer, FlowerPotBlock> POTTED_FIR_SAPLINGS = new HashMap<>();

    private static final HashSet<Block> AIR_AND_LEAF_BLOCKS = new HashSet<>(ImmutableList.of(
            Blocks.AIR, Blocks.ACACIA_LEAVES, Blocks.BIRCH_LEAVES, Blocks.OAK_LEAVES, Blocks.DARK_OAK_LEAVES,
            Blocks.JUNGLE_LEAVES, Blocks.MANGROVE_LEAVES, Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES,
            Blocks.SPRUCE_LEAVES, Blocks.CHERRY_LEAVES, FIR_LEAVES_BLOCK, ModdedAzaleaBlock.PURPLE_AZALEA_LEAVES_BLOCK,
            ModdedAzaleaBlock.WHITE_AZALEA_LEAVES_BLOCK, ModdedAzaleaBlock.YELLOW_AZALEA_LEAVES_BLOCK));

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
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> content.addAfter(Items.SPRUCE_LEAVES, FIR_LEAVES_BLOCK));
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> 0x80bb55, FIR_LEAVES_BLOCK);
        FlammableBlockRegistry.getDefaultInstance().add(FIR_LEAVES_BLOCK, 30, 60);
        CompostingChanceRegistry.INSTANCE.add(FIR_LEAVES_BLOCK, 0.3f);
    }

    private static void registerSaplingsAndPottedSaplings() {
        // Yes these are backwards.  It's the simplest way to put them in order in the creative block palette.
        FlowerPotBlock pottedSapling;
        String name;
        String potted = "potted_";
        name = "fir_four_candle_sapling";
        FirSaplingBlock fourCandleSapling = registerBlock(name, FirSaplingBlock.FirFourCandleSaplingBlock::new, Settings.copy(Blocks.SPRUCE_SAPLING), true);
        pottedSapling = registerBlock(potted + name, s -> new FlowerPotBlock(fourCandleSapling, s), Settings.copy(Blocks.FLOWER_POT).luminance((state) -> fourCandleSapling.getDefaultState().getLuminance()), false);
        registerSaplingAndPottedSapling(fourCandleSapling, pottedSapling);
        name = "fir_three_candle_sapling";
        FirSaplingBlock threeCandleSapling = registerBlock(name, FirSaplingBlock.FirThreeCandleSaplingBlock::new, Settings.copy(Blocks.SPRUCE_SAPLING), true);
        pottedSapling = registerBlock(potted + name, s -> new FlowerPotBlock(threeCandleSapling, s), Settings.copy(Blocks.FLOWER_POT).luminance((state) -> threeCandleSapling.getDefaultState().getLuminance()), false);
        registerSaplingAndPottedSapling(threeCandleSapling, pottedSapling);
        name = "fir_two_candle_sapling";
        FirSaplingBlock twoCandleSapling = registerBlock(name, FirSaplingBlock.FirTwoCandleSaplingBlock::new, Settings.copy(Blocks.SPRUCE_SAPLING), true);
        pottedSapling = registerBlock(potted + name, s -> new FlowerPotBlock(twoCandleSapling, s), Settings.copy(Blocks.FLOWER_POT).luminance((state) -> twoCandleSapling.getDefaultState().getLuminance()), false);
        registerSaplingAndPottedSapling(twoCandleSapling, pottedSapling);
        name = "fir_one_candle_sapling";
        FirSaplingBlock oneCandleSapling = registerBlock(name, FirSaplingBlock.FirOneCandleSaplingBlock::new, Settings.copy(Blocks.SPRUCE_SAPLING), true);
        pottedSapling = registerBlock(potted + name, s -> new FlowerPotBlock(oneCandleSapling, s), Settings.copy(Blocks.FLOWER_POT).luminance((state) -> oneCandleSapling.getDefaultState().getLuminance()), false);
        registerSaplingAndPottedSapling(oneCandleSapling, pottedSapling);
        name = "fir_sapling";
        FirSaplingBlock sapling = registerBlock(name, FirSaplingBlock::new, Settings.copy(Blocks.SPRUCE_SAPLING), true);
        pottedSapling = registerBlock(potted + name, s -> new FlowerPotBlock(sapling, s), Settings.copy(Blocks.FLOWER_POT).luminance((state) -> sapling.getDefaultState().getLuminance()), false);
        registerSaplingAndPottedSapling(sapling, pottedSapling);
    }

    private static void registerSaplingAndPottedSapling(FirSaplingBlock saplingBlock, FlowerPotBlock pottedSapling) {
        int candleCount = saplingBlock.candleCount;
        FIR_SAPLINGS.put(candleCount, saplingBlock);
        POTTED_FIR_SAPLINGS.put(candleCount, pottedSapling);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> content.addAfter(Items.SPRUCE_SAPLING, saplingBlock));
        BlockRenderLayerMap.INSTANCE.putBlock(saplingBlock, RenderLayer.getCutout());
        CompostingChanceRegistry.INSTANCE.add(saplingBlock, 0.3f);
        BlockRenderLayerMap.INSTANCE.putBlock(pottedSapling, RenderLayer.getCutout());
    }

    private static void registerLog() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> content.addAfter(Items.SPRUCE_LOG, FIR_LOG_BLOCK));
        FlammableBlockRegistry.getDefaultInstance().add(FIR_LOG_BLOCK, 5, 5);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> content.addAfter(Items.SPRUCE_WOOD, FIR_WOOD_BLOCK));
        FlammableBlockRegistry.getDefaultInstance().add(FIR_WOOD_BLOCK, 5, 5);
    }

    public static Settings createLogSettings(MapColor topMapColor, MapColor sideMapColor, BlockSoundGroup sounds) {
        return Settings.create().mapColor((state) -> state.get(PillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor).
                instrument(NoteBlockInstrument.BASS).strength(2.0F).sounds(sounds).burnable();
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
        world.setBlockState(blockPosFromDoubles(x, y, z), FIR_LOG_BLOCK.getDefaultState());
        world.setBlockState(blockPosFromDoubles(x, y+1, z), FIR_LOG_BLOCK.getDefaultState());
        // Make all the branches, and fill in the trunk after each branch (in case the branch makes leaves in the trunk block)
        for (double i = branchStartHeight; i < trunkHeight; i = i + branchSpacing) {
            boolean madeExtendedBranch = makeBranch(x, (int)Math.floor(y+i), z, currentBranchLength, ROTATION_ANGLE * branchNumber, candleCount, world);
            // If we actually made branches that stuck out, turn the trunk into wood.
            // If not, we are almost certainly at the point of the tree, and we want it to stay green.
            BlockPos trunkBlockPos = blockPosFromDoubles(x, y + i, z);
            Block trunkBlockLocationCurrentBlock = world.getBlockState(trunkBlockPos).getBlock();
            if (madeExtendedBranch && (trunkBlockLocationCurrentBlock.equals(FIR_LEAVES_BLOCK) || trunkBlockLocationCurrentBlock.equals(Blocks.CANDLE))) {
                world.setBlockState(trunkBlockPos, FIR_LOG_BLOCK.getDefaultState());
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
                world.setBlockState(leafBlockPos, FirTree.FIR_LEAVES_BLOCK.getDefaultState());
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
