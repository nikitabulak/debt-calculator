package calculator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DebtCalculator {
    private final Map<String, Map<String, Double>> outputTable = new LinkedHashMap<>();
    private final Map<String, Integer> namesOrder = new LinkedHashMap<>();
    private final Map<Integer, String> namesOrderReverse = new LinkedHashMap<>();


    private String[] getNextNotNullPair(String debtorName, String creditorName) {
        int debtorNumber = namesOrder.get(debtorName);
        int creditorNumber = namesOrder.get(creditorName);
        boolean creditorNumberReset = false;
        for (int i = debtorNumber; i < namesOrderReverse.keySet().size() + 2; i++) {
            for (int j = creditorNumber; j < namesOrderReverse.keySet().size() + 2; j++) {
                if (!namesOrderReverse.get(i).equals(namesOrderReverse.get(j)) &&
                        outputTable.get(namesOrderReverse.get(i)).get(namesOrderReverse.get(j)) != 0) {
                    return new String[]{namesOrderReverse.get(i), namesOrderReverse.get(j)};
                }
            }
            // Проверка следующей строки с начала
            if (!creditorNumberReset && creditorNumber > 2) {
                creditorNumberReset = true;
                creditorNumber = 2;
            }
        }
        return null;
    }

    private void printOutputTable() {
        System.out.println();
        for (String debtorName : outputTable.keySet()) {
            System.out.print(debtorName + ": ");
            for (String creditorName : outputTable.get(debtorName).keySet()) {
                System.out.print(creditorName + ":" + outputTable.get(debtorName).get(creditorName) + " ");
            }
            System.out.println();
        }
    }

    private void writeOutputFile() {
        List<String> outputFileLines = new ArrayList<>();
        String headerLine = "";
        for (String name : namesOrder.keySet()) {
            headerLine += "," + name;
        }
        outputFileLines.add(headerLine);
        for (String debtorName : namesOrder.keySet()) {
            String newLine = debtorName;
            for (String creditorName : namesOrder.keySet()) {
                Double debt = outputTable.get(debtorName).get(creditorName);
                newLine += "," + (debt != 0 ? debt : "");
            }
            outputFileLines.add(newLine);
        }
        try {
            if (!Files.exists(Path.of(".\\resourses\\output.csv"))) {
                Files.createFile(Path.of(".\\resourses\\output.csv"));
            }
            Files.write(Path.of(".\\resourses\\output.csv"), outputFileLines);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readInputFile() {
        try {
            String inputFilePath = ".\\resourses\\input.csv"; //оk
//            String inputFilePath = ".\\resourses\\input_lowlines.csv"; //ok
//            String inputFilePath = ".\\resourses\\input1.csv"; //оk
//            String inputFilePath = ".\\resourses\\input2.csv"; //ok
//            String inputFilePath = ".\\resourses\\input3.csv"; //ok
//            String inputFilePath = ".\\resourses\\input4.csv"; //ok
//            String inputFilePath = ".\\resourses\\input4_lowlines.csv"; //ok
            List<String> inputFileLines = Files.readAllLines(Path.of(inputFilePath));
            String[] headerLine = inputFileLines.get(0).split(",");
            //Начальное заполнение структур
            for (int debtorNumber = 2; debtorNumber < headerLine.length; debtorNumber++) {
                String name = headerLine[debtorNumber].trim();
                namesOrder.put(name, debtorNumber);
                namesOrderReverse.put(debtorNumber, name);
                Map<String, Double> innerMap = new LinkedHashMap<>();
                outputTable.put(name, innerMap);
            }
            //Начальное заполнение внутренних мап нулями
            for (String lineName : namesOrder.keySet()) {
                for (String columnName : namesOrder.keySet()) {
                    outputTable.get(lineName).put(columnName, 0.0);
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

                    if (outputTable.get(debtor) == null) {
                        inputLine = new LinkedHashMap<>();
                        inputLine.put(creditor, Double.parseDouble(currentLineArray[namesOrder.get(debtor)]));
                    } else {
                        inputLine = outputTable.get(debtor);
                        if (inputLine.containsKey(creditor)) {
                            debtAmount += inputLine.get(creditor);
                        }
                        inputLine.put(creditor, debtAmount);
                    }
                    outputTable.put(debtor, inputLine);
                }
            }
        } catch (IOException e) {
            System.out.println("Невозможно прочитать файл!");
        }
    }

    private void reduceMutualDebts() {
        for (String debtorName : namesOrder.keySet()) {
            for (String creditorName : namesOrder.keySet()) {
                if (!debtorName.equals(creditorName)) {
                    double reductionSum = Math.min(outputTable.get(debtorName).get(creditorName),
                            outputTable.get(creditorName).get(debtorName));
                    outputTable.get(debtorName).put(creditorName, outputTable.get(debtorName)
                            .get(creditorName) - reductionSum);
                    outputTable.get(creditorName).put(debtorName, outputTable.get(creditorName)
                            .get(debtorName) - reductionSum);
                } else {
                    outputTable.get(debtorName).put(creditorName, 0.0);
                }
            }
        }
    }

    private void reduceDebts() {
        //Задаем точку начала поиска первого долга в верхнем левом углу будущей выходной таблицы
        int startDebtorNumber = 2;
        int startCreditorNumber = 2;
        //Получили первую ячейку с ненулевым долгом, обновили ее координаты
        String[] currentPair = getNextNotNullPair(namesOrderReverse.get(startDebtorNumber),
                namesOrderReverse.get(startCreditorNumber));
        //Пока не дойдем до конца таблицы выполняем:
        while (currentPair != null) {
            startDebtorNumber = namesOrder.get(currentPair[0]);
            startCreditorNumber = namesOrder.get(currentPair[1]);
            String debtorName = currentPair[0];
            String creditorName = currentPair[1];
            double debtorsDebt = outputTable.get(debtorName).get(creditorName);
            double creditorsDebt;
            boolean debtsReduced = false;

            //Если кредитор должен кому-то, то исключаем кредитора из цепочки должник - кредитор - кредитор кредитора
            for (String creditorsCreditor : namesOrder.keySet()) {
                creditorsDebt = outputTable.get(creditorName).get(creditorsCreditor);
                if (creditorsDebt != 0) {
                    double reductionSum = Math.min(debtorsDebt, creditorsDebt);
                    debtorsDebt = debtorsDebt - reductionSum;
                    creditorsDebt = creditorsDebt - reductionSum;
                    outputTable.get(debtorName).put(creditorsCreditor,
                            outputTable.get(debtorName).get(creditorsCreditor) + reductionSum);
                    outputTable.get(creditorName).put(creditorsCreditor, creditorsDebt);
                    outputTable.get(debtorName).put(creditorName, debtorsDebt);
                    debtsReduced = true;
                }
            }
            /*Если была произведена операция: усекаем взаимные долги и проверяем строку сначала
             * Иначе если последнее значение в строке: проверяем следующую строку сначала
             * Иначе: проверяем следующее значение в строке */
            if (debtsReduced) {
                reduceMutualDebts();
                startCreditorNumber = 2;
            } else if (startCreditorNumber == namesOrderReverse.keySet().size() + 1) {
                startDebtorNumber++;
                startCreditorNumber = 2;
            } else {
                startCreditorNumber++;
            }
            currentPair = getNextNotNullPair(namesOrderReverse.get(startDebtorNumber), namesOrderReverse.get(startCreditorNumber));
        }
    }

    private void reduceAmountOfTransactions() {
        Integer debtorIndex = 0;
        Integer creditorIndex = 0;
        Map<Integer, String> indexToDebtorName = new HashMap<>();
        Map<Integer, String> indexToCreditorName = new HashMap<>();
        Map<String, Integer> creditorToIndexName = new HashMap<>();
        List<Double> debtList = new ArrayList<>(namesOrder.size());
        List<Double> creditList = new ArrayList<>(namesOrder.size());

        //Анализ исходной таблицы, подсчет суммарных долгов и кредитов (у кого они есть)
        for (String debtorName : namesOrder.keySet()) {
            Double totalDebtAmount = outputTable.get(debtorName).values().stream().mapToDouble(Double::doubleValue).sum();
            if (totalDebtAmount != 0) {
                indexToDebtorName.put(debtorIndex, debtorName);
                debtList.add(totalDebtAmount);
                debtorIndex++;
            }
            for (String creditorName : namesOrder.keySet()) {
                Double currentCredit = outputTable.get(debtorName).get(creditorName);
                if (currentCredit != 0) {
                    if (!creditorToIndexName.containsKey(creditorName)) {
                        creditorToIndexName.put(creditorName, creditorIndex);
                        indexToCreditorName.put(creditorIndex, creditorName);
                        creditList.add(creditorIndex, currentCredit);
                        creditorIndex++;
                    } else {
                        int currentCreditorIndex = creditorToIndexName.get(creditorName);
                        creditList.set(currentCreditorIndex, creditList.get(currentCreditorIndex) + currentCredit);
                    }
                    //Обнуляем в исходной таблице каждое считанное значение
                    outputTable.get(debtorName).put(creditorName, 0.0);
                }
            }
        }

        //Подсчет и запись окончательных переводов (долгов)
        Double maxDebt = debtList.stream().reduce(0.0, Math::max);
        Double maxCredit = creditList.stream().reduce(0.0, Math::max);
        while (maxDebt != 0 && maxCredit != 0) {
            int maxDebtIndex = debtList.indexOf(maxDebt);
            int maxCreditIndex = creditList.indexOf(maxCredit);
            double reductionSum = Math.min(maxDebt, maxCredit);
            debtList.set(maxDebtIndex, debtList.get(maxDebtIndex) - reductionSum);
            creditList.set(maxCreditIndex, creditList.get(maxCreditIndex) - reductionSum);
            outputTable.get(indexToDebtorName.get(maxDebtIndex)).put(indexToCreditorName.get(maxCreditIndex), reductionSum);
            maxDebt = debtList.stream().reduce(0.0, Math::max);
            maxCredit = creditList.stream().reduce(0.0, Math::max);
        }
    }

    public void calculateDebts() {
        readInputFile();
        reduceMutualDebts();
        reduceDebts();
        reduceAmountOfTransactions();
        writeOutputFile();
        printOutputTable();
    }
}
