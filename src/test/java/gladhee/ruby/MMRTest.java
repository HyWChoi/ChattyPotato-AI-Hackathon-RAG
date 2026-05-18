package gladhee.ruby;

import gladhee.ruby.util.MMR;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MMRTest {
    @Test
    void picks_diverse_by_relevance_when_no_sim() {
        double[] rel = {0.9, 0.8, 0.7, 0.1};
        List<Integer> order = MMR.select(3, 0.75, rel, null);
        assertEquals(3, order.size());
        assertEquals(0, order.get(0)); // best first
    }

    @Test
    void penalizes_similarity_when_sim_provided() {
        double[] rel = {0.9, 0.85, 0.8};
        double[][] sim = {
                {1.0, 0.9, 0.1},
                {0.9, 1.0, 0.2},
                {0.1, 0.2, 1.0}
        };
        List<Integer> order = MMR.select(2, 0.5, rel, sim);
        assertEquals(0, order.get(0));
        assertTrue(order.get(1)==2); // pick diverse doc 2 over near-duplicate 1
    }
}
