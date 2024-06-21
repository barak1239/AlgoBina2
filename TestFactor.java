/*import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

public class TestFactor {
    private Factor factor;

    @BeforeEach
    public void setUp() {
        List<String> variables = Arrays.asList("A", "B", "C", "D");
        Map<List<String>, Double> cpt = new HashMap<>();
        cpt.put(Arrays.asList("A=T", "B=T", "C=T", "D=T"), 0.1);
        cpt.put(Arrays.asList("A=T", "B=T", "C=T", "D=F"), 0.2);
        cpt.put(Arrays.asList("A=T", "B=T", "C=F", "D=T"), 0.3);
        cpt.put(Arrays.asList("A=T", "B=T", "C=F", "D=F"), 0.4);
        cpt.put(Arrays.asList("A=T", "B=F", "C=T", "D=T"), 0.5);
        cpt.put(Arrays.asList("A=T", "B=F", "C=T", "D=F"), 0.6);
        cpt.put(Arrays.asList("A=T", "B=F", "C=F", "D=T"), 0.7);
        cpt.put(Arrays.asList("A=T", "B=F", "C=F", "D=F"), 0.8);
        cpt.put(Arrays.asList("A=F", "B=T", "C=T", "D=T"), 0.9);
        cpt.put(Arrays.asList("A=F", "B=T", "C=T", "D=F"), 0.05);
        cpt.put(Arrays.asList("A=F", "B=T", "C=F", "D=T"), 0.15);
        cpt.put(Arrays.asList("A=F", "B=T", "C=F", "D=F"), 0.25);
        cpt.put(Arrays.asList("A=F", "B=F", "C=T", "D=T"), 0.35);
        cpt.put(Arrays.asList("A=F", "B=F", "C=T", "D=F"), 0.45);
        cpt.put(Arrays.asList("A=F", "B=F", "C=F", "D=T"), 0.55);
        cpt.put(Arrays.asList("A=F", "B=F", "C=F", "D=F"), 0.65);
        factor = new Factor(variables, cpt);
    }

    @Test
    public void testRestrict() {
        Map<String, String> evidence = new HashMap<>();
        evidence.put("A", "T");
        Factor restrictedFactor = factor.restrict(evidence);
        assertNotNull(restrictedFactor);
        assertEquals(3, restrictedFactor.getVariables().size());
        assertEquals(Arrays.asList("B", "C", "D"), restrictedFactor.getVariables());
    }

    @Test
    public void testContainsVariable() {
        assertTrue(factor.containsVariable("A"));
        assertFalse(factor.containsVariable("E"));
    }

   @Test
    public void testSumOut() {
    int[] result = factor.sumOut("A");
    assertNotNull(result);
    assertEquals(3, factor.getVariables().size());
    assertEquals(Arrays.asList("B", "C", "D"), factor.getVariables());
    }

    @Test
    public void testMultiply() {
    List<String> variables2 = Arrays.asList("C", "D", "E");
    Map<List<String>, Double> cpt2 = new HashMap<>();
    cpt2.put(Arrays.asList("C=T", "D=T", "E=T"), 0.1);
    cpt2.put(Arrays.asList("C=T", "D=T", "E=F"), 0.2);
    cpt2.put(Arrays.asList("C=T", "D=F", "E=T"), 0.3);
    cpt2.put(Arrays.asList("C=T", "D=F", "E=F"), 0.4);
    cpt2.put(Arrays.asList("C=F", "D=T", "E=T"), 0.5);
    cpt2.put(Arrays.asList("C=F", "D=T", "E=F"), 0.6);
    cpt2.put(Arrays.asList("C=F", "D=F", "E=T"), 0.7);
    cpt2.put(Arrays.asList("C=F", "D=F", "E=F"), 0.8);

    Factor factor2 = new Factor(variables2, cpt2);
    int[] result = factor.multiply(factor2);
    assertNotNull(result);
    assertEquals(5, factor.getVariables().size());
    }

@Test
    public void testNormalize() {
    int[] result = factor.normalize();
    assertNotNull(result);
    double sum = factor.getCpt().values().stream().mapToDouble(Double::doubleValue).sum();
    assertEquals(1.0, sum, 1e-6);
    }
}
*/