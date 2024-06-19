import java.util.*;

public class Factor {
    private List<String> variables;
    private Map<List<String>, Double> table;

    public Factor(List<String> variables) {
        this.variables = new ArrayList<>(variables);
        this.table = new HashMap<>();
    }

    public List<String> getVariables() {
        return variables;
    }

    public void setTableEntry(List<String> key, Double value) {
        table.put(key, value);
    }

    public Double getTableEntry(List<String> key) {
        return table.get(key);
    }

    public Map<List<String>, Double> getTable() {
        return table;
    }

    public Factor multiply(Factor other) {
        List<String> newVariables = new ArrayList<>(variables);
        for (String var : other.getVariables()) {
            if (!newVariables.contains(var)) {
                newVariables.add(var);
            }
        }

        Factor result = new Factor(newVariables);
        for (List<String> key1 : table.keySet()) {
            for (List<String> key2 : other.table.keySet()) {
                if (isConsistent(key1, key2)) {
                    List<String> newKey = mergeKeys(key1, key2);
                    Double newValue = table.get(key1) * other.getTableEntry(key2);
                    result.setTableEntry(newKey, newValue);
                }
            }
        }
        return result;
    }

    public Factor sumOut(String variable) {
        List<String> newVariables = new ArrayList<>(variables);
        newVariables.remove(variable);

        Factor result = new Factor(newVariables);
        for (List<String> key : table.keySet()) {
            List<String> newKey = new ArrayList<>(key);
            newKey.remove(variables.indexOf(variable));
            Double newValue = result.getTableEntry(newKey);
            if (newValue == null) {
                newValue = 0.0;
            }
            newValue += table.get(key);
            result.setTableEntry(newKey, newValue);
        }
        return result;
    }

    private boolean isConsistent(List<String> key1, List<String> key2) {
        for (int i = 0; i < variables.size(); i++) {
            String var = variables.get(i);
            if (key1.contains(var) && key2.contains(var)) {
                if (!key1.get(i).equals(key2.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    private List<String> mergeKeys(List<String> key1, List<String> key2) {
        List<String> newKey = new ArrayList<>(key1);
        for (int i = 0; i < key2.size(); i++) {
            if (!newKey.contains(key2.get(i))) {
                newKey.add(key2.get(i));
            }
        }
        return newKey;
    }

    public void print() {
        for (Map.Entry<List<String>, Double> entry : table.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
}
