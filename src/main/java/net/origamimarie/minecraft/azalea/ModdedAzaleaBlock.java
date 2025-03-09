package net.origamimarie.minecraft.azalea;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.SaplingGenerator;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
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

import static net.origamimarie.minecraft.OrigamiMarieMod.ORIGAMIMARIE_MOD;


public abstract class ModdedAzaleaBlock extends PlantBlock implements Fertilizable {
    private static final VoxelShape SHAPE = VoxelShapes.union(Block.createCuboidShape(0.0, 8.0, 0.0, 16.0, 16.0, 16.0), Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 8.0, 10.0));
    public static final LeavesBlock PURPLE_AZALEA_LEAVES = new LeavesBlock(AbstractBlock.Settings.copy(Blocks.AZALEA_LEAVES));
    public static final LeavesBlock WHITE_AZALEA_LEAVES = new LeavesBlock(AbstractBlock.Settings.copy(Blocks.AZALEA_LEAVES));
    public static final LeavesBlock YELLOW_AZALEA_LEAVES = new LeavesBlock(AbstractBlock.Settings.copy(Blocks.AZALEA_LEAVES));
    public static final RegistryKey<ConfiguredFeature<?, ?>> PURPLE_AZALEA_TREE = ConfiguredFeatures.of("purple_azalea_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> WHITE_AZALEA_TREE = ConfiguredFeatures.of("white_azalea_tree");
    public static final RegistryKey<ConfiguredFeature<?, ?>> YELLOW_AZALEA_TREE = ConfiguredFeatures.of("yellow_azalea_tree");
    public static final SaplingGenerator PURPLE_AZALEA_SAPLING_GENERATOR = new SaplingGenerator("purple_azalea_tree", Optional.empty(), Optional.of(PURPLE_AZALEA_TREE), Optional.empty());
    public static final SaplingGenerator WHITE_AZALEA_SAPLING_GENERATOR = new SaplingGenerator("white_azalea_tree", Optional.empty(), Optional.of(WHITE_AZALEA_TREE), Optional.empty());
    public static final SaplingGenerator YELLOW_AZALEA_SAPLING_GENERATOR = new SaplingGenerator("yellow_azalea_tree", Optional.empty(), Optional.of(YELLOW_AZALEA_TREE), Optional.empty());

    public ModdedAzaleaBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    public static void makeAndRegisterAll() {
        PurpleAzaleaBlock purpleAzaleaBlock = new PurpleAzaleaBlock(Settings.copy(Blocks.AZALEA_LEAVES));
        purpleAzaleaBlock.registerSelf();
        WhiteAzaleaBlock whiteAzaleaBlock = new WhiteAzaleaBlock(Settings.copy(Blocks.AZALEA_LEAVES));
        whiteAzaleaBlock.registerSelf();
        YellowAzaleaBlock yellowAzaleaBlock = new YellowAzaleaBlock(Settings.copy(Blocks.AZALEA_LEAVES));
        yellowAzaleaBlock.registerSelf();
    }

    protected void registerSaplingBlock(String path) {
        FlowerPotBlock pottedSapling = new FlowerPotBlock(this, AbstractBlock.Settings.copy(Blocks.FLOWER_POT));
        Registry.register(Registries.BLOCK, Identifier.of(ORIGAMIMARIE_MOD, path), this);
        Item saplingItem = new BlockItem(this, new Item.Settings());
        Registry.register(Registries.ITEM, Identifier.of(ORIGAMIMARIE_MOD, path), saplingItem);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> content.addAfter(Items.FLOWERING_AZALEA, saplingItem));
        BlockRenderLayerMap.INSTANCE.putBlock(this, RenderLayer.getCutout());
        CompostingChanceRegistry.INSTANCE.add(saplingItem, 0.3f);
        Registry.register(Registries.BLOCK, Identifier.of(ORIGAMIMARIE_MOD, "potted_" + path + "_bush"), pottedSapling);
        BlockRenderLayerMap.INSTANCE.putBlock(pottedSapling, RenderLayer.getCutout());
    }

    protected void registerLeavesBlock(LeavesBlock blockToRegister, String path) {
        Registry.register(Registries.BLOCK, Identifier.of(ORIGAMIMARIE_MOD, path), blockToRegister);
        Registry.register(Registries.ITEM, Identifier.of(ORIGAMIMARIE_MOD, path), new BlockItem(blockToRegister, new Item.Settings()));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> content.addAfter(Items.AZALEA_LEAVES, blockToRegister));
        FlammableBlockRegistry.getDefaultInstance().add(blockToRegister, 30, 60);
        CompostingChanceRegistry.INSTANCE.add(blockToRegister, 0.3f);
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

        public void registerSelf() {
            registerSaplingBlock("purple_flowering_azalea");
            registerLeavesBlock(PURPLE_AZALEA_LEAVES, "purple_flowering_azalea_leaves");
        }

        @Override
        /*public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
            // AZALEA = new SaplingGenerator("azalea", Optional.empty(), Optional.of(TreeConfiguredFeatures.AZALEA_TREE), Optional.empty());
            new ModdedAzaleaSaplingGenerators.PurpleAzaleaSaplingGenerator().generate(world, world.getChunkManager().getChunkGenerator(), pos, state, random);
        }*/
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

        public void registerSelf() {
            registerSaplingBlock("white_flowering_azalea");
            registerLeavesBlock(WHITE_AZALEA_LEAVES, "white_flowering_azalea_leaves");
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

        public void registerSelf() {
            registerSaplingBlock("yellow_flowering_azalea");
            registerLeavesBlock(YELLOW_AZALEA_LEAVES, "yellow_flowering_azalea_leaves");
        }

        @Override
        public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
            YELLOW_AZALEA_SAPLING_GENERATOR.generate(world, world.getChunkManager().getChunkGenerator(), pos, state, random);
        }
    }

}
