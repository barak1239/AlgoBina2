import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class BayesianNetwork {
    Map<String, Node> nodes;

    public BayesianNetwork(String xmlFileName) throws ParserConfigurationException, IOException, SAXException {
        nodes = new HashMap<>();
        parseXML(xmlFileName);
        printNetwork();
    }
    public Node getNodeByName(String nodeName) {
        return nodes.get(nodeName);
    }
    private void parseXML(String xmlFileName) throws ParserConfigurationException, IOException, SAXException {
        File inputFile = new File(xmlFileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();

        NodeList variableList = doc.getElementsByTagName("VARIABLE");
        for (int i = 0; i < variableList.getLength(); i++) {
            Element variableElement = (Element) variableList.item(i);
            String name = variableElement.getElementsByTagName("NAME").item(0).getTextContent();
            Node node = new Node(name);

            NodeList outcomeList = variableElement.getElementsByTagName("OUTCOME");
            for (int j = 0; j < outcomeList.getLength(); j++) {
                node.addOutcome(outcomeList.item(j).getTextContent());
            }
            nodes.put(name, node);
        }

        NodeList definitionList = doc.getElementsByTagName("DEFINITION");
        for (int i = 0; i < definitionList.getLength(); i++) {
            Element definitionElement = (Element) definitionList.item(i);
            String nodeName = definitionElement.getElementsByTagName("FOR").item(0).getTextContent();
            Node node = nodes.get(nodeName);

            NodeList givenList = definitionElement.getElementsByTagName("GIVEN");
            List<String> parents = new ArrayList<>();
            for (int j = 0; j < givenList.getLength(); j++) {
                String parentName = givenList.item(j).getTextContent();
                Node parentNode = nodes.get(parentName);
                node.addParent(parentNode);
                parentNode.addChild(node);
                parents.add(parentName);
            }

            String[] table = definitionElement.getElementsByTagName("TABLE").item(0).getTextContent().trim().split(" ");
            node.setCPT(parents, table);
        }
    }

    private void printNetwork() {
        System.out.println("Bayesian Network Structure:");

        List<Node> topologicallySortedNodes = topologicalSort();
        for (Node node : topologicallySortedNodes) {
            System.out.println("Node: " + node.getName());
            System.out.println("Dependencies:");
            System.out.println("  Parents: " + node.getParentsNames());
            System.out.println("  Children: " + node.getChildrenNames());
            System.out.println("CPT:");
            for (Map.Entry<List<String>, Double> entry : node.getCPT().entrySet()) {
                System.out.print("  Outcomes: ");
                boolean first = true;
                for (String outcome : entry.getKey()) {
                    if (first) {
                        first = false;
                    } else {
                        System.out.print(", ");
                    }
                    System.out.print(outcome);
                }
                System.out.printf(" -> Probability: %.3f", entry.getValue());
                System.out.println();
            }
            System.out.println();
        }
    }

    List<Node> topologicalSort() {
        List<Node> sortedNodes = new ArrayList<>();
        Set<Node> visited = new HashSet<>();
        Set<Node> visiting = new HashSet<>();

        for (Node node : nodes.values()) {
            if (!visited.contains(node)) {
                topologicalSortUtil(node, visited, visiting, sortedNodes);
            }
        }

        Collections.reverse(sortedNodes);
        return sortedNodes;
    }

    private void topologicalSortUtil(Node node, Set<Node> visited, Set<Node> visiting, List<Node> sortedNodes) {
        visiting.add(node);

        for (Node child : node.getChildren()) {
            if (!visited.contains(child)) {
                if (visiting.contains(child)) {
                    throw new IllegalStateException("The network contains a cycle.");
                }
                topologicalSortUtil(child, visited, visiting, sortedNodes);
            }
        }

        visiting.remove(node);
        visited.add(node);
        sortedNodes.add(node);
    }
    public Map<String, Node> getNodes() {
        return nodes;
    }

}
class Node {
    private String name;
    private List<String> outcomes;
    private List<Node> parents;
    private List<Node> children;
    private Map<List<String>, Double> cpt;
    public Node(String name) {
            this.name = name;
            this.outcomes = new ArrayList<>();
            this.parents = new ArrayList<>();
            this.children = new ArrayList<>();
            this.cpt = new HashMap<>();
    }
    public String getName() {
            return name;
    }

    public List<String> getOutcomes() {
        return outcomes;
    }
    public void addOutcome(String outcome) {
        outcomes.add(outcome);
    }

    public List<Node> getParents() {
        return parents;
    }

    public List<String> getParentsNames() {
        List<String> names = new ArrayList<>();
        for (Node parent : parents) {
            names.add(parent.getName());
        }
        return names;
    }

    public void addParent(Node parent) {
        parents.add(parent);
    }
    public List<Node> getChildren() {
        return children;
    }
    public List<String> getChildrenNames() {
        List<String> names = new ArrayList<>();
        for (Node child : children) {
            names.add(child.getName());
        }
        return names;
    }
    public List<String> getVariables() {
        return outcomes;
    }
    public void addChild(Node child) {
        children.add(child);
    }

    public Map<List<String>, Double> getCPT() {
        return cpt;
    }

    public void setCPT(List<String> parents, String[] table) {
        int numParents = parents.size();
        int numOutcomes = (int) Math.pow(2, numParents); // Correct number of combinations for the parents' outcomes
        for (int i = 0; i < numOutcomes; i++) {
            List<String> keyT = new ArrayList<>();
            List<String> keyF = new ArrayList<>();
            for (int j = 0; j < numParents; j++) {
                int outcomeIndex = (i / (int) Math.pow(2, numParents - 1 - j)) % 2; // Iterate correctly through combinations
                keyT.add(parents.get(j) + "=" + (outcomeIndex == 0 ? "T" : "F"));
                keyF.add(parents.get(j) + "=" + (outcomeIndex == 0 ? "T" : "F"));
            }
            keyT.add(name + "=T");
            keyF.add(name + "=F");
            double trueProbability = Double.parseDouble(table[2 * i]);
            double falseProbability = Double.parseDouble(table[2 * i + 1]);
            cpt.put(keyT, trueProbability);
            cpt.put(keyF, falseProbability);
        }
    }
    public Factor toFactor() {
        List<String> factorVariables = new ArrayList<>(parents.size() + 1);
        for (Node parent : parents) {
            factorVariables.add(parent.getName());
        }
        factorVariables.add(name);

        return new Factor(factorVariables, cpt);
    }
        // Other necessary methods
    }


