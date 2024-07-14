package net.origamimarie.minecraft.util;

import com.google.common.base.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

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
    private static final String RESOURCES_DIR = "C:/Users/origa/java/minecraftMods/src/main/resources/";
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

    // Perform each requested string substitution.
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
        public static final String FIELD_SEPARATOR = "; ";
        public static final String ESCAPED_CORRELATED_FIELD_SEPARATOR = "\\|";

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
            String[] originalStrings = originalString.split(ESCAPED_CORRELATED_FIELD_SEPARATOR);
            String[] replacementStrings = replacementString.split(ESCAPED_CORRELATED_FIELD_SEPARATOR);
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
                if (currentLine.length() == 0 || currentLine.startsWith(COMMENT)) {
                    continue;
                }
                // Now we have a transform definition
                String[] sourceAndDestFiles = currentLine.split(FIELD_SEPARATOR);
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
                    List<String> replacementTokens = new ArrayList<>(Arrays.asList(currentLine.split(FIELD_SEPARATOR)));
                    String key = replacementTokens.remove(0);
                    replacements.put(key, replacementTokens);
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
