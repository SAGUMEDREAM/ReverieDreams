package cc.thonly.reverie_dreams.paper.util.event;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"UnusedReturnValue", "rawtypes", "unchecked"})
public class NodeSorting {
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-api-base");
    @VisibleForTesting
    public static boolean ENABLE_CYCLE_WARNING = true;

    public static <N extends SortableNode<N>> boolean sort(List<N> sortedNodes, String elementDescription, Comparator<N> comparator) {
        List<N> toposort = new ArrayList<>(sortedNodes.size());

        for(N node : sortedNodes) {
            forwardVisit(node, null, toposort);
        }

        clearStatus(toposort);
        Collections.reverse(toposort);
        Map<N, NodeScc<N>> nodeToScc = new IdentityHashMap<>();

        for(N node : toposort) {
            if (!node.visited) {
                List<N> sccNodes = new ArrayList<>();
                backwardVisit(node, sccNodes);
                sccNodes.sort(comparator);
                NodeScc<N> scc = new NodeScc<N>(sccNodes);

                for(N nodeInScc : sccNodes) {
                    nodeToScc.put(nodeInScc, scc);
                }
            }
        }

        clearStatus(toposort);

        for(NodeScc<N> scc : nodeToScc.values()) {
            for(N node : scc.nodes) {
                for(N subsequentNode : node.subsequentNodes) {
                    NodeScc<N> subsequentScc = (NodeScc)nodeToScc.get(subsequentNode);
                    if (subsequentScc != scc) {
                        scc.subsequentSccs.add(subsequentScc);
                        ++subsequentScc.inDegree;
                    }
                }
            }
        }
        PriorityQueue<NodeScc<N>> pq = new PriorityQueue(Comparator.comparing((sccx) -> ((NodeScc)sccx).nodes.getFirst(), comparator));
        sortedNodes.clear();

        for(NodeScc<N> scc : nodeToScc.values()) {
            if (scc.inDegree == 0) {
                pq.add(scc);
                scc.inDegree = -1;
            }
        }

        boolean noCycle = true;

        while(!pq.isEmpty()) {
            NodeScc<N> scc = pq.poll();
            sortedNodes.addAll(scc.nodes);
            if (scc.nodes.size() > 1) {
                noCycle = false;
                if (ENABLE_CYCLE_WARNING) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("Found cycle while sorting ").append(elementDescription).append(":\n");

                    for(N node : scc.nodes) {
                        builder.append("\t").append(node.getDescription()).append("\n");
                    }

                    LOGGER.warn(builder.toString());
                }
            }

            for(NodeScc<N> subsequentScc : scc.subsequentSccs) {
                --subsequentScc.inDegree;
                if (subsequentScc.inDegree == 0) {
                    pq.add(subsequentScc);
                }
            }
        }

        return noCycle;
    }

    private static <N extends SortableNode<N>> void forwardVisit(N node, N parent, List<N> toposort) {
        if (!node.visited) {
            node.visited = true;

            for(N data : node.subsequentNodes) {
                forwardVisit(data, node, toposort);
            }

            toposort.add(node);
        }

    }

    private static <N extends SortableNode<N>> void clearStatus(List<N> nodes) {
        for(N node : nodes) {
            node.visited = false;
        }

    }

    private static <N extends SortableNode<N>> void backwardVisit(N node, List<N> sccNodes) {
        if (!node.visited) {
            node.visited = true;
            sccNodes.add(node);

            for(N data : node.previousNodes) {
                backwardVisit(data, sccNodes);
            }
        }

    }

    private static class NodeScc<N extends SortableNode<N>> {
        final List<N> nodes;
        final List<NodeScc<N>> subsequentSccs = new ArrayList();
        int inDegree = 0;

        private NodeScc(List<N> nodes) {
            this.nodes = nodes;
        }
    }
}
