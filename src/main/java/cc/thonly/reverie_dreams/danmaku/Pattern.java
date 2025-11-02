package cc.thonly.reverie_dreams.danmaku;

import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Getter
public class Pattern {
    public static class BuiltIn {
        public static final Pattern STAR = of(new String[]{
                "##   ##",
                " ## ## ",
                " # # # ",
                "##   ##",
                "  # #  ",
                "   #   "
        });
        public static final Pattern HEART = of(new String[] {
                "   #   ",
                "  # #  ",
                " # # # ",
                "# # # #",
                " #   # ",
        });
        public static final Pattern X = of(new String[] {
                "#   #",
                " # # ",
                "  #  ",
                " # # ",
                "#   #",
        });
    }
    private final List<String> patternString;

    public Pattern(List<String> patternString) {
        this.patternString = patternString;
    }

    public List<String> getPatternString() {
        return new LinkedList<>(this.patternString);
    }

    public char[][] getChars() {
        List<String> lines = getPatternString();
        if (lines.isEmpty()) return new char[0][0];

        int height = lines.size();
        int width = lines.stream().mapToInt(String::length).max().orElse(0);

        char[][] chars = new char[height][width];

        for (int y = 0; y < height; y++) {
            String line = lines.get(y);
            for (int x = 0; x < width; x++) {
                chars[y][x] = x < line.length() ? line.charAt(x) : ' ';
            }
        }

        return chars;
    }


    public static Pattern of(String[] strings) {
        return new Pattern(Arrays.stream(strings).toList());
    }

    public static Pattern of(String string) {
        return new Pattern(List.of(string));
    }
}
