package net.origamimarie.minecraft.glass;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.AbstractBlock;
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
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.origamimarie.minecraft.util.UnderscoreColors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.origamimarie.minecraft.OrigamiMarieMod.ORIGAMIMARIE_MOD;
import static net.origamimarie.minecraft.util.UnderscoreColors.*;

// TODO fix codec so the real connectedglass blocks from old worlds actually load
// TODO maybe thicker panes with darker edges?
// TODO textures for clear glass
// TODO change tinted glass textures
// TODO shine on all surfaces of blocks & panes -- probably in the shader settings
public class ConnectedGlassBlock extends TransparentBlock {
    public static final MapCodec<ConnectedGlassBlock> CODEC = ConnectedGlassBlock.createCodec(ConnectedGlassBlock::new);
    private static final String BORDERLESS = "borderless_";
    private static final String CLEAR = "clear_";
    private static final String SCRATCHED = "scratched_";
    private static final String TINTED_BORDERLESS = "tinted_borderless_";
    private static final String GLASS = "glass";
    private static final String GLASS_PANE = "glass_pane";
    private static final List<String> TRANSPARENT_CLARITIES = List.of(BORDERLESS, CLEAR, SCRATCHED);
    public static final Map<UnderscoreColors, Map<String, Map<String, Block>>> BLOCK_PATHS = new HashMap<>();
    public static final Map<Block, Block> TRANSLATION_MAP = new HashMap<>();

    public ConnectedGlassBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    public MapCodec<ConnectedGlassBlock> getCodec() {
        return CODEC;
    }

    public static void registerAll() {
        Settings settings = Settings.create().instrument(NoteBlockInstrument.HAT).strength(0.3F).sounds(BlockSoundGroup.GLASS).allowsSpawning(Blocks::never).solidBlock(Blocks::never).suffocates(Blocks::never).blockVision(Blocks::never).nonOpaque();
        for (UnderscoreColors color : ALL_UNDERSCORE_COLORS) {
            BLOCK_PATHS.put(color, new HashMap<>());
            BLOCK_PATHS.get(color).put(GLASS, new HashMap<>());
            BLOCK_PATHS.get(color).put(GLASS_PANE, new HashMap<>());
            Settings colorSettings = UnderscoreColors.copySettingsAndAddMapColor(settings, color.dyeColor);
            Identifier tintedGlassIdentifier = Identifier.of(ORIGAMIMARIE_MOD, color + TINTED_BORDERLESS + GLASS);
            TintedConnectedGlassBlock tintedGlassBlock = new TintedConnectedGlassBlock(colorSettings);
            BLOCK_PATHS.get(color).get(GLASS).put(TINTED_BORDERLESS, tintedGlassBlock);
            Registry.register(Registries.BLOCK, tintedGlassIdentifier, tintedGlassBlock);
            BlockRenderLayerMap.INSTANCE.putBlock(tintedGlassBlock, RenderLayer.getTranslucent());
            Item tintedGlassBlockItem = new BlockItem(tintedGlassBlock, new Item.Settings());
            Registry.register(Registries.ITEM, tintedGlassIdentifier, tintedGlassBlockItem);

            for (String clarity : TRANSPARENT_CLARITIES) {
                Identifier glassIdentifier = Identifier.of(ORIGAMIMARIE_MOD, color + clarity + GLASS);
                ConnectedGlassBlock glassBlock = new ConnectedGlassBlock(colorSettings);
                BLOCK_PATHS.get(color).get(GLASS).put(clarity, glassBlock);
                Registry.register(Registries.BLOCK, glassIdentifier, glassBlock);
                BlockRenderLayerMap.INSTANCE.putBlock(glassBlock, RenderLayer.getTranslucent());
                Item glassBlockItem = new BlockItem(glassBlock, new Item.Settings());
                Registry.register(Registries.ITEM, glassIdentifier, glassBlockItem);

                Identifier glassPaneIdentifier = Identifier.of(ORIGAMIMARIE_MOD, color + clarity + GLASS_PANE);
                ConnectedGlassPaneBlock glassPaneBlock = new ConnectedGlassPaneBlock(colorSettings);
                BLOCK_PATHS.get(color).get(GLASS_PANE).put(clarity, glassPaneBlock);
                Registry.register(Registries.BLOCK, glassPaneIdentifier, glassPaneBlock);
                BlockRenderLayerMap.INSTANCE.putBlock(glassPaneBlock, RenderLayer.getTranslucent());
                Item glassPaneBlockItem = new BlockItem(glassPaneBlock, new Item.Settings());
                Registry.register(Registries.ITEM, glassPaneIdentifier, glassPaneBlockItem);
            }
        }

        for (UnderscoreColors color : BLOCK_PATHS.keySet()) {
            for (String pane : BLOCK_PATHS.get(color).keySet()) {
                for (String clarity : BLOCK_PATHS.get(color).get(pane).keySet()) {
                    TRANSLATION_MAP.put(OldConnectedGlassBlock.BLOCK_PATHS.get(color).get(pane).get(clarity), BLOCK_PATHS.get(color).get(pane).get(clarity));
                }
            }
        }

    }

    public static class ConnectedGlassPaneBlock extends PaneBlock {
        public static final MapCodec<ConnectedGlassPaneBlock> CODEC = ConnectedGlassPaneBlock.createCodec(ConnectedGlassPaneBlock::new);

        public ConnectedGlassPaneBlock(AbstractBlock.Settings settings) {
            super(settings);
        }

        public MapCodec<ConnectedGlassPaneBlock> getCodec() {
            return CODEC;
        }
    }

    public static class TintedConnectedGlassBlock extends TransparentBlock {
        public static final MapCodec<TintedConnectedGlassBlock> CODEC = TintedConnectedGlassBlock.createCodec(TintedConnectedGlassBlock::new);

        public TintedConnectedGlassBlock(AbstractBlock.Settings settings) {
            super(settings);
        }

        public MapCodec<TintedConnectedGlassBlock> getCodec() {
            return CODEC;
        }

        protected boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
            return false;
        }

        protected int getOpacity(BlockState state, BlockView world, BlockPos pos) {
            return world.getMaxLightLevel();
        }
    }

}
