package net.origamimarie.minecraft.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic4CommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.item.Item;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.origamimarie.minecraft.OrigamiMarieMod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CatenaryCommands {
    private static final Dynamic4CommandExceptionType CURVE_DOES_NOT_COMPLETE_EXCEPTION = new Dynamic4CommandExceptionType((nose, tail, belly, type) -> Text.stringifiedTranslatable("commands.cat-draw.curve-too-long", nose, tail, belly, type));
    private static final Dynamic4CommandExceptionType BELLY_BETWEEN_NOSE_AND_TAIL_EXCEPTION = new Dynamic4CommandExceptionType((nose, tail, belly, type) -> Text.stringifiedTranslatable("commands.cat-draw.midway-belly", nose, tail, belly, type));

    private static final double MAGIC_Y_OFFSET = -2.352409615243247;
    private static final double MAGIC_Y_MULTIPLIER = 1.0/1.352409615;
    private static final double MAGIC_X_OFFSET = -1.5;
    private static final double MAGIC_X_MULTIPLIER = 3.0;

    private static BlockPos NOSE_POSITION = null;
    private static BlockPos TAIL_POSITION = null;
    private static Integer BELLY_HEIGHT = null;

    public static void registerCommands() {
        ArgumentTypeRegistry.registerArgumentType(
                Identifier.of(OrigamiMarieMod.ORIGAMIMARIE_MOD, CatenaryPointConnectArgumentType.POINT_CONNECT_TYPE),
                CatenaryPointConnectArgumentType.class, ConstantArgumentSerializer.of(CatenaryPointConnectArgumentType::catenaryPointConnect));

        registerCommand("cat-nose", CatenaryNoseCommand.NOSE, new CatenaryNoseCommand(), BlockPosArgumentType.blockPos());
        registerCommand("cat-tail", CatenaryTailCommand.TAIL, new CatenaryTailCommand(), BlockPosArgumentType.blockPos());
        registerCommand("cat-belly", CatenaryBellyCommand.BELLY, new CatenaryBellyCommand(), IntegerArgumentType.integer(-64, 320));

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            LiteralCommandNode<ServerCommandSource> commandNode = CommandManager
                    .literal("cat-draw")
                    .then((CommandManager.argument(CatenaryPointConnectArgumentType.POINT_CONNECT_TYPE, CatenaryPointConnectArgumentType.catenaryPointConnect()).executes(commandContext -> CatenaryDrawCommand.execute(commandContext, Collections.singleton((commandContext.getSource()).getPlayerOrThrow()), CatenaryPointConnectArgumentType.getCatenaryPointConnect(commandContext, CatenaryPointConnectArgumentType.POINT_CONNECT_TYPE)))).then(CommandManager.argument("target", EntityArgumentType.players()).executes(commandContext -> CatenaryDrawCommand.execute(commandContext, EntityArgumentType.getPlayers(commandContext, "target"), CatenaryPointConnectArgumentType.getCatenaryPointConnect(commandContext, CatenaryPointConnectArgumentType.POINT_CONNECT_TYPE)))))
                    .build();
            dispatcher.getRoot().addChild(commandNode);
        });
    }

    private static <T> void registerCommand(String commandName, String argumentName,
                                            Command<ServerCommandSource> command, ArgumentType<T> argumentType) {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            LiteralCommandNode<ServerCommandSource> commandNode = CommandManager
                    .literal(commandName)
                    .then(CommandManager.argument(argumentName, argumentType)
                            .executes(command))
                    .build();
            dispatcher.getRoot().addChild(commandNode);
        });
    }

    public static class CatenaryNoseCommand implements Command<ServerCommandSource> {
        public static final String NOSE = "nose";
        @Override
        public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            NOSE_POSITION = BlockPosArgumentType.getLoadedBlockPos(context, NOSE);
            return 0;
        }
    }

    public static class CatenaryTailCommand implements Command<ServerCommandSource> {
        public static final String TAIL = "tail";
        @Override
        public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            TAIL_POSITION = BlockPosArgumentType.getLoadedBlockPos(context, TAIL);
            return 0;
        }
    }

    public static class CatenaryBellyCommand implements Command<ServerCommandSource> {
        public static final String BELLY = "belly";
        @Override
        public int run(CommandContext<ServerCommandSource> context) {
            BELLY_HEIGHT = IntegerArgumentType.getInteger(context, BELLY);
            return 0;
        }
    }

    public static class CatenaryDrawCommand {
        public static int execute(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> targets, CatenaryPointConnectArgumentType.CatenaryPointConnect catenaryPointConnect) throws CommandSyntaxException {
            if (NOSE_POSITION != null && TAIL_POSITION != null && BELLY_HEIGHT != null) {
                BlockState curveBlockType = Blocks.DIRT.getDefaultState();
                if (targets.size() == 1) {
                    // Almost certainly the player, who might be holding a block right now.
                    ServerPlayerEntity player = targets.iterator().next();
                    Item heldItem = player.getMainHandStack().getItem();
                    Block heldBlock = Block.getBlockFromItem(heldItem);
                    if (heldBlock != null) {
                        curveBlockType = heldBlock.getDefaultState();
                    }
                }
                World world = context.getSource().getWorld();
                List<BlockPos> catenaryCoords = makeCatenaryCoords(NOSE_POSITION, TAIL_POSITION, BELLY_HEIGHT, catenaryPointConnect);
                for (BlockPos catenaryCoord : catenaryCoords) {
                    placeBlockIfAir(world, curveBlockType, catenaryCoord);
                }
            }
            return 0;
        }
    }

    private static void placeBlockIfAir(World world, BlockState blockState, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() == Blocks.AIR) {
            world.setBlockState(pos, blockState);
        }
    }

    private static List<BlockPos> makeCatenaryCoords(BlockPos nose, BlockPos tail, int bellyY, CatenaryPointConnectArgumentType.CatenaryPointConnect catenaryPointConnect) throws CommandSyntaxException {
        List<BlockPos> catenaryCoords = new ArrayList<>();
        BlockPos start = nose;
        BlockPos end = tail;
        // See if we have a lopsided curve, and figure out where the virtual end
        // (where it would end if they were at the same y) is.
        if (end.getY() != start.getY()) {
            // Everything is simpler if we always know that the start is the point that is further from the belly.
            if (Math.abs(end.getY() - bellyY) > Math.abs(start.getY() - bellyY)) {
                start = tail;
                end = nose;
            }
            boolean bellyBelowBoth = end.getY() > bellyY && start.getY() > bellyY;
            boolean bellyAboveBoth = end.getY() < bellyY && start.getY() < bellyY;
            if (!bellyBelowBoth && !bellyAboveBoth) {
                // This means the belly is situated between the nose and the tail, and that does not make a catenary.
                throw BELLY_BETWEEN_NOSE_AND_TAIL_EXCEPTION.create(nose, tail, bellyY, catenaryPointConnect);
            }
            end = findVirtualEnd(start, end, bellyY, catenaryPointConnect);
        }
        int startX = start.getX();
        int startY = start.getY();
        int startZ = start.getZ();
        int height = start.getY() - bellyY;
        int xLength = end.getX() - start.getX();
        int zLength = end.getZ() - start.getZ();
        double horizontalLength = Math.sqrt(xLength * xLength + zLength * zLength);
        int stepCount = (int)Math.round(4 * Math.max(Math.abs(horizontalLength), 2 * Math.abs(height)));
        double coshStepSize = 1.0 / stepCount;
        for (int i = 0; i <= stepCount; i++) {
            double a = i * coshStepSize;
            double y = calculateY(a);
            catenaryCoords.add(new BlockPos(startX + (int)Math.round(a * xLength), startY + (int)Math.round(y * height), startZ + (int)Math.round(a * zLength)));
        }
        return catenaryCoords;
    }

    // One end is lower than the other, and that is a somewhat tricky thing to calculate for.
    // So we're stretching out the curve until the y height at the specified (x,z) is right.
    // Then that stretch level tells us where there virtual end (where the other end is at the same height) would be.
    private static BlockPos findVirtualEnd(BlockPos start, BlockPos end, int bellyY, CatenaryPointConnectArgumentType.CatenaryPointConnect catenaryPointConnect) throws CommandSyntaxException {
        int targetY = end.getY();
        int startX = start.getX();
        int startY = start.getY();
        int startZ = start.getZ();
        double xLength = end.getX() - start.getX();
        double zLength = end.getZ() - start.getZ();
        double horizontalLength = Math.sqrt(xLength * xLength + zLength * zLength);
        // We'll try increments every ~1/4 block, so that we don't overshoot by much.
        // This isn't exactly a step count, but more like a horizontal distance divisor.
        int stepCount = (int)Math.round(horizontalLength * 4);
        double horizontalIncrement = horizontalLength / stepCount;
        // The completion condition (getting to / past the target Y)
        // approaches from different directions depending on relative belly height.
        boolean highBelly = end.getY() < bellyY;
        int height = start.getY() - bellyY;
        double virtualHorizontalLength = horizontalLength;
        double y = start.getY() + height * calculateY(Math.abs(horizontalLength / virtualHorizontalLength));

        int i;
        double maxI = Math.abs(20 * stepCount);
        for (i = 0; i < maxI && (highBelly ? y < targetY : y > targetY); i++) {
            virtualHorizontalLength += horizontalIncrement;
            y = startY + height * calculateY(Math.abs(horizontalLength / virtualHorizontalLength));
        }
        if (catenaryPointConnect == CatenaryPointConnectArgumentType.CatenaryPointConnect.DIRECT) {
            for (i = 0; i < maxI && (highBelly ? y > targetY : y < targetY); i++) {
                virtualHorizontalLength += horizontalIncrement;
                y = startY + height * calculateY(Math.abs(horizontalLength / virtualHorizontalLength));
            }
        }
        if (i >= maxI) {
            throw CURVE_DOES_NOT_COMPLETE_EXCEPTION.create(NOSE_POSITION.toString(), TAIL_POSITION.toString(), BELLY_HEIGHT.toString(), catenaryPointConnect.toString());
        }
        double lengthMultiplier = virtualHorizontalLength / horizontalLength;
        return new BlockPos((int)Math.round(startX + xLength * lengthMultiplier),
                startY,
                (int)Math.round(startZ + zLength * lengthMultiplier));
    }

    private static double calculateY(double horizontalFractionOfCatenary) {
        return (Math.cosh(horizontalFractionOfCatenary * MAGIC_X_MULTIPLIER + MAGIC_X_OFFSET) + MAGIC_Y_OFFSET) * MAGIC_Y_MULTIPLIER;
    }

    public static void main(String[] args) {
        double magicYOffset = -2.352409615243247;
        double magicYMultiplier = 1.0/1.352409615;
        double magicXOffset = -1.5;
        double magicXMultiplier = 3.0;

        for (int i = 0; i <= 100; i++) {
            double x = i * 0.01;
            double y = (Math.cosh(x * magicXMultiplier + magicXOffset) + magicYOffset) * magicYMultiplier;
            System.out.println(x + "\t" + y);
        }
    }
}
