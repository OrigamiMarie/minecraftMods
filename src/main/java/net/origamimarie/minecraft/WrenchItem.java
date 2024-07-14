package net.origamimarie.minecraft;

import net.minecraft.block.AmethystClusterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.block.enums.StairShape;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static net.minecraft.block.StairsBlock.*;
import static net.origamimarie.minecraft.rainbow_crystal.RainbowCrystalClusterBlock.FACING;

public class WrenchItem extends Item {
    private static final List<SlabType> SLAB_TYPES = List.of(SlabType.TOP, SlabType.BOTTOM, SlabType.TOP);
    private static final List<Direction> HORIZONTAL_DIRECTIONS = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH);
    private static final List<Pair<Direction, BlockHalf>> STAIR_BLOCK_STATES = List.of(
            Pair.of(Direction.NORTH, BlockHalf.TOP),
            Pair.of(Direction.NORTH, BlockHalf.BOTTOM),
            Pair.of(Direction.EAST, BlockHalf.TOP),
            Pair.of(Direction.EAST, BlockHalf.BOTTOM),
            Pair.of(Direction.SOUTH, BlockHalf.TOP),
            Pair.of(Direction.SOUTH, BlockHalf.BOTTOM),
            Pair.of(Direction.WEST, BlockHalf.TOP),
            Pair.of(Direction.WEST, BlockHalf.BOTTOM),
            Pair.of(Direction.NORTH, BlockHalf.TOP)
    );

    public WrenchItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        boolean modified = true;

        if (block instanceof PillarBlock) {
            blockState = blockState.cycle(PillarBlock.AXIS);
        } else if (block instanceof HorizontalFacingBlock) {
            blockState = blockState.with(HorizontalFacingBlock.FACING, getNext(blockState.get(HorizontalFacingBlock.FACING), HORIZONTAL_DIRECTIONS));
        } else if (block instanceof FacingBlock) {
            blockState = blockState.cycle(FacingBlock.FACING);
        } else if (block instanceof DoorBlock) {
            blockState = blockState.cycle(DoorBlock.FACING);
        } else if (block instanceof AmethystClusterBlock) {
            blockState = blockState.cycle(AmethystClusterBlock.FACING);
        } else if (block instanceof HopperBlock) {
            blockState = blockState.cycle(HopperBlock.FACING);
        } else if (block instanceof SlabBlock) {
            SlabType currentSlabType = blockState.get(SlabBlock.TYPE);
            if (currentSlabType != SlabType.DOUBLE) {
                blockState = blockState.with(SlabBlock.TYPE, getNext(currentSlabType, SLAB_TYPES));
            }
        } else if (block instanceof StairsBlock) {
            Pair<Direction, BlockHalf> newPair = getNext(Pair.of(blockState.get(StairsBlock.FACING), blockState.get(StairsBlock.HALF)), STAIR_BLOCK_STATES);
            blockState = blockState.with(StairsBlock.FACING, newPair.getLeft()).with(StairsBlock.HALF, newPair.getRight());
            blockState = blockState.with(SHAPE, getStairShape(blockState, world, blockPos));
        } else {
            modified = false;
        }
        if (modified) {
            world.setBlockState(blockPos, blockState);
            world.updateNeighbors(blockPos.add(0, -1, 0), block);
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.PASS;
        }
    }

    private <T> T getNext(T item, List<T> list) {
        return list.get(list.indexOf(item) + 1);
    }

    private static StairShape getStairShape(BlockState state, BlockView world, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockState blockState = world.getBlockState(pos.offset(direction));
        if (isStairs(blockState) && state.get(HALF) == blockState.get(HALF)) {
            Direction direction2 = blockState.get(FACING);
            if (direction2.getAxis() != state.get(FACING).getAxis() && isDifferentOrientation(state, world, pos, direction2.getOpposite())) {
                if (direction2 == direction.rotateYCounterclockwise()) {
                    return StairShape.OUTER_LEFT;
                }

                return StairShape.OUTER_RIGHT;
            }
        }

        BlockState blockState2 = world.getBlockState(pos.offset(direction.getOpposite()));
        if (isStairs(blockState2) && state.get(HALF) == blockState2.get(HALF)) {
            Direction direction3 = blockState2.get(FACING);
            if (direction3.getAxis() != state.get(FACING).getAxis() && isDifferentOrientation(state, world, pos, direction3)) {
                if (direction3 == direction.rotateYCounterclockwise()) {
                    return StairShape.INNER_LEFT;
                }

                return StairShape.INNER_RIGHT;
            }
        }

        return StairShape.STRAIGHT;
    }


    private static boolean isDifferentOrientation(BlockState state, BlockView world, BlockPos pos, Direction dir) {
        BlockState blockState = world.getBlockState(pos.offset(dir));
        return !isStairs(blockState) || blockState.get(FACING) != state.get(FACING) || blockState.get(HALF) != state.get(HALF);
    }
}
