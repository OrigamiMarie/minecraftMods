package net.origamimarie.minecraft.flowers;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Triple;

import static net.origamimarie.minecraft.OrigamiMarieMod.ORIGAMIMARIE_MOD;

public class CustomFlowers {

    public static Triple<Block, Item, Item> FIREWEED_BLOCK_ITEM_SEED;

    public static void registerFlowers() {
        FIREWEED_BLOCK_ITEM_SEED = makeFlowerBlockItemSeed("fireweed", Items.PINK_PETALS, Items.PUMPKIN_SEEDS);
    }

    private static Triple<Block, Item, Item> makeFlowerBlockItemSeed(String flowerName, Item afterFlowerInMenu, Item afterSeedsInMenu) {
        // Block
        Block flowerBlock = new FlowerBlock(StatusEffects.FIRE_RESISTANCE, 4, AbstractBlock.Settings.create().mapColor(MapColor.DARK_GREEN).noCollision().breakInstantly().sounds(BlockSoundGroup.GRASS).offset(AbstractBlock.OffsetType.XZ).pistonBehavior(PistonBehavior.DESTROY));
        Registry.register(Registries.BLOCK, Identifier.of(ORIGAMIMARIE_MOD, flowerName), flowerBlock);
        BlockRenderLayerMap.INSTANCE.putBlock(flowerBlock, RenderLayer.getCutout());

        // Item
        Item flowerItem = new BlockItem(flowerBlock, new Item.Settings());
        Registry.register(Registries.ITEM, Identifier.of(ORIGAMIMARIE_MOD, flowerName), flowerItem);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> content.addAfter(afterFlowerInMenu, flowerBlock));
        CompostingChanceRegistry.INSTANCE.add(flowerItem, 0.65f);

        // Flower Pot
        FlowerPotBlock pottedFlower = new FlowerPotBlock(flowerBlock, AbstractBlock.Settings.copy(Blocks.FLOWER_POT));
        Registry.register(Registries.BLOCK, Identifier.of(ORIGAMIMARIE_MOD, "potted_" + flowerName), pottedFlower);
        BlockRenderLayerMap.INSTANCE.putBlock(pottedFlower, RenderLayer.getCutout());
        return Triple.of(flowerBlock, flowerItem, flowerItem);
    }
}
