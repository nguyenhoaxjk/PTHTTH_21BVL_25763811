import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            BankAccountService service = (BankAccountService) registry.lookup("BankAccountService");

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\n--- QUAN LY TAI KHOAN NGAN HANG ---");
                System.out.println("1. Kiem tra so du");
                System.out.println("2. Gui tien (Deposit)");
                System.out.println("3. Rut tien (Withdraw)");
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
                        System.out.println("So du hien tai: " + service.getBalance());
                        break;
                    case 2:
                        System.out.print("Nhap so tien muon gui: ");
                        try {
                            double depositAmount = Double.parseDouble(scanner.nextLine().trim());
                            if (depositAmount <= 0) {
                                System.out.println("So tien gui phai lon hon 0.");
                            } else {
                                service.deposit(depositAmount);
                                System.out.println("Gui tien thanh cong! So du moi: " + service.getBalance());
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("So tien khong hop le.");
                        }
                        break;
                    case 3:
                        System.out.print("Nhap so tien muon rut: ");
                        try {
                            double withdrawAmount = Double.parseDouble(scanner.nextLine().trim());
                            if (withdrawAmount <= 0) {
                                System.out.println("So tien rut phai lon hon 0.");
                            } else {
                                boolean success = service.withdraw(withdrawAmount);
                                if (success) {
                                    System.out.println("Rut tien thanh cong! So du moi: " + service.getBalance());
                                } else {
                                    System.out.println("Rut tien that bai! So du khong du hoac khong hop le.");
                                }
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("So tien khong hop le.");
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
