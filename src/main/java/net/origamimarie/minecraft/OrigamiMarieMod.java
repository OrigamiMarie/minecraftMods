package net.origamimarie.minecraft;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.origamimarie.minecraft.azalea.ModdedAzaleaBlock;
import net.origamimarie.minecraft.biome.IceSpikeWithCrystalsFeature;
import net.origamimarie.minecraft.firtree.FirTree;
import net.origamimarie.minecraft.flowers.CustomFlowers;
import net.origamimarie.minecraft.glass.ConnectedGlassBlock;
import net.origamimarie.minecraft.rainbow_crystal.BuddingRainbowCrystalBlock;
import net.origamimarie.minecraft.rainbow_crystal.RainbowCrystalClusterBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OrigamiMarieMod implements ModInitializer {

    public static final String ORIGAMIMARIE_MOD = "origamimarie_mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(ORIGAMIMARIE_MOD);
    public static final TagKey<Block> CANDLE_PADS = TagKey.of(RegistryKeys.BLOCK, Identifier.of(ORIGAMIMARIE_MOD, "candle_pads"));
    public static final Block MANGROVE_BOOKSHELF = new Block(AbstractBlock.Settings.copy(Blocks.BOOKSHELF));

    @Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
        registerCandles();
        registerMangroveBookshelf();
        registerWrench();
        FirTree.registerFirTree();
        OrnamentBlock.registerOrnaments();
        registerAzaleas();
        CustomFlowers.registerFlowers();
        registerRainbowCrystals();
        IceSpikeWithCrystalsFeature.register();
        ConnectedGlassBlock.registerAll();
    }

    private void registerRainbowCrystals() {
        BuddingRainbowCrystalBlock.registerAll();
        RainbowCrystalClusterBlock.registerAll();
    }
    
    private void registerAzaleas() {
        ModdedAzaleaBlock.makeAndRegisterAll();
    }

    private void registerCandles() {
        Block[] candles = new Block[] {Blocks.CANDLE, Blocks.RED_CANDLE, Blocks.ORANGE_CANDLE, Blocks.YELLOW_CANDLE, Blocks.LIME_CANDLE, Blocks.GREEN_CANDLE, Blocks.LIGHT_BLUE_CANDLE, Blocks.BLUE_CANDLE, Blocks.PURPLE_CANDLE, Blocks.MAGENTA_CANDLE, Blocks.PINK_CANDLE, Blocks.WHITE_CANDLE, Blocks.LIGHT_GRAY_CANDLE, Blocks.GRAY_CANDLE, Blocks.BLACK_CANDLE, Blocks.BROWN_CANDLE, Blocks.CYAN_CANDLE};
        for (Block candleBlock : candles) {
            String candleName = getCandleNameFromCandle(candleBlock) + "_pad";
            CandlePadBlock candlePad = new CandlePadBlock(candleBlock, AbstractBlock.Settings.copy(Blocks.CANDLE).luminance(CandlePadBlock.STATE_TO_LUMINANCE));
            BlockRenderLayerMap.INSTANCE.putBlock(candlePad, RenderLayer.getCutout());
            Registry.register(Registries.BLOCK, Identifier.of(ORIGAMIMARIE_MOD, candleName), candlePad);
        }
    }

    private void registerMangroveBookshelf() {
        Registry.register(Registries.BLOCK, Identifier.of(ORIGAMIMARIE_MOD, "mangrove_bookshelf"), MANGROVE_BOOKSHELF);
        Item bookshelfItem = new BlockItem(MANGROVE_BOOKSHELF, new Item.Settings());
        Registry.register(Registries.ITEM, Identifier.of(ORIGAMIMARIE_MOD, "mangrove_bookshelf"), bookshelfItem);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> content.addAfter(Items.BOOKSHELF, bookshelfItem));
    }

    private void registerWrench() {
        Item wrenchItem = new WrenchItem(new Item.Settings());
        Registry.register(Registries.ITEM, Identifier.of(ORIGAMIMARIE_MOD, "wrench"), wrenchItem);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> content.addAfter(Items.RECOVERY_COMPASS, wrenchItem));
    }

    private String getCandleNameFromCandle(Block candle) {
        String messyName = candle.toString().replace("}", "");
        String[] tokens = messyName.split(":");
        return tokens[tokens.length-1];
    }

}
