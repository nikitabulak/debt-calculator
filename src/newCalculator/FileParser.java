package newCalculator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileParser implements Parser {
    private final Map<String, Double> credit = new LinkedHashMap<>();
    private final Map<String, Double> debit = new LinkedHashMap<>();
    private final Map<String, Integer> namesRowIndex = new LinkedHashMap<>();
    private final Map<String, Map<String, Double>> outputTable = new LinkedHashMap<>();

    @Override
    public void readInputFile(String inputFilePath) {
        try {
            List<String> inputFileLines = Files.readAllLines(Path.of(inputFilePath));
            String[] headerLine = inputFileLines.get(0).split(",");
            for (int debtorNumber = 2; debtorNumber < headerLine.length; debtorNumber++) {
                String name = headerLine[debtorNumber].trim();
                debit.put(name, 0.0);
                credit.put(name, 0.0);
                namesRowIndex.put(name, debtorNumber);
                Map<String, Double> innerMap = new LinkedHashMap<>();
                outputTable.put(name, innerMap);
            }
            for (String lineName : namesRowIndex.keySet()) {
                for (String columnName : namesRowIndex.keySet()) {
                    outputTable.get(lineName).put(columnName, 0.0);
                }
            }
            for (int inputLineNumber = 1; inputLineNumber < inputFileLines.size(); inputLineNumber++) {
                String currentLine = inputFileLines.get(inputLineNumber).replaceAll(",$", ",0");
                String[] currentLineArray = currentLine.split(",");
                for (int i = 0; i < currentLineArray.length; i++) {
                    if (currentLineArray[i].equals("")) {
                        currentLineArray[i] = "0";
                    }
                }
                String creditor = currentLineArray[0];
                Double pastCredit = credit.get(creditor);
                Double currentCredit = 0.0;
                for (String debtor : namesRowIndex.keySet()) {
                    Double pastDebit = debit.get(debtor);
                    Double currentDebit = Double.parseDouble(currentLineArray[namesRowIndex.get(debtor)]);
                    debit.put(debtor, pastDebit + currentDebit);
                    currentCredit += currentDebit;
                }
                credit.put(creditor, pastCredit + currentCredit);
            }
        } catch (IOException e) {
            System.out.println("Невозможно прочитать файл!");
            System.exit(404);
        }
    }

    @Override
    public Map<String, Double> getCredit() {
        return credit;
    }

    @Override
    public Map<String, Double> getDebit() {
        return debit;
    }

    @Override
    public Map<String, Integer> getNamesRowIndex() {
        return namesRowIndex;
    }

    public Map<String, Map<String, Double>> getOutputTable() {
        return outputTable;
    }
}
