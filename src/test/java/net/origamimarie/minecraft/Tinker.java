package net.origamimarie.minecraft;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Tinker {

    @Test
    public void foo() {
        List<String> colors = List.of("magenta", "red", "orange", "yellow", "lime", "cyan", "blue", "purple");
        List<String> moreColors = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            moreColors.addAll(colors);
        }
        Collections.shuffle(moreColors);
        System.out.println(moreColors.stream().map(c -> "\"" + c + "\"").collect(Collectors.joining(", ")));
    }
}
// "red", "lime", "yellow", "blue", "purple", "blue", "orange", "lime", "magenta", "cyan", "orange", "magenta", "orange", "red", "cyan", "purple", "lime", "cyan", "blue", "purple", "blue", "red", "yellow", "cyan", "yellow", "magenta", "orange", "yellow", "red", "lime", "magenta", "purple"