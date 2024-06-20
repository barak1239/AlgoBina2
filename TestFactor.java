import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.*;

public class TestFactor{

    private Factor complexFactor1;
    private Factor complexFactor2;

    @Before
    public void setUp() {
        complexFactor1 = createComplexFactor1();
        complexFactor2 = createComplexFactor2();
    }

    private Factor createComplexFactor1() {
        List<String> variables = Arrays.asList("A", "B", "C", "D", "E");
        Map<List<String>, Double> cpt = new HashMap<>();

        // Example CPT values for a factor with five variables
        cpt.put(Arrays.asList("T", "T", "T", "T", "T"), 0.05);
        cpt.put(Arrays.asList("T", "T", "T", "T", "F"), 0.10);
        cpt.put(Arrays.asList("T", "T", "T", "F", "T"), 0.15);
        cpt.put(Arrays.asList("T", "T", "T", "F", "F"), 0.20);
        cpt.put(Arrays.asList("T", "T", "F", "T", "T"), 0.05);
        cpt.put(Arrays.asList("T", "T", "F", "T", "F"), 0.10);
        cpt.put(Arrays.asList("T", "T", "F", "F", "T"), 0.05);
        cpt.put(Arrays.asList("T", "T", "F", "F", "F"), 0.10);
        cpt.put(Arrays.asList("T", "F", "T", "T", "T"), 0.05);
        cpt.put(Arrays.asList("T", "F", "T", "T", "F"), 0.05);
        cpt.put(Arrays.asList("T", "F", "T", "F", "T"), 0.05);
        cpt.put(Arrays.asList("T", "F", "T", "F", "F"), 0.05);
        cpt.put(Arrays.asList("T", "F", "F", "T", "T"), 0.05);
        cpt.put(Arrays.asList("T", "F", "F", "T", "F"), 0.05);
        cpt.put(Arrays.asList("T", "F", "F", "F", "T"), 0.05);
        cpt.put(Arrays.asList("T", "F", "F", "F", "F"), 0.05);
        cpt.put(Arrays.asList("F", "T", "T", "T", "T"), 0.05);
        cpt.put(Arrays.asList("F", "T", "T", "T", "F"), 0.05);
        cpt.put(Arrays.asList("F", "T", "T", "F", "T"), 0.05);
        cpt.put(Arrays.asList("F", "T", "T", "F", "F"), 0.05);
        cpt.put(Arrays.asList("F", "T", "F", "T", "T"), 0.05);
        cpt.put(Arrays.asList("F", "T", "F", "T", "F"), 0.05);
        cpt.put(Arrays.asList("F", "T", "F", "F", "T"), 0.05);
        cpt.put(Arrays.asList("F", "T", "F", "F", "F"), 0.05);
        cpt.put(Arrays.asList("F", "F", "T", "T", "T"), 0.05);
        cpt.put(Arrays.asList("F", "F", "T", "T", "F"), 0.05);
        cpt.put(Arrays.asList("F", "F", "T", "F", "T"), 0.05);
        cpt.put(Arrays.asList("F", "F", "T", "F", "F"), 0.05);
        cpt.put(Arrays.asList("F", "F", "F", "T", "T"), 0.05);
        cpt.put(Arrays.asList("F", "F", "F", "T", "F"), 0.05);
        cpt.put(Arrays.asList("F", "F", "F", "F", "T"), 0.05);
        cpt.put(Arrays.asList("F", "F", "F", "F", "F"), 0.05);

        return new Factor(variables, cpt);
    }

    private Factor createComplexFactor2() {
        List<String> variables = Arrays.asList("C", "D", "E", "F");
        Map<List<String>, Double> cpt = new HashMap<>();
        cpt.put(Arrays.asList("T", "T", "T", "T"), 0.6);
        cpt.put(Arrays.asList("T", "T", "T", "F"), 0.4);
        cpt.put(Arrays.asList("T", "T", "F", "T"), 0.3);
        cpt.put(Arrays.asList("T", "T", "F", "F"), 0.7);
        cpt.put(Arrays.asList("T", "F", "T", "T"), 0.2);
        cpt.put(Arrays.asList("T", "F", "T", "F"), 0.8);
        cpt.put(Arrays.asList("T", "F", "F", "T"), 0.1);
        cpt.put(Arrays.asList("T", "F", "F", "F"), 0.9);
        cpt.put(Arrays.asList("F", "T", "T", "T"), 0.5);
        cpt.put(Arrays.asList("F", "T", "T", "F"), 0.5);
        cpt.put(Arrays.asList("F", "T", "F", "T"), 0.4);
        cpt.put(Arrays.asList("F", "T", "F", "F"), 0.6);
        cpt.put(Arrays.asList("F", "F", "T", "T"), 0.3);
        cpt.put(Arrays.asList("F", "F", "T", "F"), 0.7);
        cpt.put(Arrays.asList("F", "F", "F", "T"), 0.2);
        cpt.put(Arrays.asList("F", "F", "F", "F"), 0.8);

        return new Factor(variables, cpt);
    }

    @Test
    public void testConstructor() {
        List<String> variables = Arrays.asList("A", "B", "C", "D", "E");
        Map<List<String>, Double> cpt = new HashMap<>();
        cpt.put(Arrays.asList("T", "T", "T", "T", "T"), 0.05);
        cpt.put(Arrays.asList("T", "T", "T", "T", "F"), 0.10);

        Factor factor = new Factor(variables, cpt);

        assertEquals(variables, factor.getVariables());
        assertEquals(cpt, factor.getCpt());
    }

    @Test
    public void testJoin() {
        // Create two factors to join
        Factor result = complexFactor1.join(complexFactor2);
        List<String> expectedVariables = Arrays.asList("A", "B", "C", "D", "E", "F");

        assertEquals(expectedVariables, result.getVariables());
        assertNotNull(result.getCpt());
    }

    @Test
    public void testMarginalize() {
        Factor marginalizedFactor = complexFactor1.marginalize("E");

        List<String> expectedVariables = Arrays.asList("A", "B", "C", "D");
        assertEquals(expectedVariables, marginalizedFactor.getVariables());
        assertNotNull(marginalizedFactor.getCpt());
    }

    @Test
    public void testNormalize() {
        Factor factor = complexFactor1.normalize();

        double sum = 0.0;
        for (double value : factor.getCpt().values()) {
            sum += value;
        }

        assertEquals(1.0, sum, 1e-9); // Allowing a small error margin

    }

    @Test
    public void testUtilityMethods() {
        Factor factor = createComplexFactor1();

        List<String> expectedVariables = Arrays.asList("A", "B", "C", "D", "E");
        assertEquals(expectedVariables, factor.getVariables());

        assertNotNull(factor.getCpt());
        assertNotNull(factor.toString());
    }
}
