import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Factor {
    private List<String> variables;
    private Map<List<String>, Double> cpt;

    public Factor(List<String> variables, Map<List<String>, Double> cpt) {
        this.variables = variables;
        this.cpt = cpt;
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

            newCpt.merge(newKey, entry.getValue(), Double::sum);
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
        Map<List<String>, Double> newCpt = new HashMap<>();
        for (Map.Entry<List<String>, Double> entry : cpt.entrySet()) {
            newCpt.put(entry.getKey(), entry.getValue() / sum);
        }
        return new Factor(variables, newCpt);
    }

    public List<String> getVariables() {
        return variables;
    }

    public Map<List<String>, Double> getCpt() {
        return cpt;
    }
}