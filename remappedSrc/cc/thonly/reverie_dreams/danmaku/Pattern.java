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

    public static Pattern of(String[] strings) {
        return new Pattern(Arrays.stream(strings).toList());
    }

    public static Pattern of(String string) {
        return new Pattern(List.of(string));
    }
}
