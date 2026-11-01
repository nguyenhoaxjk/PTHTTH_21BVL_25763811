import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class SellerClient {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            AuctionService service = (AuctionService) registry.lookup("AuctionService");

            Scanner scanner = new Scanner(System.in);
            System.out.println("=== CHU SO HUU (SELLER) ===");
            while (true) {
                System.out.print("Nhap ten san pham (hoac 'exit' de thoat): ");
                String name = scanner.nextLine().trim();
                if (name.equalsIgnoreCase("exit")) {
                    break;
                }
                System.out.print("Nhap gia khoi diem: ");
                try {
                    double price = Double.parseDouble(scanner.nextLine().trim());
                    int id = service.addProduct(name, price);
                    System.out.println("Da them san pham vao he thong thanh cong! ID san pham: " + id);
                } catch (NumberFormatException e) {
                    System.out.println("Gia khoi diem khong hop le.");
                }
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
