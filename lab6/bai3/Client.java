import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            PrimeService service = (PrimeService) registry.lookup("PrimeService");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Nhap mot so nguyen: ");
            int n = scanner.nextInt();

            boolean result = service.isPrime(n);
            if (result) {
                System.out.println(n + " la so nguyen to.");
            } else {
                System.out.println(n + " khong phai la so nguyen to.");
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
