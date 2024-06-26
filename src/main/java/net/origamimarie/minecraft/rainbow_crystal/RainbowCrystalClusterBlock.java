package net.origamimarie.minecraft.rainbow_crystal;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.origamimarie.minecraft.OrigamiMarieMod.ORIGAMIMARIE_MOD;

// Names!

public class RainbowCrystalClusterBlock extends AmethystBlock
        implements Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    protected boolean waxed;

    public MapCodec<RainbowCrystalClusterBlock> CODEC = createCodec(RainbowCrystalClusterBlock::new);

    public static final DirectionProperty FACING = Properties.FACING;
    public static Map<String, RainbowCrystalClusterBlock> RAINBOW_CRYSTAL_CLUSTER_MAP = new HashMap<>();
    public static Map<String, RainbowCrystalClusterBlock> LARGE_RAINBOW_CRYSTAL_BUD_MAP = new HashMap<>();
    public static Map<String, RainbowCrystalClusterBlock> MEDIUM_RAINBOW_CRYSTAL_BUD_MAP = new HashMap<>();
    public static Map<String, RainbowCrystalClusterBlock> SMALL_RAINBOW_CRYSTAL_BUD_MAP = new HashMap<>();
    public static Map<String, RainbowCrystalClusterBlock> WAXED_RAINBOW_CRYSTAL_CLUSTER_MAP = new HashMap<>();
    public static Map<String, RainbowCrystalClusterBlock> WAXED_LARGE_RAINBOW_CRYSTAL_BUD_MAP = new HashMap<>();
    public static Map<String, RainbowCrystalClusterBlock> WAXED_MEDIUM_RAINBOW_CRYSTAL_BUD_MAP = new HashMap<>();
    public static Map<String, RainbowCrystalClusterBlock> WAXED_SMALL_RAINBOW_CRYSTAL_BUD_MAP = new HashMap<>();

    public static Map<Block, RainbowCrystalClusterBlock> RAINBOW_CRYSTAL_GROW_MAP = new HashMap<>();
    public static Map<Block, RainbowCrystalClusterBlock> RAINBOW_CRYSTAL_WAX_ON_MAP = new HashMap<>();
    public static Map<Block, RainbowCrystalClusterBlock> RAINBOW_CRYSTAL_WAX_OFF_MAP = new HashMap<>();


    public static final List<String> COLOR_PREFIXES = List.of("magenta", "red", "orange", "yellow", "lime", "cyan", "blue", "purple");
    static {
        AbstractBlock.Settings crystalClusterSettings = AbstractBlock.Settings.create().mapColor(MapColor.MAGENTA).solid().nonOpaque().sounds(BlockSoundGroup.AMETHYST_CLUSTER).strength(0.0f, 0.0f).luminance(state -> 5).pistonBehavior(PistonBehavior.DESTROY);
        AbstractBlock.Settings largeCrystalBudSettings = AbstractBlock.Settings.create().mapColor(MapColor.MAGENTA).solid().nonOpaque().sounds(BlockSoundGroup.LARGE_AMETHYST_BUD).strength(0.0f, 0.0f).luminance(state -> 4).pistonBehavior(PistonBehavior.DESTROY);
        AbstractBlock.Settings mediumCrystalBudSettings = AbstractBlock.Settings.create().mapColor(MapColor.MAGENTA).solid().nonOpaque().sounds(BlockSoundGroup.MEDIUM_AMETHYST_BUD).strength(0.0f, 0.0f).luminance(state -> 2).pistonBehavior(PistonBehavior.DESTROY);
        AbstractBlock.Settings smallCrystalBudSettings = AbstractBlock.Settings.create().mapColor(MapColor.MAGENTA).solid().nonOpaque().sounds(BlockSoundGroup.SMALL_AMETHYST_BUD).strength(0.0f, 0.0f).luminance(state -> 1).pistonBehavior(PistonBehavior.DESTROY);
        for (String color : COLOR_PREFIXES) {
            SMALL_RAINBOW_CRYSTAL_BUD_MAP.put(color, new RainbowCrystalClusterBlock(false, 3.0f, 4.0f, smallCrystalBudSettings));
            MEDIUM_RAINBOW_CRYSTAL_BUD_MAP.put(color, new RainbowCrystalClusterBlock(false, 4.0f, 3.0f, mediumCrystalBudSettings));
            LARGE_RAINBOW_CRYSTAL_BUD_MAP.put(color, new RainbowCrystalClusterBlock(false, 5.0f, 3.0f, largeCrystalBudSettings));
            RAINBOW_CRYSTAL_CLUSTER_MAP.put(color, new RainbowCrystalClusterBlock(false, 7.0f, 3.0f, crystalClusterSettings));
            WAXED_SMALL_RAINBOW_CRYSTAL_BUD_MAP.put(color, new RainbowCrystalClusterBlock(true, 3.0f, 4.0f, smallCrystalBudSettings));
            WAXED_MEDIUM_RAINBOW_CRYSTAL_BUD_MAP.put(color, new RainbowCrystalClusterBlock(true, 4.0f, 3.0f, mediumCrystalBudSettings));
            WAXED_LARGE_RAINBOW_CRYSTAL_BUD_MAP.put(color, new RainbowCrystalClusterBlock(true, 5.0f, 3.0f, largeCrystalBudSettings));
            WAXED_RAINBOW_CRYSTAL_CLUSTER_MAP.put(color, new RainbowCrystalClusterBlock(true, 7.0f, 3.0f, crystalClusterSettings));

            RAINBOW_CRYSTAL_GROW_MAP.put(SMALL_RAINBOW_CRYSTAL_BUD_MAP.get(color), MEDIUM_RAINBOW_CRYSTAL_BUD_MAP.get(color));
            RAINBOW_CRYSTAL_GROW_MAP.put(MEDIUM_RAINBOW_CRYSTAL_BUD_MAP.get(color), LARGE_RAINBOW_CRYSTAL_BUD_MAP.get(color));
            RAINBOW_CRYSTAL_GROW_MAP.put(LARGE_RAINBOW_CRYSTAL_BUD_MAP.get(color), RAINBOW_CRYSTAL_CLUSTER_MAP.get(color));
            RAINBOW_CRYSTAL_GROW_MAP.put(WAXED_SMALL_RAINBOW_CRYSTAL_BUD_MAP.get(color), WAXED_MEDIUM_RAINBOW_CRYSTAL_BUD_MAP.get(color));
            RAINBOW_CRYSTAL_GROW_MAP.put(WAXED_MEDIUM_RAINBOW_CRYSTAL_BUD_MAP.get(color), WAXED_LARGE_RAINBOW_CRYSTAL_BUD_MAP.get(color));
            RAINBOW_CRYSTAL_GROW_MAP.put(WAXED_LARGE_RAINBOW_CRYSTAL_BUD_MAP.get(color), WAXED_RAINBOW_CRYSTAL_CLUSTER_MAP.get(color));

            RAINBOW_CRYSTAL_WAX_ON_MAP.put(SMALL_RAINBOW_CRYSTAL_BUD_MAP.get(color), WAXED_SMALL_RAINBOW_CRYSTAL_BUD_MAP.get(color));
            RAINBOW_CRYSTAL_WAX_ON_MAP.put(MEDIUM_RAINBOW_CRYSTAL_BUD_MAP.get(color), WAXED_MEDIUM_RAINBOW_CRYSTAL_BUD_MAP.get(color));
            RAINBOW_CRYSTAL_WAX_ON_MAP.put(LARGE_RAINBOW_CRYSTAL_BUD_MAP.get(color), WAXED_LARGE_RAINBOW_CRYSTAL_BUD_MAP.get(color));
            RAINBOW_CRYSTAL_WAX_ON_MAP.put(RAINBOW_CRYSTAL_CLUSTER_MAP.get(color), WAXED_RAINBOW_CRYSTAL_CLUSTER_MAP.get(color));
            RAINBOW_CRYSTAL_WAX_OFF_MAP.put(WAXED_SMALL_RAINBOW_CRYSTAL_BUD_MAP.get(color), SMALL_RAINBOW_CRYSTAL_BUD_MAP.get(color));
            RAINBOW_CRYSTAL_WAX_OFF_MAP.put(WAXED_MEDIUM_RAINBOW_CRYSTAL_BUD_MAP.get(color), MEDIUM_RAINBOW_CRYSTAL_BUD_MAP.get(color));
            RAINBOW_CRYSTAL_WAX_OFF_MAP.put(WAXED_LARGE_RAINBOW_CRYSTAL_BUD_MAP.get(color), LARGE_RAINBOW_CRYSTAL_BUD_MAP.get(color));
            RAINBOW_CRYSTAL_WAX_OFF_MAP.put(WAXED_RAINBOW_CRYSTAL_CLUSTER_MAP.get(color), RAINBOW_CRYSTAL_CLUSTER_MAP.get(color));
        }
    }
    protected VoxelShape northShape;
    protected VoxelShape southShape;
    protected VoxelShape eastShape;
    protected VoxelShape westShape;
    protected VoxelShape upShape;
    protected VoxelShape downShape;

    public RainbowCrystalClusterBlock(boolean waxed, float height, float xzOffset, AbstractBlock.Settings settings) {
        super(settings);
        this.waxed = waxed;
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false).with(FACING, Direction.UP));
        this.upShape = Block.createCuboidShape(xzOffset, 0.0, xzOffset, 16.0f - xzOffset, height, 16.0f - xzOffset);
        this.downShape = Block.createCuboidShape(xzOffset, 16.0f - height, xzOffset, 16.0f - xzOffset, 16.0, 16.0f - xzOffset);
        this.northShape = Block.createCuboidShape(xzOffset, xzOffset, 16.0f - height, 16.0f - xzOffset, 16.0f - xzOffset, 16.0);
        this.southShape = Block.createCuboidShape(xzOffset, xzOffset, 0.0, 16.0f - xzOffset, 16.0f - xzOffset, height);
        this.eastShape = Block.createCuboidShape(0.0, xzOffset, xzOffset, height, 16.0f - xzOffset, 16.0f - xzOffset);
        this.westShape = Block.createCuboidShape(16.0f - height, xzOffset, xzOffset, 16.0, 16.0f - xzOffset, 16.0f - xzOffset);
    }

    public static void registerAll() {
        for (String color : COLOR_PREFIXES) {
            registerCluster(RAINBOW_CRYSTAL_CLUSTER_MAP.get(color), color + "_rainbow_crystal_cluster");
            registerCluster(LARGE_RAINBOW_CRYSTAL_BUD_MAP.get(color), color + "_large_rainbow_crystal_bud");
            registerCluster(MEDIUM_RAINBOW_CRYSTAL_BUD_MAP.get(color), color + "_medium_rainbow_crystal_bud");
            registerCluster(SMALL_RAINBOW_CRYSTAL_BUD_MAP.get(color), color + "_small_rainbow_crystal_bud");

            registerCluster(WAXED_RAINBOW_CRYSTAL_CLUSTER_MAP.get(color), color + "_waxed_rainbow_crystal_cluster");
            registerCluster(WAXED_LARGE_RAINBOW_CRYSTAL_BUD_MAP.get(color), color + "_waxed_large_rainbow_crystal_bud");
            registerCluster(WAXED_MEDIUM_RAINBOW_CRYSTAL_BUD_MAP.get(color), color + "_waxed_medium_rainbow_crystal_bud");
            registerCluster(WAXED_SMALL_RAINBOW_CRYSTAL_BUD_MAP.get(color), color + "_waxed_small_rainbow_crystal_bud");
        }
    }

    private static void registerCluster(Block block, String id) {
        Registry.register(Registries.BLOCK, new Identifier(ORIGAMIMARIE_MOD, id), block);
        BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout());
        Item item = new BlockItem(block, new FabricItemSettings());
        Registry.register(Registries.ITEM, new Identifier(ORIGAMIMARIE_MOD, id), item);
    }

    // This is just to make codec happy, might never get called
    public RainbowCrystalClusterBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    public MapCodec<RainbowCrystalClusterBlock> getCodec() {
        return CODEC;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getStackInHand(hand);
        Item item = itemStack.getItem();
        if (itemStack.isOf(Items.HONEYCOMB) && RAINBOW_CRYSTAL_WAX_ON_MAP.containsKey(this)) {
            if (!player.isCreative()) {
                itemStack.decrement(1);
            }
            world.setBlockState(pos, RAINBOW_CRYSTAL_WAX_ON_MAP.get(this).getDefaultState().with(RainbowCrystalClusterBlock.FACING, state.get(RainbowCrystalClusterBlock.FACING)).with(RainbowCrystalClusterBlock.WATERLOGGED, state.getFluidState().getFluid() == Fluids.WATER));
            world.playSound(null, pos, SoundEvents.ITEM_HONEYCOMB_WAX_ON, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            player.incrementStat(Stats.USED.getOrCreateStat(Items.HONEYCOMB));
            return ActionResult.SUCCESS;
        } else if (item instanceof AxeItem && RAINBOW_CRYSTAL_WAX_OFF_MAP.containsKey(this)) {
            if (!player.isCreative()) {
                itemStack.damage(1, player, (p) -> {
                    p.sendToolBreakStatus(hand);
                });
            }
            world.playSound(null, pos, SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.setBlockState(pos, RAINBOW_CRYSTAL_WAX_OFF_MAP.get(this).getDefaultState().with(RainbowCrystalClusterBlock.FACING, state.get(RainbowCrystalClusterBlock.FACING)).with(RainbowCrystalClusterBlock.WATERLOGGED, state.getFluidState().getFluid() == Fluids.WATER));
            world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction direction = state.get(FACING);
        switch (direction) {
            case NORTH -> {
                return this.northShape;
            }
            case SOUTH -> {
                return this.southShape;
            }
            case EAST -> {
                return this.eastShape;
            }
            case WEST -> {
                return this.westShape;
            }
            case DOWN -> {
                return this.downShape;
            }
        }
        return this.upShape;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos blockPos = pos.offset(direction.getOpposite());
        return world.getBlockState(blockPos).isSideSolidFullSquare(world, blockPos, direction);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if (direction == state.get(FACING).getOpposite() && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World worldAccess = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        return (this.getDefaultState().with(WATERLOGGED, worldAccess.getFluidState(blockPos).getFluid() == Fluids.WATER)).with(FACING, ctx.getSide());
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED)) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING);
    }
}
