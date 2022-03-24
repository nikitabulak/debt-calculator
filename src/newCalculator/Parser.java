package newCalculator;

import java.util.Map;

public interface Parser {
    void readInputFile(String inputFilePath);

    Map<String, Double> getCredit();

    Map<String, Double> getDebit();

    Map<String, Integer> getNamesRowIndex();

    Map<String, Map<String, Double>> getOutputTable();
}
