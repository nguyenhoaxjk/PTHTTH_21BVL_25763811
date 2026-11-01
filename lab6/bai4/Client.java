import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ContactService service = (ContactService) registry.lookup("ContactService");

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\n--- QUAN LY DANH BA ---");
                System.out.println("1. Them / Sua lien he");
                System.out.println("2. Tim kiem lien he");
                System.out.println("3. Xoa lien he");
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
                        System.out.print("Nhap ten: ");
                        String name = scanner.nextLine().trim();
                        System.out.print("Nhap so dien thoai: ");
                        String phone = scanner.nextLine().trim();
                        service.addContact(name, phone);
                        System.out.println("Da luu lien he thanh cong!");
                        break;
                    case 2:
                        System.out.print("Nhap ten can tim: ");
                        String searchName = scanner.nextLine().trim();
                        String foundPhone = service.findContact(searchName);
                        if (foundPhone != null) {
                            System.out.println("So dien thoai cua " + searchName + ": " + foundPhone);
                        } else {
                            System.out.println("Khong tim thay lien he: " + searchName);
                        }
                        break;
                    case 3:
                        System.out.print("Nhap ten can xoa: ");
                        String delName = scanner.nextLine().trim();
                        boolean deleted = service.deleteContact(delName);
                        if (deleted) {
                            System.out.println("Da xoa thanh cong lien he: " + delName);
                        } else {
                            System.out.println("Khong tim thay lien he de xoa: " + delName);
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
