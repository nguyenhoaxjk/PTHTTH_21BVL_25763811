import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            GeometryService service = (GeometryService) registry.lookup("GeometryService");

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\n--- TINH DIEN TICH HINH HOC ---");
                System.out.println("1. Dien tich hinh chu nhat");
                System.out.println("2. Dien tich hinh tron");
                System.out.println("3. Dien tich hinh tam giac");
                System.out.println("0. Thoat");
                System.out.print("Chon chuc nang: ");

                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                int choice;
                try {
                    choice = Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    System.out.println("Vui long nhap so hop le.");
                    continue;
                }

                if (choice == 0) {
                    System.out.println("Tam biet!");
                    break;
                }

                switch (choice) {
                    case 1:
                        try {
                            System.out.print("Nhap chieu rong: ");
                            double width = Double.parseDouble(scanner.nextLine().trim());
                            System.out.print("Nhap chieu dai: ");
                            double height = Double.parseDouble(scanner.nextLine().trim());
                            double rectArea = service.rectangleArea(width, height);
                            System.out.println("Dien tich hinh chu nhat: " + rectArea);
                        } catch (NumberFormatException e) {
                            System.out.println("Thong so khong hop le.");
                        }
                        break;
                    case 2:
                        try {
                            System.out.print("Nhap ban kinh: ");
                            double radius = Double.parseDouble(scanner.nextLine().trim());
                            double circArea = service.circleArea(radius);
                            System.out.println("Dien tich hinh tron: " + circArea);
                        } catch (NumberFormatException e) {
                            System.out.println("Thong so khong hop le.");
                        }
                        break;
                    case 3:
                        try {
                            System.out.print("Nhap do dai canh day (base): ");
                            double base = Double.parseDouble(scanner.nextLine().trim());
                            System.out.print("Nhap chieu cao: ");
                            double triHeight = Double.parseDouble(scanner.nextLine().trim());
                            double triArea = service.triangleArea(base, triHeight);
                            System.out.println("Dien tich hinh tam giac: " + triArea);
                        } catch (NumberFormatException e) {
                            System.out.println("Thong so khong hop le.");
                        }
                        break;
                    default:
                        System.out.println("Lua chon khong hop le.");
                }
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
