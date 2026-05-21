package net.origamimarie.minecraft.flowers;

import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.BlockSoundGroup;

import static net.origamimarie.minecraft.util.RegistrationMethods.registerBlock;

public class CustomFlowers {

    public static void registerFlowers() {
        makeFlowerBlockItemSeed("fireweed", Blocks.PINK_PETALS, StatusEffects.FIRE_RESISTANCE, 4, MapColor.DARK_GREEN);
    }

    private static void makeFlowerBlockItemSeed(String flowerName, Block afterFlowerInMenu, RegistryEntry<StatusEffect> effect,
                                                int effectLength, MapColor mapColor) {
        // Block
        Block flowerBlock = registerBlock(flowerName, s -> new FlowerBlock(effect, effectLength, s),
                Settings.create().mapColor(mapColor).noCollision().breakInstantly()
                        .sounds(BlockSoundGroup.GRASS).offset(AbstractBlock.OffsetType.XZ)
                        .pistonBehavior(PistonBehavior.DESTROY),
                true);
        BlockRenderLayerMap.putBlock(flowerBlock, BlockRenderLayer.CUTOUT);

        // Item
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> content.addAfter(afterFlowerInMenu, flowerBlock));
        CompostingChanceRegistry.INSTANCE.add(flowerBlock, 0.65f);

        // Flower Pot
        FlowerPotBlock pottedFlower = registerBlock("potted_" + flowerName, s -> new FlowerPotBlock(flowerBlock, s), Settings.copy(Blocks.FLOWER_POT), false);
        BlockRenderLayerMap.putBlock(pottedFlower, BlockRenderLayer.CUTOUT);
    }
}
