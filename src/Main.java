import calculator.DebtCalculator;
import calculator.FromFileDebtCalculator;

public class Main {
    public static void main(String[] args) {
        String inputFilePath = ".\\resourses\\input.csv"; //оk
//        String inputFilePath = ".\\resourses\\input_droptest.csv"; //оk
//        String inputFilePath = ".\\resourses\\input_lowlines.csv"; //ok
//        String inputFilePath = ".\\resourses\\input1.csv"; //оk
//        String inputFilePath = ".\\resourses\\input2.csv"; //ok
//        String inputFilePath = ".\\resourses\\input3.csv"; //ok; в calculateDebts() на одну операцию больше, чем в *ByMax()
//        String inputFilePath = ".\\resourses\\input4.csv"; //ok; в calculateDebts() на одну операцию больше, чем в *ByMax()
//        String inputFilePath = ".\\resourses\\input4_lowlines.csv"; //ok;  в calculateDebts() на одну операцию больше, чем в *ByMax()
        DebtCalculator debtCalculator = new FromFileDebtCalculator(inputFilePath);
        debtCalculator.run();
     }
}
