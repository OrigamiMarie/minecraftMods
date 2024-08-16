package net.origamimarie.minecraft.glass;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.origamimarie.minecraft.OrigamiMarieMod;
import net.origamimarie.minecraft.util.UnderscoreColors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.origamimarie.minecraft.util.UnderscoreColors.ALL_UNDERSCORE_COLORS;

public class OldConnectedGlassBlock extends TransparentBlock {
    public static final MapCodec<OldConnectedGlassBlock> CODEC = OldConnectedGlassBlock.createCodec(OldConnectedGlassBlock::new);
    private static final String CONNECTEDGLASS = "connectedglass";
    private static final String BORDERLESS = "borderless_";
    private static final String CLEAR = "clear_";
    private static final String SCRATCHED = "scratched_";
    private static final String TINTED_BORDERLESS = "tinted_borderless_";
    private static final String GLASS = "glass";
    private static final String GLASS_PANE = "glass_pane";
    private static final List<String> TRANSPARENT_CLARITIES = List.of(BORDERLESS, CLEAR, SCRATCHED);
    public static final Map<UnderscoreColors, List<Block>> NAME_TO_COLOR_BLOCKS = new HashMap<>();
    public static final Map<UnderscoreColors, Map<String, Map<String, Block>>> BLOCK_PATHS = new HashMap<>();

    public OldConnectedGlassBlock(Settings settings) {
        super(settings);
    }

    public MapCodec<OldConnectedGlassBlock> getCodec() {
        return CODEC;
    }

    public static void registerAll() {
        OrigamiMarieMod.LOGGER.info("Registering OldConnectedGlassBlock");
        Settings settings = Settings.create().instrument(NoteBlockInstrument.HAT).strength(0.3F).sounds(BlockSoundGroup.GLASS).allowsSpawning(Blocks::never).solidBlock(Blocks::never).suffocates(Blocks::never).blockVision(Blocks::never).nonOpaque().ticksRandomly();
        for (UnderscoreColors color : ALL_UNDERSCORE_COLORS) {
            BLOCK_PATHS.put(color, new HashMap<>());
            BLOCK_PATHS.get(color).put(GLASS, new HashMap<>());
            BLOCK_PATHS.get(color).put(GLASS_PANE, new HashMap<>());
            List<Block> blocksOfColor = new ArrayList<>();
            NAME_TO_COLOR_BLOCKS.put(color, blocksOfColor);
            Settings colorSettings = UnderscoreColors.copySettingsAndAddMapColor(settings, color.dyeColor);
            Identifier tintedGlassIdentifier = Identifier.of(CONNECTEDGLASS, color + TINTED_BORDERLESS + GLASS);
            OldTintedConnectedGlassBlock tintedGlassBlock = new OldTintedConnectedGlassBlock(colorSettings);
            blocksOfColor.add(tintedGlassBlock);
            BLOCK_PATHS.get(color).get(GLASS).put(TINTED_BORDERLESS, tintedGlassBlock);
            Registry.register(Registries.BLOCK, tintedGlassIdentifier, tintedGlassBlock);
            BlockRenderLayerMap.INSTANCE.putBlock(tintedGlassBlock, RenderLayer.getTranslucent());
            Item tintedGlassBlockItem = new BlockItem(tintedGlassBlock, new Item.Settings());
            Registry.register(Registries.ITEM, tintedGlassIdentifier, tintedGlassBlockItem);
            for (String clarity : TRANSPARENT_CLARITIES) {
                Identifier glassIdentifier = Identifier.of(CONNECTEDGLASS, color + clarity + GLASS);
                OldConnectedGlassBlock glassBlock = new OldConnectedGlassBlock(colorSettings);
                blocksOfColor.add(glassBlock);
                BLOCK_PATHS.get(color).get(GLASS).put(clarity, glassBlock);
                Registry.register(Registries.BLOCK, glassIdentifier, glassBlock);
                BlockRenderLayerMap.INSTANCE.putBlock(glassBlock, RenderLayer.getTranslucent());
                Item glassBlockItem = new BlockItem(glassBlock, new Item.Settings());
                Registry.register(Registries.ITEM, glassIdentifier, glassBlockItem);

                Identifier glassPaneIdentifier = Identifier.of(CONNECTEDGLASS, color + clarity + GLASS_PANE);
                OldConnectedGlassPaneBlock glassPaneBlock = new OldConnectedGlassPaneBlock(colorSettings);
                blocksOfColor.add(glassPaneBlock);
                BLOCK_PATHS.get(color).get(GLASS_PANE).put(clarity, glassPaneBlock);
                Registry.register(Registries.BLOCK, glassPaneIdentifier, glassPaneBlock);
                BlockRenderLayerMap.INSTANCE.putBlock(glassPaneBlock, RenderLayer.getTranslucent());
                Item glassPaneBlockItem = new BlockItem(glassPaneBlock, new Item.Settings());
                Registry.register(Registries.ITEM, glassPaneIdentifier, glassPaneBlockItem);
            }
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        Block block = ConnectedGlassBlock.TRANSLATION_MAP.get(state.getBlock());
        if (block != null) {
            world.setBlockState(pos, block.getDefaultState());
        }
    }

    public static class OldConnectedGlassPaneBlock extends PaneBlock {
        public static final MapCodec<OldConnectedGlassPaneBlock> CODEC = OldConnectedGlassPaneBlock.createCodec(OldConnectedGlassPaneBlock::new);

        public OldConnectedGlassPaneBlock(Settings settings) {
            super(settings);
        }

        public MapCodec<OldConnectedGlassPaneBlock> getCodec() {
            return CODEC;
        }

        @Override
        public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
            Block block = ConnectedGlassBlock.TRANSLATION_MAP.get(state.getBlock());
            if (block != null) {
                world.setBlockState(pos, block.getDefaultState());
            }
        }

    }

    public static class OldTintedConnectedGlassBlock extends TransparentBlock {
        public static final MapCodec<OldTintedConnectedGlassBlock> CODEC = OldTintedConnectedGlassBlock.createCodec(OldTintedConnectedGlassBlock::new);

        public OldTintedConnectedGlassBlock(Settings settings) {
            super(settings);
        }

        public MapCodec<OldTintedConnectedGlassBlock> getCodec() {
            return CODEC;
        }

        protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
            return false;
        }

        protected int getOpacity(BlockState state, BlockView world, BlockPos pos) {
            return world.getMaxLightLevel();
        }

        @Override
        public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
            Block block = ConnectedGlassBlock.TRANSLATION_MAP.get(state.getBlock());
            if (block != null) {
                world.setBlockState(pos, block.getDefaultState());
            }
        }

    }

}
