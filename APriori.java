import java.io.*;
import java.util.*;

public class APriori {

    String filename;
    // frequency threshold (support)
    int s;
    int totalBaskets;
    Set<Integer> basket;

    // translates item names to integers from 1 to n
    Map<String, Integer> itemEncodes;
    Map<Integer, String> itemDecodes;
    // triples structure
    Map<Set<Integer>, Integer> candidates;
    // translates itemsets to integers from 1 to m
    Map<Set<Integer>, Integer> currentFrequents;
    Map<Set<Integer>, Integer> allFrequents;

    // stores the count of each integer (representing an itemset)
    //Map<Integer, Integer> itemCounts;
    // counts of pairs (codes to counts)
    Map<Integer, Integer> counts;

    APriori(String filename, int s) {
        this.itemEncodes = new HashMap<>();
        this.itemDecodes = new HashMap<>();
        this.currentFrequents = new HashMap<>();
        this.allFrequents = new HashMap<>();
        this.candidates = new HashMap<>();
        this.counts = new HashMap<>();
        this.basket = new HashSet<>();

        this.totalBaskets = 0;
        this.s = s;
        this.filename = filename;
    }

    void doPriori() throws IOException {
        pass();
        findFrequentsFromCandidates();
        pass2();
    }

    Integer encodeItem(String item) {
        return itemEncodes.get(item);
    }

    // FIRST PASS
    // parses a csv file into a list of lists
    // new list after every line, new element after every comma (blanks excluded)
    // creates two tables: one translates item names to integers, the other does integers to counts
    void pass() throws IOException {
        Scanner fileScanner = new Scanner(new File(filename));

        int n = 0;
        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();

            Scanner lineScanner = new Scanner(line);
            lineScanner.useDelimiter("\\s*,\\s*");

            while (lineScanner.hasNext()) {
                String item = lineScanner.next();
                if (!item.equals("")) {
                    if (itemEncodes.containsKey(item)) {
                        updateCount(Collections.singleton(encodeItem(item)));
                    } else {
                        addItem(item, ++n);
                    }
                }
            }
            totalBaskets++;
            lineScanner.close();
        }
        fileScanner.close();
    }

    // FIRST PASS ONLY
    void addItem(String item, int n) {
        Set<Integer> toAdd = new HashSet<>(Collections.singleton(n));
            candidates.put(toAdd, n);
            itemEncodes.put(item, n);
            itemDecodes.put(n, item);
            counts.put(n, 1);
    }

    void updateCount(Set<Integer> item) {
        counts.put(candidates.get(item), counts.get(candidates.get(item)) + 1);
    }


// gets candidates of size k (not sure if this is the best way to do this)
    void getCandidates(int k) {
        int n = allFrequents.size()+1;
        candidates.clear();
        for (Set<Integer> item1 : allFrequents.keySet()) {
            for (Set<Integer> item2 : allFrequents.keySet()) {
                Set<Integer> candidate = new HashSet<>();
                candidate.addAll(item1);
                candidate.addAll(item2);

                boolean uniqueAndRightSize = (item1 != item2) && (item1.size() + item2.size() == k);
                if (uniqueAndRightSize && !candidates.containsKey(candidate)) {
                    candidates.put(candidate, n++);

                }
            }
        }
    }

    // SECOND PASS
    void pass2() throws IOException {
        Scanner fileScanner = new Scanner(new File(filename));

        int k = 2;
        while (currentFrequents.size() > 0) {
            currentFrequents.clear();

            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();

                Scanner lineScanner = new Scanner(line);
                lineScanner.useDelimiter("\\s*,\\s*");

                getCandidates(k);
                while (lineScanner.hasNext()) {
                    String item = lineScanner.next();

                    if (allFrequents.containsKey(Collections.singleton(encodeItem(item)))) {
                        basket.add(encodeItem(item));
                    }
                }

                addToCount();
                lineScanner.close();
            }
            findFrequentsFromCandidates();
            k++;
            //fileScanner.close();
        }
    }

    // add to count
    void addToCount() {
        for (Set<Integer> itemset : candidates.keySet()) {
            boolean inBasket = true;
            for (Integer item : itemset) {
                if (!basket.contains(item)) {
                    inBasket = false;
                    break;
                }
            }
            if (inBasket) {
                if (counts.containsKey(candidates.get(itemset))) {
                    counts.put(candidates.get(itemset), counts.get(candidates.get(itemset)) +1);
                } else {
                    counts.put(candidates.get(itemset), 1);
                }
            }
        }
        basket.clear();
    }

    void findFrequentsFromCandidates() {
        for (Set<Integer> itemset : candidates.keySet()) {
            if (counts.getOrDefault(candidates.get(itemset), 0) >= s
                    && !allFrequents.containsKey(itemset) && !currentFrequents.containsKey(itemset)) {
                allFrequents.put(itemset, allFrequents.size()+1);
                currentFrequents.put(itemset, allFrequents.size()+1);
            }
        }

    }

    float getConfidence(Integer itemCode) {
        if (!counts.containsKey(itemCode)) {
            throw new IllegalArgumentException("could not find item code " + itemCode);
        }
        return (float) counts.get(itemCode) / totalBaskets;
    }
}