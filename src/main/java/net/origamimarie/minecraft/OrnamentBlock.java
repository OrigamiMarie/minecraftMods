package net.origamimarie.minecraft;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.origamimarie.minecraft.OrigamiMarieMod.ORIGAMIMARIE_MOD;

public class OrnamentBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(4.0, 4.0, 4.0, 12.0, 12.0, 12.0);

    public static final List<OrnamentBlock> ORNAMENT_BLOCKS = new ArrayList<>(16);

    public OrnamentBlock() {
        super(AbstractBlock.Settings.copy(Blocks.GLASS).breakInstantly().nonOpaque().luminance((state) -> 10));
    }

    public static void registerOrnaments() {
        List<DyeColor> dyeColorsInOrder = List.of(DyeColor.WHITE, DyeColor.LIGHT_GRAY, DyeColor.GRAY, DyeColor.BLACK, DyeColor.BROWN, DyeColor.RED, DyeColor.ORANGE, DyeColor.YELLOW, DyeColor.LIME, DyeColor.GREEN, DyeColor.CYAN, DyeColor.LIGHT_BLUE, DyeColor.BLUE, DyeColor.PURPLE, DyeColor.MAGENTA, DyeColor.PINK);
        for (DyeColor color : dyeColorsInOrder) {
            OrnamentBlock ornamentBlock = new OrnamentBlock();
            ORNAMENT_BLOCKS.add(ornamentBlock);
            String ornamentPathName = color.name().toLowerCase(Locale.ROOT) + "_ornament";
            Registry.register(Registries.BLOCK, Identifier.of(ORIGAMIMARIE_MOD, ornamentPathName), ornamentBlock);
            Item item = new BlockItem(ornamentBlock, new Item.Settings());
            Registry.register(Registries.ITEM, Identifier.of(ORIGAMIMARIE_MOD, ornamentPathName), item);
            BlockRenderLayerMap.INSTANCE.putBlock(ornamentBlock, RenderLayer.getCutout());
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.COLORED_BLOCKS).register(content -> content.add(item));
        }
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
}
