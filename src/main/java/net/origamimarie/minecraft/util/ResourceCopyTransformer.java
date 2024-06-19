package net.origamimarie.minecraft.util;

import com.google.common.base.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;

/*
 * This reads a file from the resources directory,
 * and multiplexes it out to many other files based on requested string replacements.
 */
public class ResourceCopyTransformer {

    private static final String RESOURCES_DIR = "C:\\Users\\origa\\java\\minecraftMods\\src\\main\\resources\\";
    private static final List<String> RAINBOW_CRYSTAL_COLORS = List.of("magenta", "red", "orange", "yellow", "lime", "cyan", "blue", "purple");
    private static final List<String> RAINBOW_CRYSTAL_TYPES = List.of("rainbow_crystal_cluster", "large_rainbow_crystal_bud", "medium_rainbow_crystal_bud", "small_rainbow_crystal_bud");
    private static final List<String> ALL_MINECRAFT_COLORS = List.of("pink", "magenta", "red", "orange", "yellow", "lime", "green", "cyan", "light_blue", "blue", "purple", "white", "light_gray", "gray", "black", "brown");
    private static final List<String> ALL_MINECRAFT_COLORS_WITH_EMPTY = new ArrayList<>();

    static {
        for (String color : ALL_MINECRAFT_COLORS) {
            ALL_MINECRAFT_COLORS_WITH_EMPTY.add("_" + color);
        }
        ALL_MINECRAFT_COLORS_WITH_EMPTY.add("");
    }

    private static final List<TransformParameters> transforms = List.of(
            new TransformParameters("assets\\origamimarie_mod\\blockstates\\templates\\color_crystal_type.json",
                    "assets\\origamimarie_mod\\blockstates\\",
                    Map.of("color", RAINBOW_CRYSTAL_COLORS,
                            "crystal_type", RAINBOW_CRYSTAL_TYPES)),
            new TransformParameters("assets\\origamimarie_mod\\models\\block\\rainbow_crystal\\templates\\color_crystal_type.json",
                    "assets\\origamimarie_mod\\models\\block\\rainbow_crystal\\",
                    Map.of("color", RAINBOW_CRYSTAL_COLORS,
                            "crystal_type", RAINBOW_CRYSTAL_TYPES)),
            new TransformParameters("assets\\origamimarie_mod\\models\\item\\templates\\color_crystal_type.json",
                    "assets\\origamimarie_mod\\models\\item\\",
                    Map.of("color", RAINBOW_CRYSTAL_COLORS,
                            "crystal_type", RAINBOW_CRYSTAL_TYPES)),
            new TransformParameters("data\\origamimarie_mod\\loot_tables\\blocks\\templates\\color_crystal_type.json",
                    "data\\origamimarie_mod\\loot_tables\\blocks\\",
                    Map.of("color", RAINBOW_CRYSTAL_COLORS,
                            "crystal_type", RAINBOW_CRYSTAL_TYPES)),

            new TransformParameters("assets\\origamimarie_mod\\textures\\block\\rainbow_crystal\\templates\\magenta_crystal_type.png.mcmeta",
                    "assets\\origamimarie_mod\\textures\\block\\rainbow_crystal\\",
                    Map.of("crystal_type", RAINBOW_CRYSTAL_TYPES)),
            new TransformParameters("assets\\origamimarie_mod\\textures\\block\\rainbow_crystal\\templates\\red_crystal_type.png.mcmeta",
                    "assets\\origamimarie_mod\\textures\\block\\rainbow_crystal\\",
                    Map.of("crystal_type", RAINBOW_CRYSTAL_TYPES)),
            new TransformParameters("assets\\origamimarie_mod\\textures\\block\\rainbow_crystal\\templates\\orange_crystal_type.png.mcmeta",
                    "assets\\origamimarie_mod\\textures\\block\\rainbow_crystal\\",
                    Map.of("crystal_type", RAINBOW_CRYSTAL_TYPES)),
            new TransformParameters("assets\\origamimarie_mod\\textures\\block\\rainbow_crystal\\templates\\yellow_crystal_type.png.mcmeta",
                    "assets\\origamimarie_mod\\textures\\block\\rainbow_crystal\\",
                    Map.of("crystal_type", RAINBOW_CRYSTAL_TYPES)),
            new TransformParameters("assets\\origamimarie_mod\\textures\\block\\rainbow_crystal\\templates\\lime_crystal_type.png.mcmeta",
                    "assets\\origamimarie_mod\\textures\\block\\rainbow_crystal\\",
                    Map.of("crystal_type", RAINBOW_CRYSTAL_TYPES)),
            new TransformParameters("assets\\origamimarie_mod\\textures\\block\\rainbow_crystal\\templates\\cyan_crystal_type.png.mcmeta",
                    "assets\\origamimarie_mod\\textures\\block\\rainbow_crystal\\",
                    Map.of("crystal_type", RAINBOW_CRYSTAL_TYPES)),
            new TransformParameters("assets\\origamimarie_mod\\textures\\block\\rainbow_crystal\\templates\\blue_crystal_type.png.mcmeta",
                    "assets\\origamimarie_mod\\textures\\block\\rainbow_crystal\\",
                    Map.of("crystal_type", RAINBOW_CRYSTAL_TYPES)),
            new TransformParameters("assets\\origamimarie_mod\\textures\\block\\rainbow_crystal\\templates\\purple_crystal_type.png.mcmeta",
                    "assets\\origamimarie_mod\\textures\\block\\rainbow_crystal\\",
                    Map.of("crystal_type", RAINBOW_CRYSTAL_TYPES)),

            new TransformParameters("assets\\connectedglass\\blockstates\\templates\\type_glass_color.json",
                    "assets\\connectedglass\\blockstates\\",
                    Map.of("type", List.of("borderless", "clear", "scratched", "tinted_borderless"),
                            "_color", ALL_MINECRAFT_COLORS_WITH_EMPTY)),
            new TransformParameters("assets\\connectedglass\\blockstates\\templates\\type_glass_color_pane.json",
                    "assets\\connectedglass\\blockstates\\",
                    Map.of("type", List.of("borderless", "clear", "scratched", "tinted_borderless"),
                            "_color", ALL_MINECRAFT_COLORS_WITH_EMPTY))
    );

