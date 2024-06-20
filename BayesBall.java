import java.util.*;

public class BayesBall {
    private BayesianNetwork network;

    public BayesBall(BayesianNetwork network) {
        this.network = network;
    }

    public String run(String query) {
        System.out.println("Running query: " + query);
        String[] parts = query.split("\\|");
        if (parts.length != 1 && parts.length != 2) {
            return "Invalid query format, comes from BayesBall";
        }

        String[] nodes = parts[0].split("-");
        if (nodes.length != 2) {
            return "Invalid query format,comes from BayesBall";
        }

        String startNode = nodes[0].trim();
        String endNode = nodes[1].trim();
        Set<String> evidence = new HashSet<>();
        if (parts.length == 2 && !parts[1].trim().isEmpty()) {
            String[] evidences = parts[1].split(",");
            for (String e : evidences) {
                evidence.add(e.split("=")[0].trim());
            }
        }
        System.out.println("Parsed evidence: " + evidence);

        boolean independent = dSeparated(startNode, endNode, evidence);
        System.out.println("Independence check: " + independent);

        return independent ? "yes" : "no";
    }

    private boolean dSeparated(String start, String end, Set<String> evidence) {
        Set<Triple> visited = new HashSet<>();
        Queue<Triple> queue = new LinkedList<>();
        queue.add(new Triple(start, null, "up"));

        while (!queue.isEmpty()) {
            Triple triple = queue.poll();
            String current = triple.node;
            String prev = triple.prev;
            String direction = triple.direction;

            if (current.equals(end)) {
                return false;
            }

            if (visited.contains(triple)) {
                continue;
            }
            visited.add(triple);

            Node currentNode = network.getNodeByName(current);
            if (direction.equals("up")) {
                if (!evidence.contains(current)) {
                    for (Node parent : currentNode.getParents()) {
                        queue.add(new Triple(parent.getName(), current, "up"));
                    }
                    for (Node child : currentNode.getChildren()) {
                        queue.add(new Triple(child.getName(), current, "down"));
                    }
                }
            } else if (direction.equals("down")) {
                if (!evidence.contains(current)) {
                    for (Node child : currentNode.getChildren()) {
                        queue.add(new Triple(child.getName(), current, "down"));
                    }
                }
                if (prev == null || !isParent(currentNode, prev)) {
                    for (Node parent : currentNode.getParents()) {
                        queue.add(new Triple(parent.getName(), current, "up"));
                    }
                }

                // Handle v-structures
                if (isParent(currentNode, prev)) {
                    if (evidence.contains(current) || hasDescendantInEvidence(currentNode, evidence)) {
                        for (Node parent : currentNode.getParents()) {
                            queue.add(new Triple(parent.getName(), current, "up"));
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean isParent(Node node, String parentName) {
        for (Node parent : node.getParents()) {
            if (parent.getName().equals(parentName)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDescendantInEvidence(Node node, Set<String> evidence) {
        Queue<Node> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(node);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (evidence.contains(current.getName())) {
                return true;
            }
            visited.add(current.getName());

            for (Node child : current.getChildren()) {
                if (!visited.contains(child.getName())) {
                    queue.add(child);
                }
            }
        }
        return false;
    }

    private static class Triple {
        String node;
        String prev;
        String direction;

        Triple(String node, String prev, String direction) {
            this.node = node;
            this.prev = prev;
            this.direction = direction;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Triple triple = (Triple) obj;
            return Objects.equals(node, triple.node) &&
                    Objects.equals(prev, triple.prev) &&
                    Objects.equals(direction, triple.direction);
        }

        @Override
        public int hashCode() {
            return Objects.hash(node, prev, direction);
        }

        @Override
        public String toString() {
            return node + "-" + (prev == null ? "null" : prev) + "-" + direction;
        }
    }
}
