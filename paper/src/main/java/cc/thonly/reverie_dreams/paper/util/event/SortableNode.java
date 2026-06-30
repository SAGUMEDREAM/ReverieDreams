package cc.thonly.reverie_dreams.paper.util.event;

import java.util.ArrayList;
import java.util.List;

public abstract class SortableNode<N extends SortableNode<N>> {
    protected final List<N> subsequentNodes = new ArrayList<>();
    protected final List<N> previousNodes = new ArrayList<>();
    boolean visited = false;

    protected abstract String getDescription();

    protected void addSubsequentNode(N node) {
        this.subsequentNodes.add(node);
    }

    protected void addPreviousNode(N node) {
        this.previousNodes.add(node);
    }

    public static <N extends SortableNode<N>> void link(N first, N second) {
        if (first == second) {
            throw new IllegalArgumentException("Cannot link a node to itself!");
        } else {
            first.addSubsequentNode(second);
            second.addPreviousNode(first);
        }
    }
}
