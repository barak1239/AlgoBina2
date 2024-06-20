import java.util.*;

public class Factor {
    private List<String> variables;
    private Map<List<String>, Double> cpt;
    private int additionCount;
    private int multiplicationCount;

    public Factor(List<String> variables, Map<List<String>, Double> cpt) {
        this.variables = variables;
        this.cpt = cpt;
        this.additionCount = 0;
        this.multiplicationCount = 0;
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

    public Factor marginalize(String variable) {
        List<String> newVariables = new ArrayList<>(variables);
        newVariables.remove(variable);

        Map<List<String>, Double> newCpt = new HashMap<>();

        for (Map.Entry<List<String>, Double> entry : cpt.entrySet()) {
            List<String> key = entry.getKey();
            List<String> newKey = new ArrayList<>();

            for (int i = 0; i < variables.size(); i++) {
                if (!variables.get(i).equals(variable)) {
                    newKey.add(key.get(i));
                }
            }

            newCpt.merge(newKey, entry.getValue(), (a, b) -> {
                additionCount++;
                return a + b;
            });
        }

        return new Factor(newVariables, newCpt);
    }

    public Factor join(Factor other) {
        List<String> newVariables = new ArrayList<>(variables);
        for (String variable : other.variables) {
            if (!newVariables.contains(variable)) {
                newVariables.add(variable);
            }
        }

        Map<List<String>, Double> newCpt = new HashMap<>();

        for (Map.Entry<List<String>, Double> entry1 : cpt.entrySet()) {
            for (Map.Entry<List<String>, Double> entry2 : other.cpt.entrySet()) {
                List<String> key1 = entry1.getKey();
                List<String> key2 = entry2.getKey();

                if (isConsistent(key1, key2)) {
                    List<String> newKey = combineKeys(key1, key2, newVariables);
                    newCpt.put(newKey, entry1.getValue() * entry2.getValue());
                    multiplicationCount++;
                }
            }
        }

        return new Factor(newVariables, newCpt);
    }

    private boolean isConsistent(List<String> key1, List<String> key2) {
        Map<String, String> map1 = new HashMap<>();
        for (String assignment : key1) {
            String[] parts = assignment.split("=");
            map1.put(parts[0], parts[1]);
        }

        Map<String, String> map2 = new HashMap<>();
        for (String assignment : key2) {
            String[] parts = assignment.split("=");
            map2.put(parts[0], parts[1]);
        }

        for (String variable : map1.keySet()) {
            if (map2.containsKey(variable) && !map1.get(variable).equals(map2.get(variable))) {
                return false;
            }
        }

        return true;
    }

    private List<String> combineKeys(List<String> key1, List<String> key2, List<String> newVariables) {
        Map<String, String> combinedMap = new HashMap<>();

        for (String assignment : key1) {
            String[] parts = assignment.split("=");
            combinedMap.put(parts[0], parts[1]);
        }

        for (String assignment : key2) {
            String[] parts = assignment.split("=");
            combinedMap.put(parts[0], parts[1]);
        }

        List<String> newKey = new ArrayList<>();
        for (String variable : newVariables) {
            newKey.add(variable + "=" + combinedMap.get(variable));
        }

        return newKey;
    }

    public Factor normalize() {
        double sum = cpt.values().stream().mapToDouble(Double::doubleValue).sum();
        additionCount += cpt.size() - 1;  // count the additions for normalization
        Map<List<String>, Double> newCpt = new HashMap<>();
        for (Map.Entry<List<String>, Double> entry : cpt.entrySet()) {
            newCpt.put(entry.getKey(), entry.getValue() / sum);
        }
        return new Factor(variables, newCpt);
    }
    public Factor sumOut(String variable) {
        int index = variables.indexOf(variable);
        if (index == -1) {
            throw new IllegalArgumentException("Variable not found in factor.");
        }

        List<String> newVariables = new ArrayList<>(variables);
        newVariables.remove(variable);

        Map<List<String>, Double> newCpt = new HashMap<>();
        for (Map.Entry<List<String>, Double> entry : cpt.entrySet()) {
            List<String> key = entry.getKey();
            List<String> newKey = new ArrayList<>(key);
            newKey.remove(index);

            newCpt.put(newKey, newCpt.getOrDefault(newKey, 0.0) + entry.getValue());
            additionCount++;
        }

        return new Factor(newVariables, newCpt);
    }

    public Factor multiply(Factor other) {
        List<String> newVariables = new ArrayList<>(variables);
        for (String var : other.variables) {
            if (!newVariables.contains(var)) {
                newVariables.add(var);
            }
        }

        Map<List<String>, Double> newCpt = new HashMap<>();
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
                    newCpt.put(newKey, newCpt.getOrDefault(newKey, 0.0) + newValue);
                }
            }
        }

        return new Factor(newVariables, newCpt);
    }


    public List<String> getVariables() {
        return variables;
    }

    public Map<List<String>, Double> getCpt() {
        return cpt;
    }

    public int getAdditionCount() {
        return additionCount;
    }

    public int getMultiplicationCount() {
        return multiplicationCount;
    }
}
