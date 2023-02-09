import calculator.DebtCalculator;
import calculator.FromFileDebtCalculator;

public class Main {
    public static void main(String[] args) {
//        String inputFilePath = "C:\\Users\\Nikita\\dev\\debtCalculator\\resources\\input.csv"; //оk
//        String inputFilePath = "C:\Users\Nikita\dev\debtCalculator\\resources\\input_droptest.csv"; //оk
//        String inputFilePath = "C:\Users\Nikita\dev\debtCalculator\\resources\\input_lowlines.csv"; //ok
//        String inputFilePath = "C:\Users\Nikita\dev\debtCalculator\\resources\\input1.csv"; //оk
//        String inputFilePath = "C:\Users\Nikita\dev\debtCalculator\\resources\\input2.csv"; //ok
//        String inputFilePath = "C:\Users\Nikita\dev\debtCalculator\\resources\\input3.csv"; //ok; в calculateDebts() на одну операцию больше, чем в *ByMax()
//        String inputFilePath = "C:\Users\Nikita\dev\debtCalculator\\resources\\input4.csv"; //ok; в calculateDebts() на одну операцию больше, чем в *ByMax()
//        String inputFilePath = "C:\Users\Nikita\dev\debtCalculator\\resources\\input4_lowlines.csv"; //ok;  в calculateDebts() на одну операцию больше, чем в *ByMax()
//        String outputFilePath = "C:\\Users\\Nikita\\dev\\debtCalculator\\resources\\output.csv";
        String inputFilePath = args[0];
        String outputFilePath = args[1];
        DebtCalculator debtCalculator = new FromFileDebtCalculator(inputFilePath, outputFilePath);
        debtCalculator.run();
    }
}
