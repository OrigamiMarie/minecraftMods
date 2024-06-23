package net.origamimarie.minecraft.util;

import com.google.common.base.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/*
 * This reads a file from the resources directory,
 * and multiplexes it out to many other files based on requested string replacements.
 */
public class ResourceCopyTransformer {

    private static final Charset UTF_8 = Charsets.UTF_8;
    private static final String RESOURCES_DIR = "C:/Users/origa/java/minecraftMods/src/main/resources/";
    private static final List<String> RAINBOW_CRYSTAL_COLORS = List.of("magenta", "red", "orange", "yellow", "lime", "cyan", "blue", "purple");
    private static final List<String> RAINBOW_CRYSTAL_TYPES = List.of("rainbow_crystal_cluster", "large_rainbow_crystal_bud", "medium_rainbow_crystal_bud", "small_rainbow_crystal_bud");

    private static List<String> transformFiles;

    // I know, a static try/catch is probably not great form.
    // But this is just a file generator, it's not part of the minecraft mod runtime.
    static {
        try {
            transformFiles = FileUtils.readLines(new File(RESOURCES_DIR, "copierControllerFile"), UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        doTransformsFromFiles();
    }

    private static void doTransformsFromFiles() throws IOException {
        List<TransformParameters> transforms = new ArrayList<>();
        for (String transformFile : transformFiles) {
            transforms.addAll(TransformParameters.readFromFile(new File(RESOURCES_DIR, transformFile)));
        }
        doTransforms(transforms);
    }

    private static void doTransforms(List<TransformParameters> transforms) throws IOException {
        for (TransformParameters transform : transforms) {
            String originalFilename = transform.sourceFile.getName();
            String originalFileContents = FileUtils.readFileToString(transform.sourceFile, UTF_8);
            List<Set<Pair<String, String>>> replacementSets = transform.calculateAllStringReplacementSets();

            for (Set<Pair<String, String>> replacementSet : replacementSets) {
                String newFileName = performAllReplacements(originalFilename, replacementSet);
                String newFileContents =performAllReplacements(originalFileContents, replacementSet);
                File newFile = new File(transform.destinationDir, newFileName);
                FileUtils.write(newFile, newFileContents, UTF_8);
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


    public record TransformParameters(File sourceFile, File destinationDir,
                                      Map<String, List<String>> replacements) {
            public static final String COMMENT = "#";
            public static final String DOUBLE_DOT = "..";
            public static final String LINE_SEPARATOR = "\r\n";
            public static final String COMMA_SEPARATOR = ", ";

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

            public static List<TransformParameters> readFromFile(File file) throws IOException {
                List<TransformParameters> result = new ArrayList<>();
                String fileContents = FileUtils.readFileToString(file, UTF_8);
                String[] lines = fileContents.split(LINE_SEPARATOR);
                File parentFile = file.getParentFile();
                for (int i = 0; i < lines.length; i++) {
                    // Skip over the comment lines and empty lines
                    String currentLine = lines[i].strip();
                    if (currentLine.length() == 0 || currentLine.startsWith(COMMENT)) {
                        continue;
                    }
                    // Now we have a transform definition
                    String[] sourceAndDestFiles = currentLine.split(", ");
                    File sourceFile = mashFileParts(new File(parentFile, sourceAndDestFiles[0]));
                    File destinationDir = mashFileParts(new File(parentFile, sourceAndDestFiles[1]));
                    Map<String, List<String>> replacements = new HashMap<>();
                    i++;
                    while (i < lines.length && lines[i].strip().length() > 0) {
                        currentLine = lines[i];
                        i++;
                        if (currentLine.strip().startsWith(COMMENT)) {
                            continue;
                        }
                        List<String> replementTokens = new ArrayList<>(Arrays.asList(currentLine.split(COMMA_SEPARATOR)));
                        String key = replementTokens.remove(0);
                        replacements.put(key, replementTokens);
                    }
                    result.add(new TransformParameters(sourceFile, destinationDir, replacements));
                }
                return result;
            }

            private static File mashFileParts(File file) {
                String fullFile = file.getAbsolutePath();
                while (fullFile.contains(DOUBLE_DOT)) {
                    int location = fullFile.indexOf(DOUBLE_DOT);
                    int startOfFileAbove = StringUtils.lastIndexOf(fullFile, "\\", location - 2);
                    fullFile = fullFile.substring(0, startOfFileAbove) + fullFile.substring(location + 2);
                }
                return new File(fullFile);
            }
        }
}
