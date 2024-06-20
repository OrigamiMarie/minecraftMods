package net.origamimarie.minecraft;

import net.origamimarie.minecraft.util.ResourceCopyTransformer;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.origamimarie.minecraft.util.ResourceCopyTransformer.TransformParameters.UP_ONE_FOLDER;

public class Tinker {

    @Test
    public void foo() {
        System.out.println(mashFileParts(new File("hello/world/../hey")));
    }

    private static File mashFileParts(File file) {
        String fullFile = file.getAbsolutePath();
        System.out.println(fullFile);
        while (fullFile.contains("..")) {
            int location = fullFile.indexOf("..");
            System.out.println(location);
            int startOfFileAbove = StringUtils.lastIndexOf(fullFile, "\\", location - 2);
            System.out.println(startOfFileAbove);
            fullFile = fullFile.substring(0, startOfFileAbove) + fullFile.substring(location + 2);
        }
        return new File(fullFile);
    }

}
