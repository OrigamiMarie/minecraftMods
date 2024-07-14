package net.origamimarie.minecraft;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;

import static net.minecraft.server.command.CommandManager.*;

public class BigBiomeReplacerCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register(literal("bigBiomeReplace")
                .then(argument("pointA", BlockPosArgumentType.blockPos())
                        .then(argument("pointB", BlockPosArgumentType.blockPos())
                                .then(argument("originalBiome", RegistryEntryArgumentType.registryEntry(commandRegistryAccess, RegistryKeys.BIOME))
                                        .then(argument("newBiome", RegistryEntryArgumentType.registryEntry(commandRegistryAccess, RegistryKeys.BIOME))
                                                .executes(BigBiomeReplacerCommand::doReplacement))))));
    }

    public static int doReplacement(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        BlockPos pointA = BlockPosArgumentType.getBlockPos(ctx, "pointA");
        String pointAString = pointA.toShortString().replace(",", "");
        BlockPos pointB = BlockPosArgumentType.getBlockPos(ctx, "pointB");
        String pointBString = pointB.toShortString().replace(",", "");

        String originalBiomeString = RegistryEntryArgumentType.getRegistryEntry(ctx, "originalBiome", RegistryKeys.BIOME).registryKey().toString().split(" ")[2].replace("]", "");
        String newBiomeString = RegistryEntryArgumentType.getRegistryEntry(ctx, "newBiome", RegistryKeys.BIOME).registryKey().toString().split(" ")[2].replace("]", "");
        List<Integer> pointACoords = Arrays.stream(pointAString.split(" ")).map(Integer::parseInt).toList();
        List<Integer> pointBCoords = Arrays.stream(pointBString.split(" ")).map(Integer::parseInt).toList();
        int[] lowCoordinate = new int[] {Math.min(pointACoords.get(0), pointBCoords.get(0)), Math.min(pointACoords.get(1), pointBCoords.get(1)), Math.min(pointACoords.get(2), pointBCoords.get(2))};
        lowCoordinate[1] = Math.max(lowCoordinate[1], -64);
        int[] highCoordinate = new int[] {Math.max(pointACoords.get(0), pointBCoords.get(0)), Math.max(pointACoords.get(1), pointBCoords.get(1)), Math.max(pointACoords.get(2), pointBCoords.get(2))};
        highCoordinate[1] = Math.min(highCoordinate[1], 316);

        String commandTemplate = "fillbiome %d %d %d %d %d %d " + newBiomeString + " replace " + originalBiomeString;
        int chunkSize = 16;
        int x = lowCoordinate[0];
        while (x < highCoordinate[0]) {
            int nextX = Math.min(nextMod(x, chunkSize), highCoordinate[0]);
            int y = lowCoordinate[1];
            while (y < highCoordinate[1]) {
                int nextY = Math.min(nextMod(y, chunkSize), highCoordinate[1]);
                int z = lowCoordinate[2];
                while (z < highCoordinate[2]) {
                    int nextZ = Math.min(nextMod(z, chunkSize), highCoordinate[2]);
                    String commandString = String.format(commandTemplate, x, y, z, nextX, nextY, nextZ);
                    ctx.getSource().getServer().getCommandManager().executeWithPrefix(ctx.getSource(),commandString);
                    z = nextZ;
                }
                y = nextY;
            }
            x = nextX;
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int nextMod(int n, int mod) {

        if (n < 0) {
            return n + 1 - (n + 1) % mod;
        } else {
            return n + 1 - (n + 1) % mod + mod;
        }
    }
}
