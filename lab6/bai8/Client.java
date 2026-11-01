import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            FlightBookingService service = (FlightBookingService) registry.lookup("FlightBookingService");

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\n--- HE THONG DAT VE MAY BAY ---");
                System.out.println("1. Kiem tra so ghe trong");
                System.out.println("2. Dat ve");
                System.out.println("3. Huy ve");
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
                        System.out.print("Nhap ma chuyen bay (vi du: VN123, VJ456, QH789): ");
                        String fnCheck = scanner.nextLine().trim();
                        int seats = service.availableSeats(fnCheck);
                        if (seats >= 0) {
                            System.out.println("Chuyen bay " + fnCheck.toUpperCase() + " con trong: " + seats + " ghe.");
                        } else {
                            System.out.println("Khong tim thay chuyen bay: " + fnCheck);
                        }
                        break;
                    case 2:
                        try {
                            System.out.print("Nhap ma chuyen bay: ");
                            String fnBook = scanner.nextLine().trim();
                            System.out.print("Nhap so luong ghe muon dat: ");
                            int numSeatsBook = Integer.parseInt(scanner.nextLine().trim());
                            boolean booked = service.bookTicket(fnBook, numSeatsBook);
                            if (booked) {
                                System.out.println("Dat ve thanh cong!");
                            } else {
                                System.out.println("Dat ve that bai! (Khong du ghe hoac sai ma chuyen bay)");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("So luong ghe phai la so nguyen.");
                        }
                        break;
                    case 3:
                        try {
                            System.out.print("Nhap ma chuyen bay: ");
                            String fnCancel = scanner.nextLine().trim();
                            System.out.print("Nhap so luong ghe muon huy: ");
                            int numSeatsCancel = Integer.parseInt(scanner.nextLine().trim());
                            boolean cancelled = service.cancelBooking(fnCancel, numSeatsCancel);
                            if (cancelled) {
                                System.out.println("Huy ve thanh cong!");
                            } else {
                                System.out.println("Huy ve that bai! (Sai ma chuyen bay hoac so luong khong hop le)");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("So luong ghe phai la so nguyen.");
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
