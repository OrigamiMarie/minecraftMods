package net.origamimarie.minecraft.rainbow_crystal;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AmethystBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.origamimarie.minecraft.util.UnderscoreColors;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.origamimarie.minecraft.OrigamiMarieMod.ORIGAMIMARIE_MOD;

public class RainbowCrystalClusterBlock extends AmethystBlock
        implements Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    protected boolean waxed;

    public MapCodec<RainbowCrystalClusterBlock> CODEC = createCodec(RainbowCrystalClusterBlock::new);

    public static final String WAXED = "waxed_";
    public static final String UNWAXED = "";
    public static final String BRIGHT = "";
    public static final String DIM = "dim_";
    public static final String SMALL = "small_rainbow_crystal_bud";
    public static final String MEDIUM = "medium_rainbow_crystal_bud";
    public static final String LARGE = "large_rainbow_crystal_bud";
    public static final String CLUSTER = "rainbow_crystal_cluster";
    public static final DirectionProperty FACING = Properties.FACING;

    // This map is needed for starting crystal growth from bud blocks
    public static final Map<UnderscoreColors, RainbowCrystalClusterBlock> SMALL_RAINBOW_CRYSTAL_BUD_MAP = new HashMap<>();
    public static final Map<UnderscoreColors, RainbowCrystalClusterBlock> DIM_SMALL_RAINBOW_CRYSTAL_BUD_MAP = new HashMap<>();
    private static final Map<UnderscoreColors, Map<String, Map<String, Map<String, RainbowCrystalClusterBlock>>>> ALL_CRYSTALS_MAP = new HashMap<>();

    public static final Map<Block, RainbowCrystalClusterBlock> RAINBOW_CRYSTAL_GROW_MAP = new HashMap<>();
    private static final Map<Block, RainbowCrystalClusterBlock> RAINBOW_CRYSTAL_WAX_ON_MAP = new HashMap<>();
    private static final Map<Block, RainbowCrystalClusterBlock> RAINBOW_CRYSTAL_WAX_OFF_MAP = new HashMap<>();

    static {
        List<String> coatings = List.of(UNWAXED, WAXED);
        List<String> brightnesses = List.of(BRIGHT, DIM);
        List<String> sizes = List.of(SMALL, MEDIUM, LARGE, CLUSTER);

        Map<String, Float> sizeHeight = Map.of(SMALL, 3.0f, MEDIUM, 4.0f, LARGE, 5.0f, CLUSTER, 7.0f);
        Map<String, Float> sizeXzOffset = Map.of(SMALL, 4.0f, MEDIUM, 3.0f, LARGE, 3.0f, CLUSTER, 3.0f);
        Map<String, AbstractBlock.Settings> brightSizeSettings = Map.of(
                SMALL, AbstractBlock.Settings.create().solid().nonOpaque().sounds(BlockSoundGroup.SMALL_AMETHYST_BUD).strength(0.0f, 0.0f).luminance(state -> 1).pistonBehavior(PistonBehavior.DESTROY),
                MEDIUM, AbstractBlock.Settings.create().solid().nonOpaque().sounds(BlockSoundGroup.MEDIUM_AMETHYST_BUD).strength(0.0f, 0.0f).luminance(state -> 2).pistonBehavior(PistonBehavior.DESTROY),
                LARGE, AbstractBlock.Settings.create().solid().nonOpaque().sounds(BlockSoundGroup.LARGE_AMETHYST_BUD).strength(0.0f, 0.0f).luminance(state -> 4).pistonBehavior(PistonBehavior.DESTROY),
                CLUSTER, AbstractBlock.Settings.create().solid().nonOpaque().sounds(BlockSoundGroup.AMETHYST_CLUSTER).strength(0.0f, 0.0f).luminance(state -> 5).pistonBehavior(PistonBehavior.DESTROY)
        );
        Map<String, AbstractBlock.Settings> dimSizeSettings = Map.of(
                SMALL, AbstractBlock.Settings.create().solid().nonOpaque().sounds(BlockSoundGroup.SMALL_AMETHYST_BUD).strength(0.0f, 0.0f).pistonBehavior(PistonBehavior.DESTROY),
                MEDIUM, AbstractBlock.Settings.create().solid().nonOpaque().sounds(BlockSoundGroup.MEDIUM_AMETHYST_BUD).strength(0.0f, 0.0f).pistonBehavior(PistonBehavior.DESTROY),
                LARGE, AbstractBlock.Settings.create().solid().nonOpaque().sounds(BlockSoundGroup.LARGE_AMETHYST_BUD).strength(0.0f, 0.0f).pistonBehavior(PistonBehavior.DESTROY),
                CLUSTER, AbstractBlock.Settings.create().solid().nonOpaque().sounds(BlockSoundGroup.AMETHYST_CLUSTER).strength(0.0f, 0.0f).pistonBehavior(PistonBehavior.DESTROY)
        );
        Map<String, Map<String, AbstractBlock.Settings>> brightnessSizeSettings = Map.of(BRIGHT, brightSizeSettings, DIM, dimSizeSettings);
        for (UnderscoreColors color : UnderscoreColors.RAINBOW_EIGHT) {
            for (String coating : coatings) {
                for (String brightness : brightnesses) {
                    for (String size : sizes) {
                        Settings colorSettings = UnderscoreColors.copySettingsAndAddMapColor(brightnessSizeSettings.get(brightness).get(size), color.dyeColor);
                        RainbowCrystalClusterBlock clusterBlock = new RainbowCrystalClusterBlock(coating.equals(WAXED), sizeHeight.get(size), sizeXzOffset.get(size), colorSettings);
                        addToAllCrystals(color, coating, brightness, size, clusterBlock);
                    }
                }
            }
        }
        for (UnderscoreColors color : UnderscoreColors.RAINBOW_EIGHT) {
            SMALL_RAINBOW_CRYSTAL_BUD_MAP.put(color, getCrystal(color, UNWAXED, BRIGHT, SMALL));
            DIM_SMALL_RAINBOW_CRYSTAL_BUD_MAP.put(color, getCrystal(color, UNWAXED, DIM, SMALL));

            for (String brightness : brightnesses) {
                for (String size : sizes) {
                    RAINBOW_CRYSTAL_WAX_ON_MAP.put(getCrystal(color, UNWAXED, brightness, size), getCrystal(color, WAXED, brightness, size));
                    RAINBOW_CRYSTAL_WAX_OFF_MAP.put(getCrystal(color, WAXED, brightness, size), getCrystal(color, UNWAXED, brightness, size));
                }

                for (String coating : coatings) {
                    RAINBOW_CRYSTAL_GROW_MAP.put(getCrystal(color, coating, brightness, SMALL), getCrystal(color, coating, brightness, MEDIUM));
                    RAINBOW_CRYSTAL_GROW_MAP.put(getCrystal(color, coating, brightness, MEDIUM), getCrystal(color, coating, brightness, LARGE));
                    RAINBOW_CRYSTAL_GROW_MAP.put(getCrystal(color, coating, brightness, LARGE), getCrystal(color, coating, brightness, CLUSTER));
                }
            }
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
        for (UnderscoreColors color : ALL_CRYSTALS_MAP.keySet()) {
            Map<String, Map<String, Map<String, RainbowCrystalClusterBlock>>> singleColorMap = ALL_CRYSTALS_MAP.get(color);
            for (String coating : singleColorMap.keySet()) {
                Map<String, Map<String, RainbowCrystalClusterBlock>> singleCoatingMap = singleColorMap.get(coating);
                for (String brightness : singleCoatingMap.keySet()) {
                    Map<String, RainbowCrystalClusterBlock> singleBrightnessMap = singleCoatingMap.get(brightness);
                    for (String size : singleBrightnessMap.keySet()) {
                        registerCluster(singleBrightnessMap.get(size), color + coating + brightness + size);
                    }
                }
            }
        }
    }

    private static void addToAllCrystals(UnderscoreColors color, String coating, String brightness, String size, RainbowCrystalClusterBlock block) {
        ALL_CRYSTALS_MAP.computeIfAbsent(color, k -> new HashMap<>()).computeIfAbsent(coating, k -> new HashMap<>()).computeIfAbsent(brightness, k -> new HashMap<>()).put(size, block);
    }

    public static RainbowCrystalClusterBlock getCrystal(UnderscoreColors color, String coating, String brightness, String size) {
        return ALL_CRYSTALS_MAP.get(color).get(coating).get(brightness).get(size);
    }

    private static void registerCluster(Block block, String id) {
        Registry.register(Registries.BLOCK, Identifier.of(ORIGAMIMARIE_MOD, id), block);
        BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout());
        Item item = new BlockItem(block, new Item.Settings());
        Registry.register(Registries.ITEM, Identifier.of(ORIGAMIMARIE_MOD, id), item);
    }

    // This is just to make codec happy, might never get called
    public RainbowCrystalClusterBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    public MapCodec<RainbowCrystalClusterBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
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
            return ItemActionResult.SUCCESS;
        } else if (item instanceof AxeItem && RAINBOW_CRYSTAL_WAX_OFF_MAP.containsKey(this)) {
            world.playSound(null, pos, SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.setBlockState(pos, RAINBOW_CRYSTAL_WAX_OFF_MAP.get(this).getDefaultState().with(RainbowCrystalClusterBlock.FACING, state.get(RainbowCrystalClusterBlock.FACING)).with(RainbowCrystalClusterBlock.WATERLOGGED, state.getFluidState().getFluid() == Fluids.WATER));
            world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            if (!player.isCreative()) {
                itemStack.damage(1, player, LivingEntity.getSlotForHand(hand));
            }
            return ItemActionResult.SUCCESS;
        }
        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
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
