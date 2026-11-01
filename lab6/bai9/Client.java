import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            AuctionService service = (AuctionService) registry.lookup("AuctionService");

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\n=== HE THONG DAU GIA TRUC TUYEN ===");
                System.out.println("1. Chu so huu (Seller) - Them san pham dau gia");
                System.out.println("2. Nguoi dau gia (Bidder) - Xem danh sach san pham");
                System.out.println("3. Nguoi dau gia (Bidder) - Dat gia thau (Bid)");
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
                            System.out.print("Nhap ten san pham: ");
                            String name = scanner.nextLine().trim();
                            System.out.print("Nhap gia khoi diem: ");
                            double price = Double.parseDouble(scanner.nextLine().trim());
                            int id = service.addProduct(name, price);
                            System.out.println("Them san pham thanh cong voi ID: " + id);
                        } catch (NumberFormatException e) {
                            System.out.println("Gia khoi diem khong hop le.");
                        }
                        break;
                    case 2:
                        List<Product> list = service.getProducts();
                        System.out.println("\n--- DANH SACH SAN PHAM DANG DAU GIA ---");
                        if (list.isEmpty()) {
                            System.out.println("Hien chua co san pham nao trong he thong.");
                        } else {
                            for (Product p : list) {
                                System.out.println(p);
                            }
                        }
                        break;
                    case 3:
                        try {
                            System.out.print("Nhap ID san pham muon dau gia: ");
                            int prodId = Integer.parseInt(scanner.nextLine().trim());
                            System.out.print("Nhap ten nguoi dau gia: ");
                            String bidder = scanner.nextLine().trim();
                            System.out.print("Nhap gia thau (bid): ");
                            double bid = Double.parseDouble(scanner.nextLine().trim());
                            boolean ok = service.placeBid(prodId, bidder, bid);
                            if (ok) {
                                System.out.println("Dat gia thau thanh cong!");
                            } else {
                                System.out.println("Dat gia that bai! (Gia phai cao hon gia hien tai hoac sai ID san pham)");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Thong tin nhap vao khong hop le.");
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
