package net.origamimarie.minecraft.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CatenaryPointConnectArgumentType implements ArgumentType<CatenaryPointConnectArgumentType.CatenaryPointConnect> {
    public static final String POINT_CONNECT_TYPE = "point_connect_type";

    private static final Collection<String> EXAMPLES;
    private static final CatenaryPointConnect[] VALUES;

    public CatenaryPointConnectArgumentType() {
    }

    public CatenaryPointConnect parse(StringReader stringReader) {
        String string = stringReader.readUnquotedString();
        return CatenaryPointConnect.byId(string, CatenaryPointConnect.DEFAULT);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return context.getSource() instanceof CommandSource ? CommandSource.suggestMatching(Arrays.stream(VALUES).map(CatenaryPointConnect::getId), builder) : Suggestions.empty();
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static CatenaryPointConnectArgumentType catenaryPointConnect() {
        return new CatenaryPointConnectArgumentType();
    }

    public static CatenaryPointConnect getCatenaryPointConnect(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, CatenaryPointConnect.class);
    }

    static {
        EXAMPLES = Stream.of(CatenaryPointConnect.DIRECT, CatenaryPointConnect.BOUNCE).map(CatenaryPointConnect::getId).collect(Collectors.toList());
        VALUES = CatenaryPointConnect.values();
    }


    public enum CatenaryPointConnect implements StringIdentifiable {
        DIRECT("direct"),
        BOUNCE("bounce");

        public static final CatenaryPointConnect DEFAULT = BOUNCE;
        public static final StringIdentifiable.EnumCodec<CatenaryPointConnect> CODEC = StringIdentifiable.createCodec(CatenaryPointConnect::values);

        private final String id;

        CatenaryPointConnect(final String id) {
            this.id = id;
        }

        public String getId() {
            return this.id;
        }

        @Nullable
        @Contract("_,!null->!null;_,null->_")
        public static CatenaryPointConnectArgumentType.CatenaryPointConnect byId(String id, @Nullable CatenaryPointConnectArgumentType.CatenaryPointConnect fallback) {
            CatenaryPointConnect catenaryPointConnect = CODEC.byId(id);
            return catenaryPointConnect != null ? catenaryPointConnect : fallback;
        }

        public String asString() {
            return this.id;
        }
    }
}
