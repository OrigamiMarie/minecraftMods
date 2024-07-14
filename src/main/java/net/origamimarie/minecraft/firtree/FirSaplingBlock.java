package net.origamimarie.minecraft.firtree;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class FirSaplingBlock extends PlantBlock {
    private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);;
    public MapCodec<FirSaplingBlock> codec = createCodec(FirSaplingBlock::new);

    protected int candleCount;

    public FirSaplingBlock() {
        this(0);
    }

    public FirSaplingBlock(int lightLevel) {
        super(AbstractBlock.Settings.copy(Blocks.SPRUCE_SAPLING).luminance((state) -> lightLevel));
        candleCount = 0;
    }

    // Makes codec happy
    public FirSaplingBlock(Settings settings) {
        this(0);
    }

    public MapCodec<FirSaplingBlock> getCodec() {
        return codec;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(Items.BONE_MEAL)) {
            boolean madeTree = FirTree.makeTree(world, pos, candleCount);
            if (madeTree) {
                if (!player.isCreative()) {
                    itemStack.decrement(1);
                }
                return ActionResult.SUCCESS;
            } else {
                return ActionResult.PASS;
            }
        }
        return ActionResult.PASS;
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getLightLevel(pos.up()) >= 9 && random.nextInt(7) == 0) {
            FirTree.makeTree(world, pos, candleCount);
        }
    }


    public static class FirOneCandleSaplingBlock extends FirSaplingBlock {

        public FirOneCandleSaplingBlock() {
            super(3);
            candleCount = 1;
        }
    }


    public static class FirTwoCandleSaplingBlock extends FirSaplingBlock {

        public FirTwoCandleSaplingBlock() {
            super(6);
            candleCount = 2;
        }
    }


    public static class FirThreeCandleSaplingBlock extends FirSaplingBlock {

        public FirThreeCandleSaplingBlock() {
            super(9);
            candleCount = 3;
        }
    }


    public static class FirFourCandleSaplingBlock extends FirSaplingBlock {

        public FirFourCandleSaplingBlock() {
            super(12);
            candleCount = 4;
        }
    }
}
