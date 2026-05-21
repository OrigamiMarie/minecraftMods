package net.origamimarie.minecraft.util;

import com.google.common.base.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * This utility automates the creation of all the similar asset & data files required for block variants.
 * 1. A controller file is read, which points to each copier file.
 * 2. Each copier file contains one or more sections that describe how to multiplex a file.
 * 3. Each section has these "; " lines:
 *   a. original filename, and destination directory
 *   b. one or more lines with an initial key to replace, and one or more values to use to replace it.
 *
 * All modifications described on each line, are multiplexed against all other lines.  So if your lines look like:
 *   animal; cat; dog; elephant
 *   color; brown; white
 *   age; 9; 12
 * this would result in 12 files, ranging from cat/brown/9 to cat/white/12 to elephant/white/12.
 *
 * Strings are replaced both in the file text and in the filename.
 *
 * Variants with no string in a slot can be made, with syntax like this:
 *   thing; alpha; beta
 *   property_; ; shiny_
 * which would result in variants like "alpha", "beta", "shiny_alpha", and "shiny_beta"
 *
 * If you want to make correlated changes that aren't multiplexed, use pipes like this:
 *   color|phase; red|10; orange|20
 *   shape; square; circle
 * which would result in variants like "red square 10", "red circle 10", "orange square 20", and "orange circle 20"
 */
public class ResourceCopyTransformer {

    private static final Charset UTF_8 = Charsets.UTF_8;
    private static final String PNG_EXTENSION = ".png";
    private static final String PNG_COLOR_MAP = "pngColorMap-";
    public static final String COMMENT = "#";
    public static final String DOUBLE_DOT = "..";
    public static final String LINE_SEPARATOR = "\r\n";
    public static final String FIELD_SEPARATOR = "; ";
    public static final String DISPOSABLE_CHARACTER = "`";
    public static final String CORRELATED_FIELD_SEPARATOR = "|";
    public static final String ESCAPED_CORRELATED_FIELD_SEPARATOR = "\\|";
    public static final String COLOR_SEPARATOR = "-";
    public static final String COLOR_CHANNEL_SEPARATOR = ",";
    private static final String RESOURCES_DIR = "C:/Users/origa/IdeaProjects/minecraftMods/src/main/resources/";
    private static final File COPIER_CONTROLLER_FILE = new File(RESOURCES_DIR, "copierControllerFile");

    public static void main(String[] args) throws IOException {
        readControllerFileAndPerformCopierTransforms();
    }

    private static void readControllerFileAndPerformCopierTransforms() throws IOException {
        List<String> transformFiles = FileUtils.readLines(COPIER_CONTROLLER_FILE, UTF_8);
        List<TransformParameters> transforms = new ArrayList<>();
        for (String transformFile : transformFiles) {
            transforms.addAll(TransformParameters.readFromFile(new File(RESOURCES_DIR, transformFile)));
        }
        doTransforms(transforms);
    }

    private static void doTransforms(List<TransformParameters> transforms) throws IOException {
        for (TransformParameters transform : transforms) {
            List<Set<Pair<String, String>>> replacementSets = transform.calculateAllStringReplacementSets();
            boolean fileIsPng = transform.sourceFile.toString().endsWith(PNG_EXTENSION);
            String originalFileContents = null;
            BufferedImage originalImage = null;
            if (fileIsPng) {
                originalImage = ImageIO.read(transform.sourceFile);
            } else {
                originalFileContents = FileUtils.readFileToString(transform.sourceFile, UTF_8);
            }

            for (Set<Pair<String, String>> replacementSet : replacementSets) {
                String newFileName = performAllReplacements(transform.sourceFileNameAndSubDirs, replacementSet);
                String newDestinationDirName = performAllReplacements(transform.destinationSubDirPattern, replacementSet);
                File newFile = new File(mashSingleFile(new File(transform.destinationParentDir, newDestinationDirName)), newFileName);
                if (fileIsPng) {
                    BufferedImage newImage = performAllColorReplacements(originalImage, replacementSet);
                    ImageIO.write(newImage, "png", newFile);
                } else {
                    String newFileContents = performAllReplacements(originalFileContents, replacementSet);
                    FileUtils.write(newFile, newFileContents, UTF_8);
                }
            }
        }
    }
    
