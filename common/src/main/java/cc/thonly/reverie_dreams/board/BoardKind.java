package cc.thonly.reverie_dreams.board;

public enum BoardKind {
    XIANGQI(9, 10, "xiangqi_board"), CHESS(8, 8, "chess_board"), GO(9, 9, "go_board");
    public final int width, height;
    public final String id;

    BoardKind(int width, int height, String id) {
        this.width = width;
        this.height = height;
        this.id = id;
    }

    public String key() {
        return "block.reverie_dreams." + id;
    }
}
