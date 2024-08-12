package net.origamimarie.minecraft.glass;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.AbstractBlock;
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

import java.util.List;

import static net.origamimarie.minecraft.util.UnderscoreColors.*;

// TODO textures for clear glass
// TODO shine on all surfaces of blocks & panes -- probably in the shader settings
// TODO glass pathname converter
public class ConnectedGlassBlock extends TransparentBlock {
    public static final MapCodec<ConnectedGlassBlock> CODEC = ConnectedGlassBlock.createCodec(ConnectedGlassBlock::new);
    private static final String CONNECTEDGLASS = "origamimarie_mod";
    private static final String BORDERLESS = "borderless_";
    private static final String CLEAR = "clear_";
    private static final String SCRATCHED = "scratched_";
    private static final String TINTED_BORDERLESS = "tinted_borderless_";
    private static final List<String> TRANSPARENT_CLARITIES = List.of(BORDERLESS, CLEAR, SCRATCHED);

    public ConnectedGlassBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    public MapCodec<ConnectedGlassBlock> getCodec() {
        return CODEC;
    }

    public static void registerAll() {
        Settings settings = Settings.create().instrument(NoteBlockInstrument.HAT).strength(0.3F).sounds(BlockSoundGroup.GLASS).allowsSpawning(Blocks::never).solidBlock(Blocks::never).suffocates(Blocks::never).blockVision(Blocks::never).nonOpaque();
        for (UnderscoreColors color : ALL_UNDERSCORE_COLORS) {
            Settings colorSettings = UnderscoreColors.copySettingsAndAddMapColor(settings, color.dyeColor);
            Identifier tintedGlassIdentifier = Identifier.of(CONNECTEDGLASS, color + TINTED_BORDERLESS + "glass");
            TintedConnectedGlassBlock tintedGlassBlock = new TintedConnectedGlassBlock(colorSettings);
            Registry.register(Registries.BLOCK, tintedGlassIdentifier, tintedGlassBlock);
            BlockRenderLayerMap.INSTANCE.putBlock(tintedGlassBlock, RenderLayer.getTranslucent());
            Item tintedGlassBlockItem = new BlockItem(tintedGlassBlock, new Item.Settings());
            Registry.register(Registries.ITEM, tintedGlassIdentifier, tintedGlassBlockItem);

            for (String clarity : TRANSPARENT_CLARITIES) {
                Identifier glassIdentifier = Identifier.of(CONNECTEDGLASS, color + clarity + "glass");
                ConnectedGlassBlock glassBlock = new ConnectedGlassBlock(colorSettings);
                Registry.register(Registries.BLOCK, glassIdentifier, glassBlock);
                BlockRenderLayerMap.INSTANCE.putBlock(glassBlock, RenderLayer.getTranslucent());
                Item glassBlockItem = new BlockItem(glassBlock, new Item.Settings());
                Registry.register(Registries.ITEM, glassIdentifier, glassBlockItem);

                Identifier glassPaneIdentifier = Identifier.of(CONNECTEDGLASS, color + clarity + "glass_pane");
                ConnectedGlassPaneBlock glassPaneBlock = new ConnectedGlassPaneBlock(colorSettings);
                Registry.register(Registries.BLOCK, glassPaneIdentifier, glassPaneBlock);
                BlockRenderLayerMap.INSTANCE.putBlock(glassPaneBlock, RenderLayer.getTranslucent());
                Item glassPaneBlockItem = new BlockItem(glassPaneBlock, new Item.Settings());
                Registry.register(Registries.ITEM, glassPaneIdentifier, glassPaneBlockItem);
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
