import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import java.util.*;

public class Ex1 {
    public static void main(String[] args) {
        try {
            // Read the input file
            BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
            String xmlFileName = reader.readLine();
            List<String> queries = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                queries.add(line);
            }
            reader.close();

            // Parse the XML file
            BayesianNetwork network = new BayesianNetwork(xmlFileName);
            BayesBall bayesBall = new BayesBall(network);
            VariableElimination variableElimination = new VariableElimination(network);

            // Handle the queries
            List<String> results = new ArrayList<>();
            for (String query : queries) {
                if (query.contains("-") && !query.startsWith("P(")) {
                    results.add(bayesBall.run(query));
                } else if (query.startsWith("P(")) {
                    System.out.println("proccessing query: " + query);
                    VariableElimination ve = new VariableElimination(network);
                    String result = ve.run(query);
                    results.add(result);
                } else {
                    results.add("Invalid query format");
                }
            }

            // Write the output file
            BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
            for (String result : results) {
                writer.write(result);
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
