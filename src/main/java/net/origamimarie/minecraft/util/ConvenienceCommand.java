package net.origamimarie.minecraft.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ConvenienceCommand /*implements Command<ServerCommandSource>*/ {
    private static final String COLOR = "color";
/*
    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        try {
            UnderscoreColors color = UnderscoreColors.valueOf(context.getArgument(COLOR, String.class));
            ServerPlayerEntity player = context.getSource().getPlayer();
            for (Block block : OldConnectedGlassBlock.NAME_TO_COLOR_BLOCKS.get(color)) {
                player.giveItemStack(new ItemStack(block, 1));
            }
            return 1; //positive numbers are success! Negative numbers are failure.
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }
*/
    public static void registerCommand() {
        /*CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            LiteralCommandNode<ServerCommandSource> convenienceCommandNode = CommandManager
                    .literal("foo")
                    .then(CommandManager.argument(COLOR, StringArgumentType.string())
                    .executes(new ConvenienceCommand()))
                    .build();
            dispatcher.getRoot().addChild(convenienceCommandNode);
        });*/
    }
}
