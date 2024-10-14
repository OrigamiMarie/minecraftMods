package net.origamimarie.minecraft;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;

import static net.origamimarie.minecraft.OrigamiMarieMod.CANDLE_PADS;

public class CandlePadBlock extends AbstractCandleBlock {

    private static final Map<Block, CandlePadBlock> CANDLES_TO_CANDLE_PADS;
    public MapCodec<CandlePadBlock> codec = createCodec(CandlePadBlock::new);
    public static final IntProperty CANDLES;
    public static final BooleanProperty LIT;
    public static final ToIntFunction<BlockState> STATE_TO_LUMINANCE;
    private static final Int2ObjectMap<List<Vec3d>> CANDLES_TO_PARTICLE_OFFSETS;
    private static final VoxelShape ONE_CANDLE_SHAPE;
    private static final VoxelShape TWO_CANDLES_SHAPE;
    private static final VoxelShape THREE_CANDLES_SHAPE;
    private static final VoxelShape FOUR_CANDLES_SHAPE;
    private final Block CANDLE;

    static {
        CANDLES = Properties.CANDLES;
        LIT = AbstractCandleBlock.LIT;
        VoxelShape padShape = Block.createCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 1.5D, 15.0D);
        ONE_CANDLE_SHAPE = VoxelShapes.union(padShape, Block.createCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 6.0D, 9.0D));
        TWO_CANDLES_SHAPE = VoxelShapes.union(padShape, Block.createCuboidShape(5.0D, 0.0D, 6.0D, 11.0D, 6.0D, 9.0D));
        THREE_CANDLES_SHAPE = VoxelShapes.union(padShape, Block.createCuboidShape(5.0D, 0.0D, 6.0D, 10.0D, 6.0D, 11.0D));
        FOUR_CANDLES_SHAPE = VoxelShapes.union(padShape, Block.createCuboidShape(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 10.0D));
        CANDLES_TO_CANDLE_PADS = Maps.newHashMap();
        STATE_TO_LUMINANCE = (state) -> (Boolean)state.get(LIT) ? 3 * state.get(CANDLES) : 0;
        // TODO looks like we will have to set these per rotation angle . . . if possible.
        CANDLES_TO_PARTICLE_OFFSETS = Util.make(() -> {
            Int2ObjectMap<List<Vec3d>> int2ObjectMap = new Int2ObjectOpenHashMap<>();
            int2ObjectMap.defaultReturnValue(ImmutableList.of());
            int2ObjectMap.put(1, ImmutableList.of(new Vec3d(0.5D, 0.5D, 0.5D)));
            int2ObjectMap.put(2, ImmutableList.of(new Vec3d(0.375D, 0.44D, 0.5D), new Vec3d(0.625D, 0.5D, 0.44D)));
            int2ObjectMap.put(3, ImmutableList.of(new Vec3d(0.5D, 0.313D, 0.625D), new Vec3d(0.375D, 0.44D, 0.5D), new Vec3d(0.56D, 0.5D, 0.44D)));
            int2ObjectMap.put(4, ImmutableList.of(new Vec3d(0.44D, 0.313D, 0.56D), new Vec3d(0.625D, 0.44D, 0.56D), new Vec3d(0.375D, 0.44D, 0.375D), new Vec3d(0.56D, 0.5D, 0.375D)));
            return Int2ObjectMaps.unmodifiable(int2ObjectMap);
        });
    }

    public CandlePadBlock(Block candle, Settings settings) {
        super(settings);
        CANDLE = candle;
        this.setDefaultState(((this.stateManager.getDefaultState()).with(CANDLES, 1)).with(LIT, false));
        CANDLES_TO_CANDLE_PADS.put(candle, this);
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (world == null || pos == null) {
                return 7455580;
            }
            return 2129968;
        }, this);
    }

    // This is just to make codec happy
    public CandlePadBlock(Settings settings) {
        super(settings);
        CANDLE = Blocks.CANDLE;
        this.setDefaultState(((this.stateManager.getDefaultState()).with(CANDLES, 1)).with(LIT, false));
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (world == null || pos == null) {
                return 7455580;
            }
            return 2129968;
        }, this);
    }

    public MapCodec<CandlePadBlock> getCodec() {
        return codec;
    }

    protected Iterable<Vec3d> getParticleOffsets(BlockState state) {
        return CANDLES_TO_PARTICLE_OFFSETS.get(state.get(CANDLES));
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(CANDLES)) {
            case 1 -> ONE_CANDLE_SHAPE;
            case 2 -> TWO_CANDLES_SHAPE;
            case 3 -> THREE_CANDLES_SHAPE;
            case 4 -> FOUR_CANDLES_SHAPE;
            default -> ONE_CANDLE_SHAPE;
        };
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        if (!context.shouldCancelInteraction() && context.getStack().getItem() == this.asItem() && state.get(CANDLES) < 4) {
            return true;
        }
        return super.canReplace(state, context);
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isEmpty()) {
            if (state.get(LIT) && player.getAbilities().allowModifyWorld) {
                extinguish(player, state, world, pos);
                return ItemActionResult.success(world.isClient);
            }
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        Item item = itemStack.getItem();
        if (itemStack.isOf(CANDLE.asItem())) {
            int oldCandleCount = state.get(CANDLES);
            state = state.cycle(CANDLES);
            // This means we actually added a candle
            if (state.get(CANDLES) > oldCandleCount) {
                world.playSound(null, pos, SoundEvents.BLOCK_LILY_PAD_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.setBlockState(pos, state);
                world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                player.incrementStat(Stats.USED.getOrCreateStat(item));
                if (!player.isCreative()) {
                    itemStack.decrement(1);
                }
                return ItemActionResult.SUCCESS;
            } else {
                return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
        } else if (itemStack.isOf(Items.FLINT_AND_STEEL)) {
            if (canBeLit(state)) {
                world.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
                world.setBlockState(pos, state.with(Properties.LIT, true), 11);
                world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                if (!player.isCreative()) {
                    itemStack.damage(1, player, LivingEntity.getSlotForHand(hand));
                }
                return ItemActionResult.SUCCESS;
            } else {
                return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
        }
        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    protected void appendProperties(Builder<Block, BlockState> builder) {
        builder.add(new Property[]{CANDLES, LIT});
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return Block.sideCoversSmallSquare(world, pos.down(), Direction.UP);
    }

    public static BlockState getCandlePadFromCandle(Block candle) {
        return (CANDLES_TO_CANDLE_PADS.get(candle)).getDefaultState();
    }

    public static boolean canBeLit(BlockState state) {
        return state.isIn(CANDLE_PADS, statex -> statex.contains(LIT) && !(Boolean)state.get(LIT));
    }
}

