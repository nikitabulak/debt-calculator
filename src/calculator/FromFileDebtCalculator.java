package calculator;

import java.util.*;

public class FromFileDebtCalculator implements DebtCalculator {

    private final Map<String, Double> creditMap;
    private final Map<String, Double> debitMap;
    private final Set<String> names;
    private final Map<String, Map<String, Double>> outputTable;
    private final Printer filePrinter;

    private final String outputFilePath;

    public FromFileDebtCalculator(String inputFilePath, String outputFilePath) {
        Parser fileParser = new FileParser();
        fileParser.readInputFile(inputFilePath);
        creditMap = fileParser.getCredit();
        debitMap = fileParser.getDebit();
        names = debitMap.keySet();
        outputTable = fileParser.getOutputTable();
        filePrinter = new FilePrinter();
        this.outputFilePath = outputFilePath;
    }

    private void reduceDebitsAndCredits() {
        for (String name : names) {
            double currentDebit = debitMap.get(name);
            double currentCredit = creditMap.get(name);
            double reduceSum = Math.min(currentDebit, currentCredit);
            debitMap.put(name, currentDebit - reduceSum);
            creditMap.put(name, currentCredit - reduceSum);
        }
    }

    //Урезает дебит и кредит последовательно
    private void calculateDebts() {
        reduceDebitsAndCredits();
        for (String creditor : names) {
            double credit = creditMap.get(creditor);
            if (credit > 0) {
                for (String debtor : names) {
                    double debit = debitMap.get(debtor);
                    if (debit == 0) {
                        continue;
                    }
                    double reduceSum = Math.min(credit, debit);
                    credit = credit - reduceSum;
                    debit = debit - reduceSum;
                    creditMap.put(creditor, credit);//not necessary
                    debitMap.put(debtor, debit);

                    Map<String, Double> innerMap = outputTable.get(debtor);
                    innerMap.put(creditor, reduceSum);
                    outputTable.put(debtor, innerMap);
                    if (credit == 0) {
                        break;
                    }
                }
            }
        }
    }

    //Урезает дебит и кредит, начиная с максимальных значений. Эффективнее предыдущего метода.
    private void calculateDebtsByMax() {
        reduceDebitsAndCredits();
        Integer debtorIndex = 0;
        Integer creditorIndex = 0;
        Map<Integer, String> indexToDebtorName = new HashMap<>();
        Map<Integer, String> indexToCreditorName = new HashMap<>();
        List<Double> debtList = new ArrayList<>(names.size());
        List<Double> creditList = new ArrayList<>(names.size());

        for (String creditor : names) {
            double currentCredit = creditMap.get(creditor);
            if (currentCredit > 0) {
                indexToCreditorName.put(creditorIndex, creditor);
                creditList.add(currentCredit);
                creditorIndex++;
            }
        }
        for (String debtor : names) {
            double currentDebit = debitMap.get(debtor);
            if (currentDebit > 0) {
                indexToDebtorName.put(debtorIndex, debtor);
                debtList.add(currentDebit);
                debtorIndex++;
            }
        }

        double maxDebt = debtList.stream().reduce(0.0, Math::max);
        double maxCredit = creditList.stream().reduce(0.0, Math::max);
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

    @Override
    public void run() {
//        calculateDebts();
        calculateDebtsByMax();
        filePrinter.writeToFile(names, outputTable, outputFilePath);
        filePrinter.print(outputTable);
    }
}
