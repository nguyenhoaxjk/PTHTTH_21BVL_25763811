import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            CalculatorService service = (CalculatorService) registry.lookup("CalculatorService");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Nhap so nguyen a: ");
            int a = scanner.nextInt();
            System.out.print("Nhap so nguyen b: ");
            int b = scanner.nextInt();

            int sum = service.add(a, b);
            System.out.println("Tong a + b nhan tu server: " + sum);
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
