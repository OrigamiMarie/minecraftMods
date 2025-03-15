package net.origamimarie.minecraft;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.origamimarie.minecraft.azalea.ModdedAzaleaBlock;
import net.origamimarie.minecraft.biome.IceSpikeWithCrystalsFeature;
import net.origamimarie.minecraft.firtree.FirTree;
import net.origamimarie.minecraft.flowers.CustomFlowers;
import net.origamimarie.minecraft.hud.CoordinatesHUD;
import net.origamimarie.minecraft.rainbow_crystal.BuddingRainbowCrystalBlock;
import net.origamimarie.minecraft.rainbow_crystal.RainbowCrystalClusterBlock;
import net.origamimarie.minecraft.util.ConvenienceCommand;
import net.origamimarie.minecraft.util.RegistrationMethods;
import net.origamimarie.minecraft.util.UnderscoreColors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.origamimarie.minecraft.util.RegistrationMethods.registerBlock;

public class OrigamiMarieMod implements ModInitializer {

    public static final String ORIGAMIMARIE_MOD = "origamimarie_mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(ORIGAMIMARIE_MOD);
    public static final TagKey<Block> CANDLE_PADS = TagKey.of(RegistryKeys.BLOCK, Identifier.of(ORIGAMIMARIE_MOD, "candle_pads"));
    private static final String MANGROVE_BOOKSHELF = "mangrove_bookshelf";
    public static final Block MANGROVE_BOOKSHELF_BLOCK = registerBlock(MANGROVE_BOOKSHELF, Block::new, Settings.copy(Blocks.BOOKSHELF), true);

    @Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
        registerCandles();
        registerMangroveBookshelf();
        registerWrench();
        registerIceSlabs();
        FirTree.registerFirTree();
        OrnamentBlock.registerOrnaments();
        registerAzaleas();
        CustomFlowers.registerFlowers();
        registerRainbowCrystals();
        IceSpikeWithCrystalsFeature.register();
        registerPastelConcrete();
        ConvenienceCommand.registerCommand();
        CoordinatesHUD.register();
    }

    private void registerPastelConcrete() {
        List<DyeColor> colors = new ArrayList<>(Arrays.asList(DyeColor.values()));
        Collections.reverse(colors);
        List<UnderscoreColors> underscoreColors = new ArrayList<>(Arrays.asList(UnderscoreColors.values()));
        underscoreColors.remove(UnderscoreColors.PLAIN_);
        Collections.reverse(underscoreColors);
        // We're stepping through the colors in reverse so that our additions to the Item Groups (the block menu)
        // can be simply added after the last vanilla concrete / concrete powder, and they'll spool out from that point.
        for (UnderscoreColors color : underscoreColors) {
            String concreteName = "pastel_" + color + "concrete";
            String concretePowderName = concreteName + "_powder";

            Settings concreteSettings = Settings.create().mapColor(color.dyeColor).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.8F);
            Block concreteBlock = registerBlock(concreteName, Block::new, concreteSettings, true);
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.COLORED_BLOCKS).register(content -> content.addAfter(Items.PINK_CONCRETE, concreteBlock));

            Settings powderSettings = Settings.create().mapColor(color.dyeColor).instrument(NoteBlockInstrument.SNARE).strength(0.5F).sounds(BlockSoundGroup.SAND);
            Block powderBlock = registerBlock(concretePowderName, s -> new ConcretePowderBlock(concreteBlock, s), powderSettings, true);
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.COLORED_BLOCKS).register(content -> content.addAfter(Items.PINK_CONCRETE_POWDER, powderBlock));
        }
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
            CandlePadBlock candlePad = registerBlock(candleName, s -> new CandlePadBlock(candleBlock, s), Settings.copy(candleBlock).luminance(CandlePadBlock.STATE_TO_LUMINANCE), false);
            BlockRenderLayerMap.INSTANCE.putBlock(candlePad, RenderLayer.getCutout());
        }
    }

    private void registerMangroveBookshelf() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> content.addAfter(Items.BOOKSHELF, MANGROVE_BOOKSHELF_BLOCK));
    }

    private void registerWrench() {
        Item wrenchItem = RegistrationMethods.registerItem("wrench", WrenchItem::new);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> content.addAfter(Items.RECOVERY_COMPASS, wrenchItem));
    }

    private void registerIceSlabs() {
        registerBlock("packed_ice_slab", SlabBlock::new, Settings.copy(Blocks.PACKED_ICE), true);
        registerBlock("blue_ice_slab", SlabBlock::new, Settings.copy(Blocks.PACKED_ICE), true);
    }

    private String getCandleNameFromCandle(Block candle) {
        String messyName = candle.toString().replace("}", "");
        String[] tokens = messyName.split(":");
        return tokens[tokens.length-1];
    }

}
