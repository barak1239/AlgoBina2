import java.util.*;
import java.util.stream.Collectors;

public class Factor {
    private List<String> variables;
    private Map<List<String>, Double> cpt;

    public Factor(List<String> variables, Map<List<String>, Double> cpt) {
        this.variables = variables;
        this.cpt = cpt;
    }

    public Factor(Node node, Map<String, String> evidence) {
        this.variables = new ArrayList<>(node.getParents().stream().map(Node::getName).collect(Collectors.toList()));
        this.variables.add(node.getName());
        this.cpt = new HashMap<>();

        for (Map.Entry<List<String>, Double> entry : node.getCPT().entrySet()) {
            List<String> assignment = new ArrayList<>(entry.getKey());
            boolean consistent = true;
            for (Node parent : node.getParents()) {
                String parentValue = evidence.get(parent.getName());
                if (parentValue != null && !assignment.get(variables.indexOf(parent.getName())).equals(parent.getName() + "=" + parentValue)) {
                    consistent = false;
                    break;
                }
            }
            if (consistent) {
                if (evidence.get(node.getName()) != null) {
                    if (assignment.get(variables.indexOf(node.getName())).equals(node.getName() + "=" + evidence.get(node.getName()))) {
                        this.cpt.put(assignment, entry.getValue());
                    }
                } else {
                    this.cpt.put(assignment, entry.getValue());
                }
            }
        }
    }

    public Factor restrict(Map<String, String> evidenceMap) {
        Map<List<String>, Double> newCpt = new HashMap<>();

        for (Map.Entry<List<String>, Double> entry : cpt.entrySet()) {
            List<String> key = entry.getKey();
            boolean isConsistent = true;

            for (int i = 0; i < variables.size(); i++) {
                String variable = variables.get(i);
                if (evidenceMap.containsKey(variable)) {
                    String evidenceValue = evidenceMap.get(variable);
                    if (!key.get(i).equals(variable + "=" + evidenceValue)) {
                        isConsistent = false;
                        break;
                    }
                }
            }

            if (isConsistent) {
                List<String> newKey = new ArrayList<>();
                for (int i = 0; i < variables.size(); i++) {
                    String variable = variables.get(i);
                    if (!evidenceMap.containsKey(variable)) {
                        newKey.add(key.get(i));
                    }
                }
                newCpt.put(newKey, entry.getValue());
            }
        }

        if (newCpt.isEmpty()) {
            return null;
        }

        List<String> newVariables = new ArrayList<>();
        for (String variable : variables) {
            if (!evidenceMap.containsKey(variable)) {
                newVariables.add(variable);
            }
        }

        return new Factor(newVariables, newCpt);
    }

    public boolean containsVariable(String variable) {
        return variables.contains(variable);
    }

    public int[] sumOut(String variable) {
        int index = variables.indexOf(variable);
        if (index == -1) {
            throw new IllegalArgumentException("Variable not found in factor.");
        }

        List<String> newVariables = new ArrayList<>(variables);
        newVariables.remove(variable);

        Map<List<String>, Double> newCpt = new HashMap<>();
        int additionCount = 0;

        for (Map.Entry<List<String>, Double> entry : cpt.entrySet()) {
            List<String> key = entry.getKey();
            List<String> newKey = new ArrayList<>(key);
            newKey.remove(index);

            double value = entry.getValue();
            if (newCpt.containsKey(newKey)) {
                value += newCpt.get(newKey);
                additionCount++;
            }
            newCpt.put(newKey, value);
        }

        this.variables = newVariables;
        this.cpt = newCpt;
        return new int[]{additionCount, 0};
    }

    public int[] multiply(Factor other) {
        List<String> newVariables = new ArrayList<>(variables);
        for (String var : other.variables) {
            if (!newVariables.contains(var)) {
                newVariables.add(var);
            }
        }

        Map<List<String>, Double> newCpt = new HashMap<>();
        int multiplicationCount = 0;

        for (Map.Entry<List<String>, Double> entry1 : cpt.entrySet()) {
            for (Map.Entry<List<String>, Double> entry2 : other.cpt.entrySet()) {
                boolean match = true;
                List<String> newKey = new ArrayList<>(Collections.nCopies(newVariables.size(), ""));
                for (int i = 0; i < variables.size(); i++) {
                    String value1 = entry1.getKey().get(i);
                    newKey.set(newVariables.indexOf(variables.get(i)), value1);
                }
                for (int i = 0; i < other.variables.size(); i++) {
                    String value2 = entry2.getKey().get(i);
                    int index = newVariables.indexOf(other.variables.get(i));
                    if (newKey.get(index).isEmpty()) {
                        newKey.set(index, value2);
                    } else if (!newKey.get(index).equals(value2)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    double newValue = entry1.getValue() * entry2.getValue();
                    multiplicationCount++;
                    newCpt.put(newKey, newValue);
                }
            }
        }

        this.variables = newVariables;
        this.cpt = newCpt;
        return new int[]{0, multiplicationCount};
    }

    public int[] normalize() {
        double sum = cpt.values().stream().mapToDouble(Double::doubleValue).sum();
        int additionCount = cpt.size() - 1;

        Map<List<String>, Double> newCpt = new HashMap<>();
        for (Map.Entry<List<String>, Double> entry : cpt.entrySet()) {
            newCpt.put(entry.getKey(), entry.getValue() / sum);
        }

        this.cpt = newCpt;
        return new int[]{additionCount, 0};
    }

    public List<String> getVariables() {
        return variables;
    }

    public Map<List<String>, Double> getCpt() {
        return cpt;
    }
}