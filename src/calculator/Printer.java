package calculator;

import java.util.Map;
import java.util.Set;

public interface Printer {
    void print(Map<String, Map<String, Double>> outputTable);

    void writeToFile(Set<String> namesOrder, Map<String, Map<String, Double>> outputTable);
}
