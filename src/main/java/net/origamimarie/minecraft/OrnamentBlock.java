package net.origamimarie.minecraft;

import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.item.ItemGroups;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.origamimarie.minecraft.util.RegistrationMethods.registerBlock;

public class OrnamentBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(4.0, 4.0, 4.0, 12.0, 12.0, 12.0);

    public static final List<OrnamentBlock> ORNAMENT_BLOCKS = new ArrayList<>(16);

    public OrnamentBlock(Settings settings, DyeColor color) {
        super(settings.breakInstantly().nonOpaque().luminance((state) -> 10).mapColor(color));
    }

    public static void registerOrnaments() {
        List<DyeColor> dyeColorsInOrder = List.of(DyeColor.WHITE, DyeColor.LIGHT_GRAY, DyeColor.GRAY, DyeColor.BLACK, DyeColor.BROWN, DyeColor.RED, DyeColor.ORANGE, DyeColor.YELLOW, DyeColor.LIME, DyeColor.GREEN, DyeColor.CYAN, DyeColor.LIGHT_BLUE, DyeColor.BLUE, DyeColor.PURPLE, DyeColor.MAGENTA, DyeColor.PINK);
        for (DyeColor color : dyeColorsInOrder) {
            String ornamentPathName = color.name().toLowerCase(Locale.ROOT) + "_ornament";
            OrnamentBlock ornamentBlock = registerBlock(ornamentPathName, s -> new OrnamentBlock(s, color), Settings.copy(Blocks.GLASS), true);
            ORNAMENT_BLOCKS.add(ornamentBlock);
            BlockRenderLayerMap.putBlock(ornamentBlock, BlockRenderLayer.CUTOUT);
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.COLORED_BLOCKS).register(content -> content.add(ornamentBlock));
        }
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
}
