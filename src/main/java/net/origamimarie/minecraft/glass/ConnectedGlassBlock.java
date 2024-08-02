package net.origamimarie.minecraft.glass;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.AbstractBlock;
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

import java.util.List;
import java.util.Map;

// TODO recipes and loot tables
// TODO crlf problems with the file copier
// TODO figure out how to block light but not sight in tinted glass
public class ConnectedGlassBlock extends TransparentBlock {
    public static final MapCodec<ConnectedGlassBlock> CODEC = ConnectedGlassBlock.createCodec(ConnectedGlassBlock::new);
    private static final String CONNECTEDGLASS = "connectedglass";
    private static final List<String> ALL_COLORS = List.of("", "white_", "light_gray_", "gray_", "black_", "brown_", "red_", "orange_", "yellow_", "lime_", "green_", "cyan_", "light_blue_", "blue_", "purple_", "magenta_", "pink_");
    private static final List<String> CLARITY = List.of("borderless_", "clear_", "scratched_", "tinted_borderless_");
    private static final Map<String, Boolean> TRANSLUCENT = Map.of("borderless_", true, "clear_", true, "scratched_", true, "tinted_borderless_", true);

    public ConnectedGlassBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    public MapCodec<ConnectedGlassBlock> getCodec() {
        return CODEC;
    }

    public static void registerAll() {
        for (String color : ALL_COLORS) {
            for (String clarity : CLARITY) {
                boolean transparent = TRANSLUCENT.getOrDefault(clarity, false);
                Settings settings = Settings.create().instrument(NoteBlockInstrument.HAT).strength(0.3F).sounds(BlockSoundGroup.GLASS).allowsSpawning(Blocks::never).solidBlock(Blocks::never).suffocates(Blocks::never).blockVision(Blocks::never);
                if (transparent) {
                    settings = settings.nonOpaque();
                }
                String glassName = color + clarity + "glass";
                Identifier glassIdentifier = Identifier.of(CONNECTEDGLASS, glassName);
                ConnectedGlassBlock glassBlock = new ConnectedGlassBlock(settings);
                Registry.register(Registries.BLOCK, glassIdentifier, glassBlock);
                BlockRenderLayerMap.INSTANCE.putBlock(glassBlock, RenderLayer.getTranslucent());
                Item glassBlockItem = new BlockItem(glassBlock, new Item.Settings());
                Registry.register(Registries.ITEM, glassIdentifier, glassBlockItem);

                String glassPaneName = color + clarity + "glass_pane";
                Identifier glassPaneIdentifier = Identifier.of(CONNECTEDGLASS, glassPaneName);
                ConnectedGlassPaneBlock glassPaneBlock = new ConnectedGlassPaneBlock(settings);
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
}
