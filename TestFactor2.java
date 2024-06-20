import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestFactor2 {
    private Factor factorH;
    private Factor factorG;

    @BeforeEach
    public void setUp() {
        // Creating Factor A
        List<String> variablesA = Arrays.asList("A", "B");
        Map<List<String>, Double> cptA = new HashMap<>();
        cptA.put(Arrays.asList("A=T", "B=T"), 0.1);
        cptA.put(Arrays.asList("A=T", "B=F"), 0.9);
        cptA.put(Arrays.asList("A=F", "B=T"), 0.4);
        cptA.put(Arrays.asList("A=F", "B=F"), 0.6);
        Factor factorA = new Factor(variablesA, cptA);

        // Creating Factor B
        List<String> variablesB = Arrays.asList("B", "C");
        Map<List<String>, Double> cptB = new HashMap<>();
        cptB.put(Arrays.asList("B=T", "C=T"), 0.3);
        cptB.put(Arrays.asList("B=T", "C=F"), 0.7);
        cptB.put(Arrays.asList("B=F", "C=T"), 0.5);
        cptB.put(Arrays.asList("B=F", "C=F"), 0.5);
        Factor factorB = new Factor(variablesB, cptB);

        // Factor H is the result of multiplying A and B
        factorH = factorA.multiply(factorB);

        // Creating Factor C
        List<String> variablesC = Arrays.asList("C", "D");
        Map<List<String>, Double> cptC = new HashMap<>();
        cptC.put(Arrays.asList("C=T", "D=T"), 0.2);
        cptC.put(Arrays.asList("C=T", "D=F"), 0.8);
        cptC.put(Arrays.asList("C=F", "D=T"), 0.6);
        cptC.put(Arrays.asList("C=F", "D=F"), 0.4);
        Factor factorC = new Factor(variablesC, cptC);

        // Creating Factor D
        List<String> variablesD = Arrays.asList("D", "E");
        Map<List<String>, Double> cptD = new HashMap<>();
        cptD.put(Arrays.asList("D=T", "E=T"), 0.5);
        cptD.put(Arrays.asList("D=T", "E=F"), 0.5);
        cptD.put(Arrays.asList("D=F", "E=T"), 0.4);
        cptD.put(Arrays.asList("D=F", "E=F"), 0.6);
        Factor factorD = new Factor(variablesD, cptD);

        // Factor G is the result of multiplying C and D
        factorG = factorC.multiply(factorD);
    }

    @Test
    public void testFactorH() {
        assertNotNull(factorH);
        assertEquals(3, factorH.getVariables().size());

        // Test other methods on factorH
        testMethods(factorH);
    }

    @Test
    public void testFactorG() {
        assertNotNull(factorG);
        assertEquals(3, factorG.getVariables().size());

        // Test other methods on factorG
        testMethods(factorG);
    }

    private void testMethods(Factor factor) {
        // Test restrict
        Map<String, String> evidence = new HashMap<>();
        evidence.put(factor.getVariables().get(0), "T");
        Factor restrictedFactor = factor.restrict(evidence);
        assertNotNull(restrictedFactor);

        // Test containsVariable
        assertTrue(factor.containsVariable(factor.getVariables().get(0)));

        // Test sumOut
        Factor summedOutFactor = factor.sumOut(factor.getVariables().get(0));
        assertNotNull(summedOutFactor);

        // Test multiply
        Factor multipliedFactor = factor.multiply(factor);
        assertNotNull(multipliedFactor);

        // Test normalize
        Factor normalizedFactor = factor.normalize();
        assertNotNull(normalizedFactor);
        double sum = normalizedFactor.getCpt().values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(1.0, sum, 1e-6);
    }
}
