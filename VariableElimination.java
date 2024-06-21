import java.util.*;

public class VariableElimination {
    private BayesianNetwork network;
    private int totalAdditionCount;
    private int totalMultiplicationCount;
    private boolean debug = true;

    public VariableElimination(BayesianNetwork network) {
        this.network = network;
    }

    private void debugPrint(String message) {
        if (debug) {
            System.out.println(message);
        }
    }

    public String run(String query) {
        this.totalAdditionCount = 0;
        this.totalMultiplicationCount = 0;

        debugPrint("Processing query: " + query);

        ParsedQuery parsedQuery = parseQuery(query);
        Set<String> relevantVariables = preprocessNetwork(parsedQuery);

        debugPrint("Relevant variables: " + relevantVariables);

        List<Factor> factors = initializeFactors(parsedQuery.getEvidence(), relevantVariables);

        debugPrint("Initial factors: " + factors.size());

        for (String var : parsedQuery.getEliminationOrder()) {
            if (relevantVariables.contains(var) && !parsedQuery.getEvidence().containsKey(var) && !var.equals(parsedQuery.getQueryVariable())) {
                debugPrint("Eliminating variable: " + var);
                List<Factor> relevantFactors = getRelevantFactors(factors, var);
                factors.removeAll(relevantFactors);
                Factor newFactor = multiplyAndSumOut(relevantFactors, var);
                factors.add(newFactor);
                factors.sort(Comparator.comparingInt(f -> f.getVariables().size()));
                debugPrint("Factors after elimination: " + factors.size());
            }
        }

        Factor resultFactor = multiplyAllFactors(factors);

        if (resultFactor.getVariables().size() > 1) {
            debugPrint("Normalizing result factor");
            resultFactor = normalizeFactor(resultFactor);
        }

        List<String> finalKey = constructFinalKey(resultFactor, parsedQuery);
        Double resultValue = resultFactor.getCpt().get(finalKey);

        if (resultValue == null) {
            System.err.println("Key not found in CPT: " + finalKey);
            for (List<String> key : resultFactor.getCpt().keySet()) {
                System.err.println("CPT key: " + key);
            }
            return "0.00000," + totalAdditionCount + "," + totalMultiplicationCount;
        }

        debugPrint("Final counts - Additions: " + totalAdditionCount + ", Multiplications: " + totalMultiplicationCount);

        return String.format("%.5f,%d,%d", resultValue, totalAdditionCount, totalMultiplicationCount);
    }

    private List<String> constructFinalKey(Factor factor, ParsedQuery parsedQuery) {
        List<String> finalKey = new ArrayList<>();
        for (String var : factor.getVariables()) {
            if (parsedQuery.getQueryVariable().equals(var)) {
                finalKey.add(var + "=" + parsedQuery.getQueryValue());
            } else if (parsedQuery.getEvidence().containsKey(var)) {
                finalKey.add(var + "=" + parsedQuery.getEvidence().get(var));
            }
        }
        return finalKey;
    }

    private ParsedQuery parseQuery(String query) {
        String[] parts = query.split(" ");
        String[] queryParts = parts[0].substring(2, parts[0].length() - 1).split("\\|");
        String[] eliminationOrder = parts[1].split("-");

        String[] queryVariable = queryParts[0].split("=");
        String queryVar = queryVariable[0];
        String queryValue = queryVariable[1];

        Map<String, String> evidence = new HashMap<>();
        if (queryParts.length > 1) {
            String[] evidenceParts = queryParts[1].split(",");
            for (String e : evidenceParts) {
                String[] ev = e.split("=");
                evidence.put(ev[0], ev[1]);
            }
        }

        return new ParsedQuery(queryVar, queryValue, evidence, eliminationOrder);
    }

    private Set<String> preprocessNetwork(ParsedQuery parsedQuery) {
        Set<String> relevantVariables = new HashSet<>();
        relevantVariables.add(parsedQuery.getQueryVariable());
        relevantVariables.addAll(parsedQuery.getEvidence().keySet());

        // Find all ancestors of query variable and evidence variables
        Set<String> ancestorVariables = findAncestors(relevantVariables);

        BayesBall bayesBall = new BayesBall(network);

        for (String variable : ancestorVariables) {
            if (!relevantVariables.contains(variable)) {
                String query = parsedQuery.getQueryVariable() + "-" + variable;
                if (!parsedQuery.getEvidence().isEmpty()) {
                    query += "|" + String.join(",", parsedQuery.getEvidence().keySet());
                }

                String result = bayesBall.run(query);
                if (result.equals("no")) {
                    relevantVariables.add(variable);
                }
            }
        }

        debugPrint("Relevant variables after Bayes Ball and ancestor filtering: " + relevantVariables);
        return relevantVariables;
    }

