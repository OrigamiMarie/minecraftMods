package net.origamimarie.minecraft.azalea;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.SaplingGenerator;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TintedParticleLeavesBlock;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;

import java.util.Optional;
import java.util.function.Function;

import static net.origamimarie.minecraft.util.RegistrationMethods.registerBlock;


public abstract class ModdedAzaleaBlock extends PlantBlock implements Fertilizable {
    private static final VoxelShape SHAPE = VoxelShapes.union(Block.createCuboidShape(0.0, 8.0, 0.0, 16.0, 16.0, 16.0), Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 8.0, 10.0));
    public static final String LEAVES = "_leaves";
    public static final String PURPLE_FLOWERING_AZALEA = "purple_flowering_azalea";
    public static final String WHITE_FLOWERING_AZALEA = "white_flowering_azalea";
    public static final String YELLOW_FLOWERING_AZALEA = "yellow_flowering_azalea";
    public static final LeavesBlock PURPLE_AZALEA_LEAVES_BLOCK = registerBlock(PURPLE_FLOWERING_AZALEA + LEAVES, s -> new TintedParticleLeavesBlock(0.01F, s), Settings.copy(Blocks.AZALEA_LEAVES), true);
    public static final LeavesBlock WHITE_AZALEA_LEAVES_BLOCK = registerBlock(WHITE_FLOWERING_AZALEA + LEAVES, s -> new TintedParticleLeavesBlock(0.01F, s), Settings.copy(Blocks.AZALEA_LEAVES), true);
    public static final LeavesBlock YELLOW_AZALEA_LEAVES_BLOCK = registerBlock(YELLOW_FLOWERING_AZALEA + LEAVES, s -> new TintedParticleLeavesBlock(0.01F, s), Settings.copy(Blocks.AZALEA_LEAVES), true);
    public static final RegistryKey<ConfiguredFeature<?, ?>> PURPLE_AZALEA_TREE = ConfiguredFeatures.of("purple_azalea_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> WHITE_AZALEA_TREE = ConfiguredFeatures.of("white_azalea_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> YELLOW_AZALEA_TREE = ConfiguredFeatures.of("yellow_azalea_tree");
    public static final SaplingGenerator PURPLE_AZALEA_SAPLING_GENERATOR = new SaplingGenerator("purple_azalea_tree", Optional.empty(), Optional.of(PURPLE_AZALEA_TREE), Optional.empty());
    public static final SaplingGenerator WHITE_AZALEA_SAPLING_GENERATOR = new SaplingGenerator("white_azalea_tree", Optional.empty(), Optional.of(WHITE_AZALEA_TREE), Optional.empty());
    public static final SaplingGenerator YELLOW_AZALEA_SAPLING_GENERATOR = new SaplingGenerator("yellow_azalea_tree", Optional.empty(), Optional.of(YELLOW_AZALEA_TREE), Optional.empty());

    public ModdedAzaleaBlock(Settings settings) {
        super(settings);
    }

    public static void makeAndRegisterAll() {
        registerSaplingBlock(PURPLE_FLOWERING_AZALEA, PurpleAzaleaBlock::new, PURPLE_AZALEA_LEAVES_BLOCK);
        registerSaplingBlock(WHITE_FLOWERING_AZALEA, WhiteAzaleaBlock::new, WHITE_AZALEA_LEAVES_BLOCK);
        registerSaplingBlock(YELLOW_FLOWERING_AZALEA, YellowAzaleaBlock::new, YELLOW_AZALEA_LEAVES_BLOCK);
    }

    protected static <T extends ModdedAzaleaBlock> void registerSaplingBlock(String path, Function<Settings, T> azaleaConstructor, Block leavesBlock) {
        ModdedAzaleaBlock saplingBlock = registerBlock(path, azaleaConstructor, Settings.copy(Blocks.AZALEA_LEAVES), true);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> content.addAfter(Items.FLOWERING_AZALEA, saplingBlock));
        BlockRenderLayerMap.putBlock(saplingBlock, BlockRenderLayer.CUTOUT);
        CompostingChanceRegistry.INSTANCE.add(saplingBlock, 0.3f);

        FlowerPotBlock pottedSaplingBlock = registerBlock("potted_" + path + "_bush", s -> new FlowerPotBlock(saplingBlock, s), Settings.copy(Blocks.FLOWER_POT), false);
        BlockRenderLayerMap.putBlock(pottedSaplingBlock, BlockRenderLayer.CUTOUT);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> content.addAfter(Items.AZALEA_LEAVES, leavesBlock));
        FlammableBlockRegistry.getDefaultInstance().add(leavesBlock, 30, 60);
        CompostingChanceRegistry.INSTANCE.add(leavesBlock, 0.3f);
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(Blocks.CLAY) || super.canPlantOnTop(floor, world, pos);
    }

    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return world.getFluidState(pos.up()).isEmpty();
    }

    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return (double)world.random.nextFloat() < 0.45;
    }

    public static class PurpleAzaleaBlock extends ModdedAzaleaBlock {
        public MapCodec<PurpleAzaleaBlock> codec = createCodec(PurpleAzaleaBlock::new);

        public PurpleAzaleaBlock(Settings settings) {
            super(settings);
        }

        public MapCodec<PurpleAzaleaBlock> getCodec() {
            return codec;
        }

        @Override
        public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
            PURPLE_AZALEA_SAPLING_GENERATOR.generate(world, world.getChunkManager().getChunkGenerator(), pos, state, random);
        }

    }

    public static class WhiteAzaleaBlock extends ModdedAzaleaBlock {
        public MapCodec<WhiteAzaleaBlock> codec = createCodec(WhiteAzaleaBlock::new);

        public WhiteAzaleaBlock(Settings settings) {
            super(settings);
        }

        public MapCodec<WhiteAzaleaBlock> getCodec() {
            return codec;
        }

        @Override
        public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
            WHITE_AZALEA_SAPLING_GENERATOR.generate(world, world.getChunkManager().getChunkGenerator(), pos, state, random);
        }
    }

    public static class YellowAzaleaBlock extends ModdedAzaleaBlock {
        public MapCodec<YellowAzaleaBlock> codec = createCodec(YellowAzaleaBlock::new);

        public YellowAzaleaBlock(Settings settings) {
            super(settings);
        }

        public MapCodec<YellowAzaleaBlock> getCodec() {
            return codec;
        }

        @Override
        public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
            YELLOW_AZALEA_SAPLING_GENERATOR.generate(world, world.getChunkManager().getChunkGenerator(), pos, state, random);
        }
    }

}
