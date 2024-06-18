package net.origamimarie.minecraft.flowers;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.VanillaResourcePackProvider;
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
        Registry.register(Registries.BLOCK, new Identifier(ORIGAMIMARIE_MOD, flowerName), flowerBlock);
        BlockRenderLayerMap.INSTANCE.putBlock(flowerBlock, RenderLayer.getCutout());

        // Item
        Item flowerItem = new BlockItem(flowerBlock, new FabricItemSettings());
        Registry.register(Registries.ITEM, new Identifier(ORIGAMIMARIE_MOD, flowerName), flowerItem);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> content.addAfter(afterFlowerInMenu, flowerBlock));
        CompostingChanceRegistry.INSTANCE.add(flowerItem, 0.65f);

        // Flower Pot
        FlowerPotBlock pottedFlower = new FlowerPotBlock(flowerBlock, AbstractBlock.Settings.copy(Blocks.FLOWER_POT));
        Registry.register(Registries.BLOCK, new Identifier(ORIGAMIMARIE_MOD, "potted_" + flowerName), pottedFlower);
        BlockRenderLayerMap.INSTANCE.putBlock(pottedFlower, RenderLayer.getCutout());

        // Seeds -- TODO fix this so we can plant them but the item doesn't replace the flower item
        /*Item seedsItem = new AliasedBlockItem(flowerBlock, new Item.Settings());
        Registry.register(Registries.ITEM, new Identifier(ORIGAMIMARIE_MOD, flowerName + "_seeds"), seedsItem);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> content.addAfter(afterSeedsInMenu, seedsItem));
        CompostingChanceRegistry.INSTANCE.add(seedsItem, 0.3f);*/

        // TODO change that back to seedsItem when you've fixed them.
        return Triple.of(flowerBlock, flowerItem, flowerItem);
    }
}
