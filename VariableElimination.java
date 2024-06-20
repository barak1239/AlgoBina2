import java.util.*;

public class VariableElimination {
    private BayesianNetwork network;
    private int additionCount;
    private int multiplicationCount;

    public VariableElimination(BayesianNetwork network) {
        this.network = network;
    }

    public String run(String query) {
        // Reset operation counts
        additionCount = 0;
        multiplicationCount = 0;

        // Parse the query and extract the relevant information
        String[] parts = query.substring(2, query.length() - 1).split("\\|");
        String[] queryVariables = parts[0].split(",");
        Map<String, String> evidenceMap = new HashMap<>();

        if (parts.length > 1) {
            String[] evidenceList = parts[1].split(",");
            for (String evidence : evidenceList) {
                String[] keyValue = evidence.split("=");
                evidenceMap.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }

        // Get the factors from the Bayesian network
        List<Factor> factors = new ArrayList<>();
        for (Node node : network.getNodes().values()) {
            factors.add(node.toFactor());
        }

        // Apply evidence to the factors
        factors = applyEvidence(factors, evidenceMap);

        // Eliminate hidden variables
        List<String> eliminationOrder = getEliminationOrder(queryVariables, evidenceMap.keySet(), network.getNodes().keySet());
        for (String variable : eliminationOrder) {
            factors = eliminateVariable(factors, variable);
        }

        // Join the remaining factors
        Factor resultFactor = joinFactors(factors);

        // Normalize the result factor
        Factor normalizedResultFactor = resultFactor.normalize();

        // Extract the probability from the result factor
        double probability = extractProbability(normalizedResultFactor, queryVariables, evidenceMap);

        // Return the result in the specified format
        return String.format("%.5f,%d,%d", probability, additionCount, multiplicationCount);
    }

    private List<Factor> applyEvidence(List<Factor> factors, Map<String, String> evidenceMap) {
        List<Factor> updatedFactors = new ArrayList<>();
        for (Factor factor : factors) {
            Factor updatedFactor = factor.restrict(evidenceMap);
            if (updatedFactor != null) {
                updatedFactors.add(updatedFactor);
            }
        }
        return updatedFactors;
    }

    private List<String> getEliminationOrder(String[] queryVariables, Set<String> evidenceVariables, Set<String> networkVariables) {
        List<String> eliminationOrder = new ArrayList<>(networkVariables);
        eliminationOrder.removeAll(Arrays.asList(queryVariables));
        eliminationOrder.removeAll(evidenceVariables);
        return eliminationOrder;
    }

    private List<Factor> eliminateVariable(List<Factor> factors, String variable) {
        List<Factor> updatedFactors = new ArrayList<>();
        List<Factor> factorsToJoin = new ArrayList<>();

        for (Factor factor : factors) {
            if (factor.containsVariable(variable)) {
                factorsToJoin.add(factor);
            } else {
                updatedFactors.add(factor);
            }
        }

        if (!factorsToJoin.isEmpty()) {
            Factor jointFactor = joinFactors(factorsToJoin);
            Factor marginalizedFactor = jointFactor.marginalize(variable);
            additionCount += marginalizedFactor.getCpt().size();
            updatedFactors.add(marginalizedFactor);
        }

        return updatedFactors;
    }

    private Factor joinFactors(List<Factor> factors) {
        Factor resultFactor = factors.get(0);
        for (int i = 1; i < factors.size(); i++) {
            resultFactor = resultFactor.join(factors.get(i));
            multiplicationCount += resultFactor.getCpt().size();
        }
        return resultFactor;
    }

    private double extractProbability(Factor resultFactor, String[] queryVariables, Map<String, String> evidenceMap) {
        List<String> queryAssignment = new ArrayList<>();
        for (String variable : resultFactor.getVariables()) {
            if (Arrays.asList(queryVariables).contains(variable)) {
                String value = evidenceMap.getOrDefault(variable, "T");
                queryAssignment.add(variable + "=" + value);
            }
        }

        for (Map.Entry<List<String>, Double> entry : resultFactor.getCpt().entrySet()) {
            boolean match = true;
            for (String assignment : queryAssignment) {
                if (!entry.getKey().contains(assignment)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return entry.getValue();
            }
        }

        return 0.0;
    }
}