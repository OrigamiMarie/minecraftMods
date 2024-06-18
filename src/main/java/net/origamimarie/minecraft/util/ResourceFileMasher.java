package net.origamimarie.minecraft.util;

import net.minecraft.util.DyeColor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ResourceFileMasher {

    public static void main(String[] args) throws IOException {
        mashFiles();;
    }

    private static void mashFiles() throws IOException {
        File originalFile = new File("C:\\Users\\origa\\java\\minecraftMods\\src\\main\\resources\\data\\origamimarie_mod\\loot_tables\\blocks\\blue_ornament.json");
        String toReplace = "blue";
        File directory = originalFile.getParentFile().getAbsoluteFile();
        String filename = originalFile.getName();
        List<String> colorNamesSansCurrent = getColors(toReplace);
        String originalFileContents = FileUtils.readFileToString(originalFile, StandardCharsets.UTF_8);
        for (String color : colorNamesSansCurrent) {
            File fileCopy = new File(directory, filename.replace(toReplace, color));
            FileUtils.write(fileCopy, originalFileContents.replace(toReplace, color), StandardCharsets.UTF_8);
        }
    }

    private static void printDyeColors(boolean initialCapital) {
        if (initialCapital) {
            System.out.println(getColors("").stream().map(ResourceFileMasher::capitalizeWords).collect(Collectors.joining("\n")));
        } else {
            System.out.println(String.join("\n", getColors("")));
        }
    }

    private static String capitalizeWords(String color) {
        color = color.toUpperCase().charAt(0) + color.substring(1);
        if (color.contains("_")) {
            String[] words = color.split("_");
            color = words[0] + " " + words[1].toUpperCase().charAt(0) + words[1].substring(1);
        }
        return color;
    }

    private static List<String> getColors(String colorToRemove) {
        return Arrays.stream(DyeColor.values()).map(color -> color.name().toLowerCase(Locale.ROOT)).filter(color -> !color.equals(colorToRemove)).sorted().collect(Collectors.toList());
    }
}
