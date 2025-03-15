package net.origamimarie.minecraft.util;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.origamimarie.minecraft.OrigamiMarieMod;

import java.util.function.Function;

import static net.minecraft.util.Identifier.isPathValid;

// These are all cobbled together from random bits of decompiled Minecraft code.
// I tried using examples from the Fabric wiki, and none of them worked quite the way I needed, or quite right.
public class RegistrationMethods {

    public static <T extends Block> T registerBlock(String id, Function<Settings, T> blockFactory, Settings settings, boolean registerAnItem) {
        T block = registerBlock(blockKeyOf(id), blockFactory, settings);
        if (registerAnItem) {
            registerItem(block);
        }
        return block;
    }

    private static RegistryKey<Block> blockKeyOf(String id) {
        return RegistryKey.of(RegistryKeys.BLOCK, ofOrigamiMarieMod(id));
    }

    private static RegistryKey<Item> itemKeyOf(String id) {
        return RegistryKey.of(RegistryKeys.ITEM, ofOrigamiMarieMod(id));
    }

    public static <T extends Block> T registerBlock(RegistryKey<Block> key, Function<AbstractBlock.Settings, T> factory, AbstractBlock.Settings settings) {
        T block = factory.apply(settings.registryKey(key));
        return Registry.register(Registries.BLOCK, key, block);
    }

    public static Identifier ofOrigamiMarieMod(String path) {
        return Identifier.of(OrigamiMarieMod.ORIGAMIMARIE_MOD, validatePath(OrigamiMarieMod.ORIGAMIMARIE_MOD, path));
    }

    private static String validatePath(String namespace, String path) {
        if (!isPathValid(path)) {
            throw new InvalidIdentifierException("Non [a-z0-9/._-] character in path of location: " + namespace + ":" + path);
        } else {
            return path;
        }
    }

    public static Item registerItem(Block block) {
        return Items.register(block, BlockItem::new);
    }

    public static Item registerItem(String id, Function<Item.Settings, Item> factory) {
        return Items.register(itemKeyOf(id), factory, new Item.Settings());
    }
}
