import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.*;

public class TestFactor2 {

    private Factor complexFactor1;
    private Factor complexFactor2;
    private Factor complexFactor3;
    private Factor complexFactor4;

    @Before
    public void setUp() {
        complexFactor1 = createComplexFactor1();
        complexFactor2 = createComplexFactor2();
        complexFactor3 = createComplexFactor3();
        complexFactor4 = createComplexFactor4();
    }

    private Factor createComplexFactor1() {
        List<String> variables = Arrays.asList("A", "B", "C");
        Map<List<String>, Double> cpt = new HashMap<>();
        cpt.put(Arrays.asList("T", "T", "T"), 0.1);
        cpt.put(Arrays.asList("T", "T", "F"), 0.2);
        cpt.put(Arrays.asList("T", "F", "T"), 0.3);
        cpt.put(Arrays.asList("T", "F", "F"), 0.4);
        cpt.put(Arrays.asList("F", "T", "T"), 0.1);
        cpt.put(Arrays.asList("F", "T", "F"), 0.2);
        cpt.put(Arrays.asList("F", "F", "T"), 0.3);
        cpt.put(Arrays.asList("F", "F", "F"), 0.4);

        return new Factor(variables, cpt);
    }

    private Factor createComplexFactor2() {
        List<String> variables = Arrays.asList("C", "D");
        Map<List<String>, Double> cpt = new HashMap<>();
        cpt.put(Arrays.asList("T", "T"), 0.4);
        cpt.put(Arrays.asList("T", "F"), 0.6);
        cpt.put(Arrays.asList("F", "T"), 0.7);
        cpt.put(Arrays.asList("F", "F"), 0.3);

        return new Factor(variables, cpt);
    }

    private Factor createComplexFactor3() {
        List<String> variables = Arrays.asList("D", "E");
        Map<List<String>, Double> cpt = new HashMap<>();
        cpt.put(Arrays.asList("T", "T"), 0.5);
        cpt.put(Arrays.asList("T", "F"), 0.5);
        cpt.put(Arrays.asList("F", "T"), 0.8);
        cpt.put(Arrays.asList("F", "F"), 0.2);

        return new Factor(variables, cpt);
    }

    private Factor createComplexFactor4() {
        List<String> variables = Arrays.asList("E", "F");
        Map<List<String>, Double> cpt = new HashMap<>();
        cpt.put(Arrays.asList("T", "T"), 0.6);
        cpt.put(Arrays.asList("T", "F"), 0.4);
        cpt.put(Arrays.asList("F", "T"), 0.9);
        cpt.put(Arrays.asList("F", "F"), 0.1);

        return new Factor(variables, cpt);
    }

    @Test
    public void testConstructor() {
        List<String> variables = Arrays.asList("A", "B", "C");
        Map<List<String>, Double> cpt = new HashMap<>();
        cpt.put(Arrays.asList("T", "T", "T"), 0.1);
        cpt.put(Arrays.asList("T", "T", "F"), 0.2);

        Factor factor = new Factor(variables, cpt);

        assertEquals(variables, factor.getVariables());
        assertEquals(cpt, factor.getCpt());
    }

    @Test
    public void testJoin() {
        // Create two factors to join
        Factor result = complexFactor1.join(complexFactor2);
        List<String> expectedVariables = Arrays.asList("A", "B", "C", "D");

        assertEquals(expectedVariables, result.getVariables());
        assertNotNull(result.getCpt());
    }

    @Test
    public void testMarginalize() {
        Factor marginalizedFactor = complexFactor1.marginalize("C");

        List<String> expectedVariables = Arrays.asList("A", "B");
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

        List<String> expectedVariables = Arrays.asList("A", "B", "C");
        assertEquals(expectedVariables, factor.getVariables());

        assertNotNull(factor.getCpt());
        assertNotNull(factor.toString());
    }

    @Test
    public void testJoinFactorsG() {
        Factor factorG = complexFactor1.join(complexFactor2);
        Factor resultJoinG = factorG.join(complexFactor3);
        System.out.println("Joined G Variables: " + resultJoinG.getVariables());
        System.out.println("Joined G CPT: " + resultJoinG.getCpt());
        assertNotNull(resultJoinG.getCpt());
        List<String> expectedVariablesG = Arrays.asList("A", "B", "C", "D", "E");
        assertEquals(expectedVariablesG, resultJoinG.getVariables());
    }

    @Test
    public void testJoinFactorsH() {
        Factor factorH = complexFactor3.join(complexFactor4);
        Factor resultJoinH = factorH.join(complexFactor1);
        System.out.println("Joined H Variables: " + resultJoinH.getVariables());
        System.out.println("Joined H CPT: " + resultJoinH.getCpt());
        assertNotNull(resultJoinH.getCpt());
        List<String> expectedVariablesH = Arrays.asList("D", "E", "F", "A", "B", "C");
        assertEquals(expectedVariablesH, resultJoinH.getVariables());
    }

    @Test
    public void testMarginalizeFactorsG() {
        Factor factorG = complexFactor1.join(complexFactor2).join(complexFactor3);
        Factor marginalizedG = factorG.marginalize("D");
        List<String> expectedMarginalizedVariablesG = Arrays.asList("A", "B", "C", "E");
        System.out.println("Actual Marginalized Variables G: " + marginalizedG.getVariables());
        System.out.println("Actual Marginalized CPT G: " + marginalizedG.getCpt());
        assertEquals(expectedMarginalizedVariablesG, marginalizedG.getVariables());
        assertNotNull(marginalizedG.getCpt());
    }

    @Test
    public void testMarginalizeFactorsH() {
        Factor factorH = complexFactor3.join(complexFactor4).join(complexFactor1);
        Factor marginalizedH = factorH.marginalize("F");
        List<String> expectedMarginalizedVariablesH = Arrays.asList("D", "E", "A", "B", "C");
        System.out.println("Actual Marginalized Variables H: " + marginalizedH.getVariables());
        System.out.println("Actual Marginalized CPT H: " + marginalizedH.getCpt());
        assertEquals(expectedMarginalizedVariablesH, marginalizedH.getVariables());
        assertNotNull(marginalizedH.getCpt());
    }

    @Test
    public void testNormalizeFactorsG() {
        Factor factorG = complexFactor1.join(complexFactor2).join(complexFactor3).marginalize("D");
        Factor normalizedG = factorG.normalize();
        double sumG = 0.0;
        for (double value : normalizedG.getCpt().values()) {
            sumG += value;
        }
        assertEquals(1.0, sumG, 1e-9); // Allowing a small error margin
    }

    @Test
    public void testNormalizeFactorsH() {
        Factor factorH = complexFactor3.join(complexFactor4).join(complexFactor1).marginalize("F");
        Factor normalizedH = factorH.normalize();
        double sumH = 0.0;
        for (double value : normalizedH.getCpt().values()) {
            sumH += value;
        }
        assertEquals(1.0, sumH, 1e-9); // Allowing a small error margin
    }
}