    private Set<String> findAncestors(Set<String> startNodes) {
        Set<String> ancestors = new HashSet<>(startNodes);
        Queue<String> queue = new LinkedList<>(startNodes);

        while (!queue.isEmpty()) {
            String currentNode = queue.poll();
            Node node = network.getNodeByName(currentNode);

            for (Node parent : node.getParents()) {
                String parentName = parent.getName();
                if (!ancestors.contains(parentName)) {
                    ancestors.add(parentName);
                    queue.add(parentName);
                }
            }
        }

        return ancestors;
    }

    private List<Factor> initializeFactors(Map<String, String> evidence, Set<String> relevantVariables) {
        List<Factor> factors = new ArrayList<>();

        for (Node node : network.getNodes().values()) {
            if (relevantVariables.contains(node.getName())) {
                Map<String, String> nodeEvidence = new HashMap<>();
                for (Node parent : node.getParents()) {
                    if (evidence.containsKey(parent.getName())) {
                        nodeEvidence.put(parent.getName(), evidence.get(parent.getName()));
                    }
                }
                if (evidence.containsKey(node.getName())) {
                    nodeEvidence.put(node.getName(), evidence.get(node.getName()));
                }
                Factor factor = new Factor(node, nodeEvidence);
                if (factor.getCpt().size() > 1) {
                    factors.add(factor);
                }
            }
        }

        return factors;
    }

    private List<Factor> getRelevantFactors(List<Factor> factors, String var) {
        List<Factor> relevantFactors = new ArrayList<>();
        for (Factor factor : factors) {
            if (factor.containsVariable(var)) {
                relevantFactors.add(factor);
            }
        }
        relevantFactors.sort(Comparator.comparingInt(f -> f.getVariables().size()));
        return relevantFactors;
    }

    private Factor multiplyAndSumOut(List<Factor> factors, String var) {
        debugPrint("Multiplying and summing out " + factors.size() + " factors for variable " + var);
        Factor result = factors.get(0);
        for (int i = 1; i < factors.size(); i++) {
            int[] counts = result.multiply(factors.get(i));
            this.totalAdditionCount += counts[0];
            this.totalMultiplicationCount += counts[1];
            debugPrint("After multiplication " + i + ": Additions=" + totalAdditionCount + ", Multiplications=" + totalMultiplicationCount);
        }
        int[] sumOutCounts = result.sumOut(var);
        this.totalAdditionCount += sumOutCounts[0];
        this.totalMultiplicationCount += sumOutCounts[1];
        debugPrint("After summing out: Additions=" + totalAdditionCount + ", Multiplications=" + totalMultiplicationCount);
        return result;
    }

    private Factor multiplyAllFactors(List<Factor> factors) {
        Factor resultFactor = factors.get(0);
        for (int i = 1; i < factors.size(); i++) {
            int[] counts = resultFactor.multiply(factors.get(i));
            this.totalAdditionCount += counts[0];
            this.totalMultiplicationCount += counts[1];
        }
        return resultFactor;
    }

    private Factor normalizeFactor(Factor factor) {
        int[] counts = factor.normalize();
        this.totalAdditionCount += counts[0];
        this.totalMultiplicationCount += counts[1];
        return factor;
    }

    private class ParsedQuery {
        private String queryVariable;
        private String queryValue;
        private Map<String, String> evidence;
        private String[] eliminationOrder;

        public ParsedQuery(String queryVariable, String queryValue, Map<String, String> evidence, String[] eliminationOrder) {
            this.queryVariable = queryVariable;
            this.queryValue = queryValue;
            this.evidence = evidence;
            this.eliminationOrder = eliminationOrder;
        }

        public String getQueryVariable() {
            return queryVariable;
        }

        public String getQueryValue() {
            return queryValue;
        }

        public Map<String, String> getEvidence() {
            return evidence;
        }

        public String[] getEliminationOrder() {
            return eliminationOrder;
        }
    }
}