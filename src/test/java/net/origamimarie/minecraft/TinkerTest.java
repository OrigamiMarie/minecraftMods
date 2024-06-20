package net.origamimarie.minecraft;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.File;

public class TinkerTest {

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