    private static BufferedImage performAllColorReplacements(BufferedImage originalImage, Set<Pair<String, String>> replacementSet) {
        int[][] originalAlpha = TransformParameters.getImageAlpha(originalImage);
        BufferedImage newPng  = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());
        int[][] newAlpha = TransformParameters.getImageAlpha(newPng);
        Map<Color, Color> colorReplacements = new HashMap<>();
        for (Pair<String, String> replacementPair : replacementSet) {
            if (replacementPair.getLeft().startsWith(PNG_COLOR_MAP)) {
                String[] colorKeys = replacementPair.getLeft().replace(PNG_COLOR_MAP, "").split(COLOR_SEPARATOR);
                String[] colorValues = replacementPair.getRight().replace(PNG_COLOR_MAP, "").split(COLOR_SEPARATOR);
                for (int i = 0; i < colorKeys.length; i++) {
                    colorReplacements.put(TransformParameters.stringToColor(colorKeys[i]), TransformParameters.stringToColor(colorValues[i]));
                }
            }
        }
        for (int x = 0; x < originalImage.getWidth(); x++) {
            for (int y = 0; y < originalImage.getHeight(); y++) {
                Color originalColor = TransformParameters.getColorWithAlpha(originalImage, originalAlpha, x, y);
                // We may not find a replacement for the color in the map, so we'll default to the original.
                Color newColor = colorReplacements.getOrDefault(originalColor, originalColor);
                newPng.setRGB(x, y, newColor.getRGB());
                newAlpha[x][y] = newColor.getAlpha();
            }
        }
        newPng.getAlphaRaster().setPixels(0, 0, newPng.getWidth(), newPng.getHeight(), TransformParameters.squareImageAlphaToLinear(newAlpha));
        return newPng;
    }

    // Perform each requested string substitution.
    private static String performAllReplacements(String s, Set<Pair<String, String>> replacements) {
        for (Pair<String, String> replacement : replacements) {
            s = s.replaceAll(replacement.getLeft(), replacement.getRight());
        }
        return s;
    }

    private static String mashSingleFile(File file) {
        String fullFile = file.getAbsolutePath();
        while (fullFile.contains(DOUBLE_DOT)) {
            int location = fullFile.indexOf(DOUBLE_DOT);
            int startOfFileAbove = StringUtils.lastIndexOf(fullFile, "\\", location - 2);
            fullFile = fullFile.substring(0, startOfFileAbove) + fullFile.substring(location + 2);
        }
        return fullFile;
    }

    public record TransformParameters(File sourceFile, String sourceFileNameAndSubDirs, File destinationParentDir,
                                      String destinationSubDirPattern, Map<String, List<String>> replacements) {
        // Multiplex all the replacements against each other, to get all combinations.
        // Split apart any correlated fields, and don't multiplex them against each other.
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
                    List<Pair<String, String>> originalAndReplacementPairs = makeReplacementPairs(originalString, replacementString);
                    for (Set<Pair<String, String>> setOfReplacements : replacementSetsTemp) {
                        Set<Pair<String, String>> setCopy = new HashSet<>(setOfReplacements);
                        setCopy.addAll(originalAndReplacementPairs);
                        allReplacementSets.add(setCopy);
                    }
                }
            }
            return allReplacementSets;
        }

        private static List<Pair<String, String>> makeReplacementPairs(String originalString, String replacementString) {
            String[] originalStrings = StringUtils.splitPreserveAllTokens(originalString, ESCAPED_CORRELATED_FIELD_SEPARATOR);
            String[] replacementStrings = StringUtils.splitPreserveAllTokens(replacementString, ESCAPED_CORRELATED_FIELD_SEPARATOR);
            if (replacementStrings.length == 0) {
                replacementStrings = new String[] {""};
            }
            List<Pair<String, String>> pairs = new ArrayList<>(originalStrings.length);
            for (int i = 0; i < originalStrings.length; i++) {
                pairs.add(Pair.of(originalStrings[i], replacementStrings[i]));
            }
            return pairs;
        }

        // Read a copier file and make a list of TransformParameters objects, one object for each section.
        public static List<TransformParameters> readFromFile(File file) throws IOException {
            List<TransformParameters> result = new ArrayList<>();
            String fileContents = FileUtils.readFileToString(file, UTF_8);
            String[] lines = fileContents.split(LINE_SEPARATOR);
            File parentFile = file.getParentFile();
            for (int i = 0; i < lines.length; i++) {
                // Skip over the comment lines and empty lines
                String currentLine = lines[i].strip();
                if (currentLine.isEmpty() || currentLine.startsWith(COMMENT)) {
                    continue;
                }
                // Now we have a transform definition
                String[] sourceAndDestFiles = currentLine.split(FIELD_SEPARATOR);
                List<File> sourceFiles = mashFileParts(new File(parentFile, sourceAndDestFiles[0]));
                Map<String, List<String>> replacements = new HashMap<>();
                i++;
                while (i < lines.length && !lines[i].isBlank()) {
                    currentLine = lines[i];
                    i++;
                    if (currentLine.strip().startsWith(COMMENT)) {
                        continue;
                    }
                    List<String> replacementTokens = Arrays.stream(currentLine.split(FIELD_SEPARATOR)).map(s -> s.replace(DISPOSABLE_CHARACTER, "")).collect(Collectors.toList());
                    String firstToken = replacementTokens.getFirst();
                    int pngFileNameIndex = fieldSectionWithPngExtension(firstToken);
                    if (pngFileNameIndex > -1) {
                        List<String> pngReplacements = parsePngReplacementList(new File(parentFile, firstToken.split(ESCAPED_CORRELATED_FIELD_SEPARATOR)[pngFileNameIndex]));
                        List<String> originalReplacementTokens = replacementTokens.stream().toList();
                        replacementTokens.clear();
                        for (int j = 0; j < pngReplacements.size(); j++) {
                            replacementTokens.add(replacePartOfToken(originalReplacementTokens.get(j), pngReplacements.get(j), pngFileNameIndex));
                        }
                    }
                    replacements.put(replacementTokens.removeFirst(), replacementTokens);
                }
                for (File sourceFile : sourceFiles) {
                    String sourceFileNameAndSubDirs = sourceFile.getAbsolutePath().replace(parentFile.getAbsolutePath(), "");
                    result.add(new TransformParameters(sourceFile, sourceFileNameAndSubDirs, parentFile, sourceAndDestFiles[1], replacements));
                }
            }
            return result;
        }

        private static String replacePartOfToken(String token, String replacement, int index) {
            String prefix = "";
            if (index > 0) {
                int sectionStart = StringUtils.ordinalIndexOf(token, CORRELATED_FIELD_SEPARATOR, index);
                prefix = token.substring(0, sectionStart);

            }
            int sectionEnd = StringUtils.ordinalIndexOf(token, CORRELATED_FIELD_SEPARATOR, index+1);
            // If there isn't an nth separator, that means we're working with the end of the string.
            sectionEnd = sectionEnd == -1 ? token.length() : sectionEnd;
            return prefix + CORRELATED_FIELD_SEPARATOR + replacement + token.substring(sectionEnd);
        }

        private static int fieldSectionWithPngExtension(String replacementKey) {
            int index = -1;
            String[] replacements = replacementKey.split(ESCAPED_CORRELATED_FIELD_SEPARATOR);
            for (int i = 0; i < replacements.length; i++) {
                if (replacements[i].endsWith(PNG_EXTENSION)) {
                    index = i;
                }
            }
            return index;
        }

        // Original pixel colors in the first column, sets of replacement colors in subsequent columns.
        private static List<String> parsePngReplacementList(File pngFile) throws IOException {
            BufferedImage image = ImageIO.read(pngFile);
            int[][] imageAlpha = getImageAlpha(image);
            List<List<Color>> colorColumns = new ArrayList<>();
            for (int x  = 0; x < image.getWidth(); x++) {
                List<Color> currentColumn = new ArrayList<>();
                for (int y = 0; y < image.getHeight(); y++) {
                    currentColumn.add(getColorWithAlpha(image, imageAlpha, x, y));
                }
                colorColumns.add(currentColumn);
            }
            List<String> colorColumnStrings = new ArrayList<>(colorColumns.size());
            for (List<Color> colorColumn : colorColumns) {
                colorColumnStrings.add(PNG_COLOR_MAP + colorColumn.stream().map(TransformParameters::colorToString).collect(Collectors.joining(COLOR_SEPARATOR)));
            }
            return colorColumnStrings;
        }

        // You may include a * in the filename, but only one *.
        private static List<File> mashFileParts(File file) {
            List<File> files = new ArrayList<>();
            String fullFile = mashSingleFile(file);
            if (fullFile.contains("*")) {
                String[] prefixAndSuffix = fullFile.split("\\*");
                if (prefixAndSuffix.length > 2) {
                    throw new RuntimeException("Sorry, a source filename may only contain one *, and this contained more: " + file.getAbsolutePath());
                }
                int splitPoint = getParentSplitPoint(prefixAndSuffix[0]);
                File parentFile = new File(fullFile.substring(0, splitPoint));
                String regex = fullFile.substring(splitPoint + 1).replace(".", "\\.").replace("*", ".*");

                String[] filenames = parentFile.list(new RegexFileFilter(regex));
                files.addAll(Arrays.stream(filenames).map(s -> new File(parentFile, s)).toList());
            } else {
                files.add(new File(fullFile));
            }
            return files;
        }

        private static int getParentSplitPoint(String filename) {
            int parentDirSplitPoint = filename.lastIndexOf("\\");
            if (parentDirSplitPoint == -1) {
                parentDirSplitPoint = filename.lastIndexOf("/");
            }
            return parentDirSplitPoint;
        }

        private static String colorToString(Color color) {
            return color.getRed() + COLOR_CHANNEL_SEPARATOR + color.getGreen() + COLOR_CHANNEL_SEPARATOR + color.getBlue() + COLOR_CHANNEL_SEPARATOR + color.getAlpha();
        }

        private static Color stringToColor(String string) {
            String[] tokens = string.split(COLOR_CHANNEL_SEPARATOR);
            return new Color(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]));
        }

        private static Color getColorWithAlpha(BufferedImage image, int[][] alphaRaster, int x, int y) {
            Color colorWithoutAlpha = new Color(image.getRGB(x, y));
            return new Color(colorWithoutAlpha.getRed(), colorWithoutAlpha.getGreen(), colorWithoutAlpha.getBlue(), alphaRaster[x][y]);
        }

        private static int[][] getImageAlpha(BufferedImage image) {
            int[][] imageAlpha = new int[image.getWidth()][image.getHeight()];
            int[] rawImageAlpha = new int[image.getWidth() * image.getHeight()];
            image.getAlphaRaster().getPixels(0, 0, image.getWidth(), image.getHeight(), rawImageAlpha);
            for (int i = 0; i < image.getWidth(); i++) {
                for (int j = 0; j < image.getHeight(); j++) {
                    imageAlpha[i][j] = rawImageAlpha[j * image.getWidth() + i];
                }
            }
            return imageAlpha;
        }

        private static int[] squareImageAlphaToLinear(int[][] squareAlpha) {
            int[] linearAlpha = new int[squareAlpha.length * squareAlpha[0].length];
            int width = squareAlpha.length;
            int height = squareAlpha[0].length;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    linearAlpha[j * width + i] = squareAlpha[i][j];
                }
            }
            return linearAlpha;
        }
    }
}
