package calculator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilePrinter implements Printer {
    @Override
    public void print(Map<String, Map<String, Double>> outputTable) {
        System.out.println();
        for (String debtorName : outputTable.keySet()) {
            System.out.print(debtorName + ": ");
            for (String creditorName : outputTable.get(debtorName).keySet()) {
                System.out.print(creditorName + ":" + outputTable.get(debtorName).get(creditorName) + " ");
            }
            System.out.println();
        }
    }

    @Override
    public void writeToFile(Set<String> namesOrder, Map<String, Map<String, Double>> outputTable) {
        List<String> outputFileLines = new ArrayList<>();
        String headerLine = "";
        for (String name : namesOrder) {
            headerLine += "," + name;
        }
        outputFileLines.add(headerLine);
        for (String debtorName : namesOrder) {
            String newLine = debtorName;
            for (String creditorName : namesOrder) {
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
}
