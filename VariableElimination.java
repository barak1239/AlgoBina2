import java.util.*;

public class VariableElimination {
    private BayesianNetwork network;
    private int additionCount;
    private int multiplicationCount;

    public VariableElimination(String xmlFileName) throws Exception {
        network = new BayesianNetwork(xmlFileName);
    }

    public String run(String query) {
        additionCount = 0;
        multiplicationCount = 0;

        try {
            System.out.println("Running query: " + query);

            int indexOfClosingParen = query.indexOf(')');
            if (indexOfClosingParen == -1) {
                return "Invalid query format, comes from VariableElimination";
            }

            String queryPart = query.substring(0, indexOfClosingParen + 1);
            String hiddenVarsPart = query.substring(indexOfClosingParen + 1).trim();

            System.out.println("Query part: " + queryPart);
            System.out.println("Hidden variables part: " + hiddenVarsPart);

            if (!queryPart.startsWith("P(") || !queryPart.contains("|")) {
                return "Invalid query format, comes from VariableElimination";
            }

            String[] querySplit = queryPart.substring(2, queryPart.length() - 1).split("\\|");
            if (querySplit.length != 2) {
                return "Invalid query format, comes from VariableElimination";
            }

            String[] queryVarParts = querySplit[0].trim().split("=");
            if (queryVarParts.length != 2) {
                return "Invalid query format, comes from VariableElimination";
            }

            String queryVar = queryVarParts[0].trim();
            String queryVal = queryVarParts[1].trim();

            System.out.println("Query variable: " + queryVar);
            System.out.println("Query value: " + queryVal);

            String[] evidenceParts = querySplit[1].trim().split("\\s*,\\s*");
            Map<String, String> evidence = new HashMap<>();
            for (String evidencePart : evidenceParts) {
                String[] ev = evidencePart.split("=");
                if (ev.length != 2) {
                    return "Invalid query format, comes from VariableElimination";
                }
                evidence.put(ev[0].trim(), ev[1].trim());
            }

            System.out.println("Evidence: " + evidence);

            String[] eliminationOrder = hiddenVarsPart.trim().split("\\s*-\\s*");
            List<String> eliminationList = Arrays.asList(eliminationOrder);

            System.out.println("Elimination order: " + eliminationList);

            double result = variableElimination(queryVar, queryVal, evidence, eliminationList);
            return String.format("%.5f,%d,%d", result, additionCount, multiplicationCount);
        } catch (Exception e) {
            e.printStackTrace();
            return "Invalid query format, comes from VariableElimination";
        }
    }


    private double variableElimination(String queryVar, String queryVal, Map<String, String> evidence, List<String> eliminationOrder) {
        System.out.println("Starting variable elimination...");
        System.out.println("Query variable: " + queryVar);
        System.out.println("Query value: " + queryVal);
        System.out.println("Evidence: " + evidence);
        System.out.println("Elimination order: " + eliminationOrder);

        List<Factor> factors = createFactors();
        System.out.println("Initial factors:");
        for (Factor factor : factors) {
            factor.print();
        }

        // Apply evidence to factors
        for (Map.Entry<String, String> entry : evidence.entrySet()) {
            factors = restrictFactors(factors, entry.getKey(), entry.getValue());
        }
        System.out.println("Factors after applying evidence:");
        for (Factor factor : factors) {
            factor.print();
        }

        // Eliminate variables
        for (String var : eliminationOrder) {
            factors = sumOutVariable(factors, var);
            System.out.println("Factors after eliminating " + var + ":");
            for (Factor factor : factors) {
                factor.print();
            }
        }

        // Multiply remaining factors
        Factor resultFactor = factors.get(0);
        for (int i = 1; i < factors.size(); i++) {
            resultFactor = resultFactor.multiply(factors.get(i));
            multiplicationCount++;
        }
        System.out.println("Final factor:");
        resultFactor.print();

        // Normalize
        double normalizationFactor = 0.0;
        for (Double value : resultFactor.getTable().values()) {
            normalizationFactor += value;
            additionCount++;
        }

        List<String> queryKey = new ArrayList<>();
        for (String var : resultFactor.getVariables()) {
            if (var.equals(queryVar)) {
                queryKey.add(queryVar + "=" + queryVal);
            } else if (evidence.containsKey(var)) {
                queryKey.add(var + "=" + evidence.get(var));
            }
        }

        Double probValue = resultFactor.getTableEntry(queryKey);
        if (probValue == null) {
            throw new IllegalArgumentException("Invalid query key: " + queryKey);
        }

        double result = probValue / normalizationFactor;
        return result;
    }
    private List<Factor> createFactors() {
        List<Factor> factors = new ArrayList<>();
        for (Node node : network.nodes.values()) { // Directly access the nodes map
            List<String> variables = new ArrayList<>(node.getParentsNames());
            variables.add(node.getName());

            Factor factor = new Factor(variables);
            Map<List<String>, Double> cpt = node.getCPT(); // Access CPT entries
            for (Map.Entry<List<String>, Double> cptEntry : cpt.entrySet()) {
                factor.setTableEntry(cptEntry.getKey(), cptEntry.getValue());
            }

            factors.add(factor);
        }
        return factors;
    }

    private List<Factor> restrictFactors(List<Factor> factors, String var, String val) {
        List<Factor> newFactors = new ArrayList<>();
        for (Factor factor : factors) {
            if (factor.getVariables().contains(var)) {
                Factor restrictedFactor = restrictFactor(factor, var, val);
                newFactors.add(restrictedFactor);
            } else {
                newFactors.add(factor);
            }
        }
        return newFactors;
    }

    private Factor restrictFactor(Factor factor, String var, String val) {
        List<String> newVariables = new ArrayList<>(factor.getVariables());
        newVariables.remove(var);

        Factor restrictedFactor = new Factor(newVariables);
        for (Map.Entry<List<String>, Double> entry : factor.getTable().entrySet()) {
            if (entry.getKey().contains(var + "=" + val)) {
                List<String> newKey = new ArrayList<>(entry.getKey());
                newKey.remove(var + "=" + val);
                restrictedFactor.setTableEntry(newKey, entry.getValue());
            }
        }
        return restrictedFactor;
    }

    private List<Factor> sumOutVariable(List<Factor> factors, String var) {
        List<Factor> newFactors = new ArrayList<>();
        List<Factor> factorsToMultiply = new ArrayList<>();

        for (Factor factor : factors) {
            if (factor.getVariables().contains(var)) {
                factorsToMultiply.add(factor);
            } else {
                newFactors.add(factor);
            }
        }

        Factor productFactor = factorsToMultiply.get(0);
        for (int i = 1; i < factorsToMultiply.size(); i++) {
            productFactor = productFactor.multiply(factorsToMultiply.get(i));
            multiplicationCount++;
        }

        Factor summedOutFactor = productFactor.sumOut(var);
        additionCount++;

        newFactors.add(summedOutFactor);
        return newFactors;
    }
}
