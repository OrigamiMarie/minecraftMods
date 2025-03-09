package net.origamimarie.minecraft.util;

public class ConvenienceCommand /*implements Command<ServerCommandSource>*/ {
    private static final String COLOR = "color";
/*
    @Override
    public int run(CommandContext<ServerCommandSource> context) {
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
