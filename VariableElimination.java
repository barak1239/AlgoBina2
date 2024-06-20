import java.util.*;

public class VariableElimination {
    private BayesianNetwork network;
    private int additionCount;
    private int multiplicationCount;

    public VariableElimination(BayesianNetwork network) {
        this.network = network;
        this.additionCount = 0;
        this.multiplicationCount = 0;
    }

    public String run(String query) {
        // Parse the VE query
        ParsedQuery parsedQuery = parseQuery(query);

        // Generate initial factors considering the evidence
        List<Factor> factors = initializeFactors(parsedQuery.getEvidence());

        // Initialize BayesBall for dependency checking
        BayesBall ball = new BayesBall(network);
        List<String> eliminationOrder = new ArrayList<>(Arrays.asList(parsedQuery.getEliminationOrder()));

        // Remove variables from elimination order that are not ancestors of the query variable or are independent of the query variable given the evidence
        Iterator<String> iterator = eliminationOrder.iterator();
        while (iterator.hasNext()) {
            String var = iterator.next();
            if (!isAncestor(var, parsedQuery.getQueryVariable(), parsedQuery.getEvidence()) ||
                    ball.run(parsedQuery.getQueryVariable() + "-" + var + "|" + String.join(",", parsedQuery.getEvidence().keySet())).equals("yes")) {
                factors.removeIf(factor -> factor.containsVariable(var));
                iterator.remove();
            }
        }

        // Process each variable in the elimination order
        for (String var : eliminationOrder) {
            List<Factor> relevantFactors = getRelevantFactors(factors, var);
            factors.removeAll(relevantFactors);
            Factor newFactor = multiplyAndSumOut(relevantFactors, var);
            factors.add(newFactor);
            factors.sort(Comparator.comparingInt(f -> f.getVariables().size()));
        }

        // Multiply all remaining factors to get the final result
        Factor resultFactor = multiplyAllFactors(factors);

        // Normalize the resulting factor if it contains more than one variable
        if (resultFactor.getVariables().size() > 1) {
            resultFactor = normalizeFactor(resultFactor);
        }

        // Construct the key for the final lookup in the correct order
        List<String> finalKey = constructFinalKey(resultFactor, parsedQuery);

        Double resultValue = resultFactor.getCpt().get(finalKey);

        if (resultValue == null) {
            System.err.println("Key not found in CPT: " + finalKey);
            for (List<String> key : resultFactor.getCpt().keySet()) {
                System.err.println("CPT key: " + key);
            }
            return "0.00000," + additionCount + "," + multiplicationCount;
        }

        double result = resultValue;
        return String.format("%.5f,%d,%d", result, additionCount, multiplicationCount);
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

    private List<Factor> initializeFactors(Map<String, String> evidence) {
        List<Factor> factors = new ArrayList<>();

        for (Node node : network.getNodes().values()) {
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
        Factor result = factors.get(0);
        for (int i = 1; i < factors.size(); i++) {
            result = result.multiply(factors.get(i));
            multiplicationCount += result.getMultiplicationCount();
        }
        return result.sumOut(var);
    }

    private Factor multiplyAllFactors(List<Factor> factors) {
        Factor resultFactor = factors.get(0);
        for (int i = 1; i < factors.size(); i++) {
            resultFactor = resultFactor.multiply(factors.get(i));
            multiplicationCount += resultFactor.getMultiplicationCount();
        }
        return resultFactor;
    }

    private Factor normalizeFactor(Factor factor) {
        Factor normalizedFactor = factor.normalize();
        additionCount += factor.getAdditionCount();
        return normalizedFactor;
    }

    private boolean isAncestor(String hidden, String queryVar, Map<String, String> evidence) {
        if (isAncestor(hidden, queryVar)) {
            return true;
        }
        for (String var : evidence.keySet()) {
            if (isAncestor(hidden, var)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAncestor(String hidden, String current) {
        if (hidden.equals(current)) {
            return true;
        }
        Node currentNode = network.getNodeByName(current);
        for (Node parent : currentNode.getParents()) {
            if (isAncestor(hidden, parent.getName())) {
                return true;
            }
        }
        return false;
    }

    // Helper class to store parsed query details
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
