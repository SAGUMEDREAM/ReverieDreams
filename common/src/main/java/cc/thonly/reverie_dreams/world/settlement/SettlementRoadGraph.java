package cc.thonly.reverie_dreams.world.settlement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Pure graph selection for registered settlement sites; callers own block activity checks.
 */
public final class SettlementRoadGraph {
    private static final int MAX_SITES = 32;
    private static final int MAX_ROADS = 64;
    private static final Comparator<SearchState> SEARCH_ORDER = (left, right) -> {
        int byCost = Double.compare(left.cost(), right.cost());
        return byCost != 0 ? byCost : comparePaths(left.path(), right.path());
    };

    private SettlementRoadGraph() {
    }

    public record Edge(Identifier a, Identifier b) {
        static final Codec<Edge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("a").forGetter(Edge::a),
                Identifier.CODEC.fieldOf("b").forGetter(Edge::b)
        ).apply(instance, Edge::new));

        Edge reverse() {
            return new Edge(this.b, this.a);
        }
    }

    static boolean isValid(Map<Identifier, BlockPos> sites, List<Edge> roads) {
        if (sites == null || sites.size() > MAX_SITES || roads == null || roads.size() > MAX_ROADS) {
            return false;
        }

        Set<Edge> seen = new HashSet<>(Math.max(16, roads.size() * 2));
        for (Edge road : roads) {
            if (road == null) {
                return false;
            }
            BlockPos a = sites.get(road.a());
            BlockPos b = sites.get(road.b());
            if (a == null || b == null || a.equals(b)
                    || a.distSqr(b) > SettlementGroundNavigation.MAX_LEG_SQR) {
                return false;
            }
            if (!seen.add(road) || !seen.add(road.reverse())) {
                return false;
            }
        }
        return true;
    }

    static List<Identifier> route(Map<Identifier, BlockPos> sites, List<Edge> roads, BlockPos from, BlockPos to,
                                  Predicate<BlockPos> active, Set<Edge> blocked) {
        if (sites == null || roads == null || active == null || blocked == null || from == null || to == null
                || sites.size() > MAX_SITES || !isValid(sites, roads)) {
            return List.of();
        }

        if (from.equals(to)) {
            Optional<Identifier> ending = activeSiteAt(sites, to, active);
            return ending.isPresent() ? List.of(ending.get()) : List.of();
        }

        Set<Identifier> roadSites = new HashSet<>();
        for (Edge road : roads) {
            roadSites.add(road.a());
            roadSites.add(road.b());
        }

        Map<Identifier, List<Identifier>> graph = adjacency(sites, roads, blocked);
        List<Identifier> starts = entrySites(sites, roadSites, from, active);
        if (starts.isEmpty()) {
            return List.of();
        }

        PriorityQueue<SearchState> queue = new PriorityQueue<>(SEARCH_ORDER);
        for (Identifier start : starts) {
            BlockPos position = sites.get(start);
            queue.add(new SearchState(Math.sqrt(from.distSqr(position)), List.of(start)));
        }

        Set<Identifier> settled = new HashSet<>();
        while (!queue.isEmpty()) {
            SearchState state = queue.poll();
            Identifier site = state.path().getLast();
            if (!settled.add(site)) {
                continue;
            }

            BlockPos position = sites.get(site);
            if (position.equals(to)) {
                return state.path();
            }
            for (Identifier neighbor : graph.get(site)) {
                BlockPos neighborPosition = sites.get(neighbor);
                if (neighborPosition == null || settled.contains(neighbor) || !active.test(neighborPosition)
                        || isBlocked(site, neighbor, blocked)) {
                    continue;
                }
                double legCost = Math.sqrt(position.distSqr(neighborPosition));
                queue.add(new SearchState(state.cost() + legCost, append(state.path(), neighbor)));
            }
        }
        return List.of();
    }

    private static Map<Identifier, List<Identifier>> adjacency(Map<Identifier, BlockPos> sites, List<Edge> roads,
                                                               Set<Edge> blocked) {
        Map<Identifier, List<Identifier>> graph = new HashMap<>();
        for (Identifier site : sites.keySet()) {
            graph.put(site, new ArrayList<>());
        }
        for (Edge road : roads) {
            if (road == null || !sites.containsKey(road.a()) || !sites.containsKey(road.b())
                    || isBlocked(road.a(), road.b(), blocked)) {
                continue;
            }
            graph.get(road.a()).add(road.b());
            graph.get(road.b()).add(road.a());
        }
        for (List<Identifier> neighbors : graph.values()) {
            sortIdentifiers(neighbors);
        }
        return graph;
    }

    private static List<Identifier> entrySites(Map<Identifier, BlockPos> sites, Set<Identifier> roadSites, BlockPos from,
                                               Predicate<BlockPos> active) {
        List<Identifier> exact = new ArrayList<>();
        boolean atRoadSite = false;
        List<Identifier> near = new ArrayList<>();
        for (Map.Entry<Identifier, BlockPos> entry : sites.entrySet()) {
            if (!roadSites.contains(entry.getKey())) {
                continue;
            }
            BlockPos position = entry.getValue();
            if (position == null) {
                continue;
            }
            if (position.equals(from)) {
                atRoadSite = true;
                if (active.test(position)) {
                    exact.add(entry.getKey());
                }
            } else if (from.distSqr(position) <= SettlementGroundNavigation.MAX_LEG_SQR) {
                if (active.test(position)) {
                    near.add(entry.getKey());
                }
            }
        }

        List<Identifier> starts = atRoadSite ? exact : near;
        sortIdentifiers(starts);
        return starts;
    }

    private static Optional<Identifier> activeSiteAt(Map<Identifier, BlockPos> sites, BlockPos position,
                                                     Predicate<BlockPos> active) {
        List<Identifier> matches = new ArrayList<>();
        for (Map.Entry<Identifier, BlockPos> entry : sites.entrySet()) {
            if (entry.getValue() != null && entry.getValue().equals(position) && active.test(entry.getValue())) {
                matches.add(entry.getKey());
            }
        }
        sortIdentifiers(matches);
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.getFirst());
    }

    private static boolean isBlocked(Identifier a, Identifier b, Set<Edge> blocked) {
        Edge edge = new Edge(a, b);
        return blocked.contains(edge) || blocked.contains(edge.reverse());
    }

    private static List<Identifier> append(List<Identifier> path, Identifier site) {
        List<Identifier> result = new ArrayList<>(path.size() + 1);
        result.addAll(path);
        result.add(site);
        return List.copyOf(result);
    }

    private static void sortIdentifiers(List<Identifier> identifiers) {
        identifiers.sort(Identifier::compareTo);
    }

    private static int comparePaths(List<Identifier> left, List<Identifier> right) {
        int commonLength = Math.min(left.size(), right.size());
        for (int i = 0; i < commonLength; i++) {
            int result = left.get(i).compareTo(right.get(i));
            if (result != 0) {
                return result;
            }
        }
        return Integer.compare(left.size(), right.size());
    }

    private record SearchState(double cost, List<Identifier> path) {
    }
}
