import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class BidderClient {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            AuctionService service = (AuctionService) registry.lookup("AuctionService");

            Scanner scanner = new Scanner(System.in);
            System.out.println("=== NGUOI DAU GIA (BIDDER) ===");
            System.out.print("Nhap ten cua ban: ");
            String bidderName = scanner.nextLine().trim();

            while (true) {
                System.out.println("\n1. Xem danh sach san pham dang dau gia");
                System.out.println("2. Dat gia thau (Bid)");
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
                    break;
                }

                switch (choice) {
                    case 1:
                        List<Product> list = service.getProducts();
                        System.out.println("\n--- SAN PHAM DANG DAU GIA ---");
                        if (list.isEmpty()) {
                            System.out.println("Hien chua co san pham nao.");
                        } else {
                            for (Product p : list) {
                                System.out.println(p);
                            }
                        }
                        break;
                    case 2:
                        try {
                            System.out.print("Nhap ID san pham: ");
                            int prodId = Integer.parseInt(scanner.nextLine().trim());
                            System.out.print("Nhap gia thau muon dat: ");
                            double bid = Double.parseDouble(scanner.nextLine().trim());
                            boolean ok = service.placeBid(prodId, bidderName, bid);
                            if (ok) {
                                System.out.println("Dat gia thau thanh cong!");
                            } else {
                                System.out.println("Dat gia that bai! Gia phai cao hon gia hien tai.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Gia tri nhap vao khong hop le.");
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
