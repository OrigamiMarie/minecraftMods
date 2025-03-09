package net.origamimarie.minecraft.util;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.DyeColor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum UnderscoreColors {
    PLAIN_("", DyeColor.WHITE),
    WHITE_("white_", DyeColor.WHITE),
    LIGHT_GRAY_("light_gray_", DyeColor.LIGHT_GRAY),
    GRAY_("gray_", DyeColor.GRAY),
    BLACK_("black_", DyeColor.BLACK),
    BROWN_("brown_", DyeColor.BROWN),
    RED_("red_", DyeColor.RED),
    ORANGE_("orange_", DyeColor.ORANGE),
    YELLOW_("yellow_", DyeColor.YELLOW),
    LIME_("lime_", DyeColor.LIME),
    GREEN_("green_", DyeColor.GREEN),
    CYAN_("cyan_", DyeColor.CYAN),
    LIGHT_BLUE_("light_blue_", DyeColor.LIGHT_BLUE),
    BLUE_("blue_", DyeColor.BLUE),
    PURPLE_("purple_", DyeColor.PURPLE),
    MAGENTA_("magenta_", DyeColor.MAGENTA),
    PINK_("pink_", DyeColor.PINK);
    public static final List<UnderscoreColors> ALL_UNDERSCORE_COLORS = List.of(PLAIN_, WHITE_, LIGHT_GRAY_, GRAY_, BLACK_, BROWN_, RED_, ORANGE_, YELLOW_, LIME_, GREEN_, CYAN_, LIGHT_BLUE_, BLUE_, PURPLE_, MAGENTA_, PINK_);
    public static final List<UnderscoreColors> RAINBOW_EIGHT = List.of(MAGENTA_, RED_, ORANGE_, YELLOW_, LIME_, CYAN_, BLUE_, PURPLE_);
    public static final List<UnderscoreColors> RAINBOW_PSEUDO_RANDOM = List.of(RED_, LIME_, YELLOW_, BLUE_, PURPLE_, BLUE_, ORANGE_, LIME_, MAGENTA_, CYAN_, ORANGE_, MAGENTA_, ORANGE_, RED_, CYAN_, PURPLE_, LIME_, CYAN_, BLUE_, PURPLE_, BLUE_, RED_, YELLOW_, CYAN_, YELLOW_, MAGENTA_, ORANGE_, YELLOW_, RED_, LIME_, MAGENTA_, PURPLE_);
    public static final Map<UnderscoreColors, String> PRETTY_NAMES = new HashMap<>();

    static {
        PRETTY_NAMES.putAll(Map.of(PLAIN_, "", WHITE_, "White ", LIGHT_GRAY_, "Light Gray ", GRAY_, "Gray ", BLACK_, "Black ", BROWN_, "Brown ", RED_, "Red ", ORANGE_, "Orange ", YELLOW_, "Yellow "));
        PRETTY_NAMES.putAll(Map.of(LIME_, "Lime ", GREEN_, "Green ", CYAN_, "Cyan ", LIGHT_BLUE_, "Light Blue ", BLUE_, "Blue ", PURPLE_, "Purple ", MAGENTA_, "Magenta ", PINK_, "Pink "));
    }

    public final String color;
    public final DyeColor dyeColor;

    UnderscoreColors(String color, DyeColor dyeColor) {
        this.color = color;
        this.dyeColor = dyeColor;
    }

    @Override
    public String toString() {
        return color;
    }


    public static AbstractBlock.Settings copySettingsAndAddMapColor(AbstractBlock.Settings settings, DyeColor color) {
        return AbstractBlock.Settings.copy(new SillyBlock(settings)).mapColor(color);
    }


    private static class SillyBlock extends AbstractBlock {
        public SillyBlock(Settings settings) {
            super(settings);
        }

        @Override
        protected MapCodec<? extends Block> getCodec() {
            return null;
        }

        @Override
        public Item asItem() {
            return null;
        }

        @Override
        protected Block asBlock() {
            return null;
        }
    }
}
