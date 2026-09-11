package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.*;

@Getter
public class GomokuBlockEntity extends BlockEntity {

    public static final int MAX_X = 15;
    public static final int MAX_Y = 15;

    /**
     * grid[x][y]
     *
     * 0 = EMPTY
     * 1 = BLACK
     * 2 = WHITE
     */
    private final List<List<Integer>> grid = new ArrayList<>();

    /**
     * 玩家 -> 棋子颜色
     */
    private final BiMap<Element, UUID> elements = HashBiMap.create();

    /**
     * 当前轮到谁
     */
    private Element currentTurn = Element.BLACK;

    /**
     * 游戏是否已经结束
     */
    private boolean gameOver = false;

    /**
     * 获胜方
     */
    private Element winner = Element.EMPTY;

    public GomokuBlockEntity(
            BlockPos worldPosition,
            BlockState blockState
    ) {
        super(RDBlockEntityTypes.GOMOKU.value(), worldPosition, blockState);
        fill();
    }

    /**
     * 初始化棋盘
     */
    private void fill() {
        this.grid.clear();

        for (int x = 0; x < MAX_X; x++) {
            List<Integer> column = new ArrayList<>(MAX_Y);

            for (int y = 0; y < MAX_Y; y++) {
                column.add(Element.EMPTY.number);
            }

            this.grid.add(column);
        }

        this.currentTurn = Element.BLACK;
        this.gameOver = false;
        this.winner = Element.EMPTY;

        setChanged();
    }

    /**
     * 让玩家加入游戏
     *
     * @return 是否成功加入
     */
    public boolean join(LivingEntity entity, Element element) {
        if (entity == null || element == null) {
            return false;
        }

        if (element == Element.EMPTY) {
            return false;
        }

        UUID uuid = entity.getUUID();

        // 已经加入
        Element existing = getElement(uuid);
        if (existing != null) {
            return existing == element;
        }

        // 颜色已经被占用
        if (this.elements.containsKey(element)) {
            return false;
        }

        // 最多两个玩家
        if (this.elements.size() >= 2) {
            return false;
        }

        this.elements.put(element, uuid);

        setChanged();
        return true;
    }

    /**
     * 获取 UUID 对应的棋子颜色
     */
    public Element getElement(UUID uuid) {
        for (Map.Entry<Element, UUID> entry : this.elements.entrySet()) {
            if (entry.getValue().equals(uuid)) {
                return entry.getKey();
            }
        }

        return null;
    }

    /**
     * 玩家是否已经加入
     */
    public boolean hasPlayer(LivingEntity entity) {
        return entity != null && getElement(entity.getUUID()) != null;
    }

    /**
     * 落子
     */
    public boolean place(LivingEntity entity, int x, int y) {
        if (entity == null) {
            return false;
        }

        if (this.gameOver) {
            return false;
        }

        // 坐标越界
        if (!inBounds(x, y)) {
            return false;
        }

        // 获取玩家颜色
        Element element = getElement(entity.getUUID());
        if (element == null || element == Element.EMPTY) {
            return false;
        }

        // 不是当前回合
        if (element != this.currentTurn) {
            return false;
        }

        // 已经有棋子
        if (get(x, y) != Element.EMPTY) {
            return false;
        }

        // 落子
        set(x, y, element);

        // 判断胜负
        if (checkWin(x, y, element)) {
            this.gameOver = true;
            this.winner = element;
        } else if (isFull()) {
            this.gameOver = true;
            this.winner = Element.EMPTY;
        } else {
            this.currentTurn = opposite(element);
        }

        setChanged();
        return true;
    }

    /**
     * 获取棋盘元素
     */
    public Element get(int x, int y) {
        if (!inBounds(x, y)) {
            return Element.EMPTY;
        }

        return Element.fromNumber(grid.get(x).get(y));
    }

    /**
     * 设置棋盘元素
     */
    private void set(int x, int y, Element element) {
        if (!inBounds(x, y)) {
            return;
        }

        this.grid.get(x).set(y, element.number);
    }