    public static void main(String[] args) throws IOException {
        doTransforms();
    }

    private static void doTransforms() throws IOException {
        for (TransformParameters transform : transforms) {
            String originalFilename = StringUtils.substringAfterLast(transform.sourceFile, "\\");
            File originalFile = new File(RESOURCES_DIR + transform.sourceFile);
            String originalFileContents = FileUtils.readFileToString(originalFile, Charsets.UTF_8);
            List<Set<Pair<String, String>>> replacementSets = transform.calculateAllStringReplacementSets();
            File destinationDir = transform.calculateFullDestinationDir();

            for (Set<Pair<String, String>> replacementSet : replacementSets) {
                String newFileName = performAllReplacements(originalFilename, replacementSet);
                String newFileContents =performAllReplacements(originalFileContents, replacementSet);
                File newFile = new File(destinationDir, newFileName);
                FileUtils.write(newFile, newFileContents, Charsets.UTF_8);
            }
        }
    }

    private static void prettyNamesPrinter() {
        for (String color : RAINBOW_CRYSTAL_COLORS) {
            for (String type : RAINBOW_CRYSTAL_TYPES) {
                String blockName = color + "_" + type;
//                String prettyBlockName = Arrays.stream(blockName.split("_")).map(s -> s.substring(0, 1).toUpperCase() + s.substring(1)).collect(Collectors.joining(" "));
//                String output = ("  'block.origamimarie_mod." + blockName + "': '" + prettyBlockName + "'").replace("'", "\"");
                String output = " origamimarie_mod:" + blockName;
                System.out.print(output);
            }
        }

    }

    private static String performAllReplacements(String s, Set<Pair<String, String>> replacements) {
        for (Pair<String, String> replacement : replacements) {
            s = s.replaceAll(replacement.getLeft(), replacement.getRight());
        }
        return s;
    }

    private static class TransformParameters {
        public final String sourceFile;
        public final String destinationDir;
        public final Map<String, List<String>> replacements;

        public TransformParameters(String sourceFile, String destinationDir, Map<String, List<String>> replacements) {
            this.sourceFile = sourceFile;
            this.destinationDir = destinationDir;
            this.replacements = replacements;
        }

        public List<Set<Pair<String, String>>> calculateAllStringReplacementSets() {
            List<Set<Pair<String, String>>> allReplacementSets = new ArrayList<>();
            allReplacementSets.add(new HashSet<>());
            List<Set<Pair<String, String>>> replacementSetsTemp = new ArrayList<>();

            for (String originalString : replacements.keySet()) {
                replacementSetsTemp.clear();
                replacementSetsTemp.addAll(allReplacementSets);
                allReplacementSets.clear();
                List<String> replacementStrings = replacements.get(originalString);
                for (String replacementString : replacementStrings) {
                    Pair<String, String> originalAndReplacement = Pair.of(originalString, replacementString);
                    for (Set<Pair<String, String>> setOfReplacements : replacementSetsTemp) {
                        Set<Pair<String, String>> setCopy = new HashSet<>(setOfReplacements);
                        setCopy.add(originalAndReplacement);
                        allReplacementSets.add(setCopy);
                    }
                }
            }
            return allReplacementSets;
        }

        public File calculateFullDestinationDir() {
            return new File(RESOURCES_DIR, destinationDir);
        }
    }
}
