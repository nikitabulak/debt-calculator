package calculator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FromFileDebtCalculator implements DebtCalculator {

    private Map<String, Map<String, Double>> debtTable;
    private Map<String, Integer> namesOrder;
    private Map<Integer, String> namesOrderReverse;

    public FromFileDebtCalculator() {
        debtTable = new LinkedHashMap<>();
        namesOrder = new LinkedHashMap<>();
        namesOrderReverse = new LinkedHashMap<>();
    }

    private String[] getNextNotNullPair(String debtorName, String creditorName) {
        int debtorNumber = namesOrder.get(debtorName);
        int creditorNumber = namesOrder.get(creditorName);
        boolean creditorNumberReset = false;
        for (int i = debtorNumber; i < namesOrderReverse.keySet().size() + 2; i++) {
            for (int j = creditorNumber; j < namesOrderReverse.keySet().size() + 2; j++) {
                if (!namesOrderReverse.get(i).equals(namesOrderReverse.get(j)) &&
                        debtTable.get(namesOrderReverse.get(i)).get(namesOrderReverse.get(j)) != 0) {
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

    @Override
    public void reduceMutualDebts() {
        for (String debtorName : namesOrder.keySet()) {
            for (String creditorName : namesOrder.keySet()) {
                if (!debtorName.equals(creditorName)) {
                    double reductionSum = Math.min(debtTable.get(debtorName).get(creditorName),
                            debtTable.get(creditorName).get(debtorName));
                    debtTable.get(debtorName).put(creditorName, debtTable.get(debtorName)
                            .get(creditorName) - reductionSum);
                    debtTable.get(creditorName).put(debtorName, debtTable.get(creditorName)
                            .get(debtorName) - reductionSum);
                } else {
                    debtTable.get(debtorName).put(creditorName, 0.0);
                }
            }
        }
    }

    @Override
    public void reduceDebts() {
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
            double debtorsDebt = debtTable.get(debtorName).get(creditorName);
            double creditorsDebt;
            boolean debtsReduced = false;

            //Если кредитор должен кому-то, то исключаем кредитора из цепочки должник - кредитор - кредитор кредитора
            for (String creditorsCreditor : namesOrder.keySet()) {
                creditorsDebt = debtTable.get(creditorName).get(creditorsCreditor);
                if (creditorsDebt != 0) {
                    double reductionSum = Math.min(debtorsDebt, creditorsDebt);
                    debtorsDebt = debtorsDebt - reductionSum;
                    creditorsDebt = creditorsDebt - reductionSum;
                    debtTable.get(debtorName).put(creditorsCreditor,
                            debtTable.get(debtorName).get(creditorsCreditor) + reductionSum);
                    debtTable.get(creditorName).put(creditorsCreditor, creditorsDebt);
                    debtTable.get(debtorName).put(creditorName, debtorsDebt);
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

//    @Override
//    public void reduceDebts(){
//
//    }

    @Override
    public void reduceAmountOfTransactions() {
        Integer debtorIndex = 0;
        Integer creditorIndex = 0;
        Map<Integer, String> indexToDebtorName = new HashMap<>();
        Map<Integer, String> indexToCreditorName = new HashMap<>();
        Map<String, Integer> creditorToIndexName = new HashMap<>();
        List<Double> debtList = new ArrayList<>(namesOrder.size());
        List<Double> creditList = new ArrayList<>(namesOrder.size());

        //Анализ исходной таблицы, подсчет суммарных долгов и кредитов (у кого они есть)
        for (String debtorName : namesOrder.keySet()) {
            Double totalDebtAmount = debtTable.get(debtorName).values().stream().mapToDouble(Double::doubleValue).sum();
            if (totalDebtAmount != 0) {
                indexToDebtorName.put(debtorIndex, debtorName);
                debtList.add(totalDebtAmount);
                debtorIndex++;
            }
            for (String creditorName : namesOrder.keySet()) {
                Double currentCredit = debtTable.get(debtorName).get(creditorName);
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
                    debtTable.get(debtorName).put(creditorName, 0.0);
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
            debtTable.get(indexToDebtorName.get(maxDebtIndex)).put(indexToCreditorName.get(maxCreditIndex), reductionSum);
            maxDebt = debtList.stream().reduce(0.0, Math::max);
            maxCredit = creditList.stream().reduce(0.0, Math::max);
        }
    }

    @Override
    public void calculateDebts() {//
        String inputFilePath = ".\\resourses\\input.csv"; //оk
//        String inputFilePath = ".\\resourses\\input_droptest.csv"; //оk
//        String inputFilePath = ".\\resourses\\input_lowlines.csv"; //ok
//        String inputFilePath = ".\\resourses\\input1.csv"; //оk
//        String inputFilePath = ".\\resourses\\input2.csv"; //ok
//        String inputFilePath = ".\\resourses\\input3.csv"; //ok
//        String inputFilePath = ".\\resourses\\input4.csv"; //ok
//        String inputFilePath = ".\\resourses\\input4_lowlines.csv"; //ok
        Parser fileParser = new FileParser();
        fileParser.readInputFile(inputFilePath);
        debtTable = fileParser.getTable();
        namesOrder = fileParser.getNamesOrder();
        namesOrderReverse = fileParser.getNamesOrderReverse();

        reduceMutualDebts();
        reduceDebts();
        reduceAmountOfTransactions();

        Printer filePrinter = new FilePrinter();
        filePrinter.writeToFile(namesOrder.keySet(), debtTable);
        filePrinter.print(debtTable);
    }
}
