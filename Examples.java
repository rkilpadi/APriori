import tester.Tester;
import java.io.IOException;
import java.util.*;

public class Examples {
    String filename = "src/test.csv";
    int uniqueItems = 38;
    APriori ap;

    void initData() throws IOException {
        ap = new APriori(filename, 2);
    }

    void testFirstPass(Tester t) throws IOException {
        initData();
        ap.pass();

        t.checkExpect(ap.itemEncodes.size(), uniqueItems);
    }

    void testGetCandidates(Tester t) throws IOException {
        initData();
        ap.doPriori();

        for (Set<Integer> itemset : ap.allFrequents.keySet()) {

            for (Integer item : itemset) {
                System.out.print(ap.itemDecodes.get(item) + ", ");

            }
            System.out.println(ap.counts.get(ap.allFrequents.get(itemset)));

        }
    }
}