    /**
     * 判断坐标是否合法
     */
    public boolean inBounds(int x, int y) {
        return x >= 0
                && x < MAX_X
                && y >= 0
                && y < MAX_Y;
    }

    /**
     * 判断某一步是否形成五连
     */
    public boolean checkWin(int x, int y, Element element) {
        if (element == Element.EMPTY) {
            return false;
        }

        // 横向
        if (countDirection(x, y, 1, 0, element)
                + countDirection(x, y, -1, 0, element) - 1 >= 5) {
            return true;
        }

        // 纵向
        if (countDirection(x, y, 0, 1, element)
                + countDirection(x, y, 0, -1, element) - 1 >= 5) {
            return true;
        }

        // 左上 -> 右下
        if (countDirection(x, y, 1, 1, element)
                + countDirection(x, y, -1, -1, element) - 1 >= 5) {
            return true;
        }

        // 右上 -> 左下
        if (countDirection(x, y, 1, -1, element)
                + countDirection(x, y, -1, 1, element) - 1 >= 5) {
            return true;
        }

        return false;
    }

    private int countDirection(
            int startX,
            int startY,
            int dx,
            int dy,
            Element element
    ) {
        int count = 0;

        int x = startX;
        int y = startY;

        while (inBounds(x, y) && get(x, y) == element) {
            count++;
            x += dx;
            y += dy;
        }

        return count;
    }

    public boolean isFull() {
        for (int x = 0; x < MAX_X; x++) {
            for (int y = 0; y < MAX_Y; y++) {
                if (get(x, y) == Element.EMPTY) {
                    return false;
                }
            }
        }

        return true;
    }

    public static Element opposite(Element element) {
        return switch (element) {
            case BLACK -> Element.WHITE;
            case WHITE -> Element.BLACK;
            case EMPTY -> Element.EMPTY;
        };
    }

    public void resetGame() {
        this.elements.clear();
        this.fill();
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putString("CurrentTurn", currentTurn.name());
        output.putBoolean("GameOver", gameOver);
        output.putString("Winner", winner.name());

        for (int x = 0; x < MAX_X; x++) {
            for (int y = 0; y < MAX_Y; y++) {
                output.putInt(
                        "Board_" + x + "_" + y,
                        this.grid.get(x).get(y)
                );
            }
        }


        for (Element element : List.of(Element.BLACK, Element.WHITE)) {
            UUID uuid = this.elements.get(element);

            if (uuid != null) {
                output.putString(
                        "Player_" + element.name(),
                        uuid.toString()
                );
            }
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.fill();
        try {
            this.currentTurn = Element.valueOf(input.getString("CurrentTurn").orElse("BLACK"));
        } catch (Exception ignored) {
            this.currentTurn = Element.BLACK;
        }

        this.gameOver = input.getBooleanOr("GameOver", false);
        try {
            winner = Element.valueOf(input.getString("Winner").orElse("EMPTY"));
        } catch (Exception ignored) {
            winner = Element.EMPTY;
        }

        for (int x = 0; x < MAX_X; x++) {
            for (int y = 0; y < MAX_Y; y++) {
                String key = "Board_" + x + "_" + y;
                int value = input.getInt(key).orElse(0);
                if (value < Element.EMPTY.number
                        || value > Element.WHITE.number) {
                    value = Element.EMPTY.number;
                }
                this.grid.get(x).set(y, value);
            }
        }

        this.elements.clear();

        for (Element element : List.of(Element.BLACK, Element.WHITE)) {
            String key = "Player_" + element.name();

            input.getString(key).ifPresent(value -> {
                try {
                    this.elements.put(element, UUID.fromString(value));
                } catch (IllegalArgumentException ignored) {
                }
            });
        }
    }

    @Getter
    public enum Element {
        EMPTY(0),
        BLACK(1),
        WHITE(2);

        private final int number;

        Element(int number) {
            this.number = number;
        }

        public static Element fromNumber(int number) {
            return switch (number) {
                case 1 -> BLACK;
                case 2 -> WHITE;
                default -> EMPTY;
            };
        }
    }
}