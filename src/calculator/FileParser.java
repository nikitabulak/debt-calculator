package calculator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileParser implements Parser{

    private final Map<String, Map<String, Double>> inputTable = new LinkedHashMap<>();
    private final Map<String, Integer> namesOrder = new LinkedHashMap<>();
    private final Map<Integer, String> namesOrderReverse = new LinkedHashMap<>();

    @Override
    public void readInputFile(String inputFilePath) {

        try {
            List<String> inputFileLines = Files.readAllLines(Path.of(inputFilePath));
            String[] headerLine = inputFileLines.get(0).split(",");
            //Начальное заполнение структур
            for (int debtorNumber = 2; debtorNumber < headerLine.length; debtorNumber++) {
                String name = headerLine[debtorNumber].trim();
                namesOrder.put(name, debtorNumber);
                namesOrderReverse.put(debtorNumber, name);
                Map<String, Double> innerMap = new LinkedHashMap<>();
                inputTable.put(name, innerMap);
            }
            //Начальное заполнение внутренних мап нулями
            for (String lineName : namesOrder.keySet()) {
                for (String columnName : namesOrder.keySet()) {
                    inputTable.get(lineName).put(columnName, 0.0);
                }
            }
            //Заполнение мапы, представляющей выходной файл, данными из строк входного файла
            for (int inputLineNumber = 1; inputLineNumber < inputFileLines.size(); inputLineNumber++) {
                /*Считывание строки входного файла, добавление нулей вместо пропусков данных для избежания ошибок
                и удобства последующего вывода*/
                String currentLine = inputFileLines.get(inputLineNumber).replaceAll(",$", ",0");
                String[] currentLineArray = currentLine.split(",");
                for (int i = 0; i < currentLineArray.length; i++) {
                    if (currentLineArray[i].equals("")) {
                        currentLineArray[i] = "0";
                    }
                }
                String creditor = currentLineArray[0];
                for (String debtor : namesOrder.keySet()) {
                    Map<String, Double> inputLine;
                    Double debtAmount = Double.parseDouble(currentLineArray[namesOrder.get(debtor)]);

                    if (inputTable.get(debtor) == null) {
                        inputLine = new LinkedHashMap<>();
                        inputLine.put(creditor, Double.parseDouble(currentLineArray[namesOrder.get(debtor)]));
                    } else {
                        inputLine = inputTable.get(debtor);
                        if (inputLine.containsKey(creditor)) {
                            debtAmount += inputLine.get(creditor);
                        }
                        inputLine.put(creditor, debtAmount);
                    }
                    inputTable.put(debtor, inputLine);
                }
            }
        } catch (IOException e) {
            System.out.println("Невозможно прочитать файл!");
            System.exit(404);
        }
    }

    public Map<String, Map<String, Double>> getTable() {
        return inputTable;
    }

    public Map<String, Integer> getNamesOrder() {
        return namesOrder;
    }

    public Map<Integer, String> getNamesOrderReverse() {
        return namesOrderReverse;
    }
}
