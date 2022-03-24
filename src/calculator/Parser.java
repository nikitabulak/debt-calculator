package calculator;

import java.util.ArrayList;
import java.util.Map;

public interface Parser {
    void readInputFile(String inputFilePath);

    Map<String, Map<String, Double>> getTable();

    Map<String, Integer> getNamesOrder();

    Map<Integer, String> getNamesOrderReverse();
}
