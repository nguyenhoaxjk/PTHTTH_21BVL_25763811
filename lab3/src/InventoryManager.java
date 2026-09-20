import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InventoryManager {

    /**
     * Tự động xác định đường dẫn thư mục data dù chạy từ thư mục gốc lab3 hay từ
     * src
     */
    private static Path getDataDirectory() {
        if (Files.exists(Path.of("data"))) {
            return Path.of("data");
        } else if (Files.exists(Path.of("..", "data"))) {
            return Path.of("..", "data");
        }
        return Path.of("data");
    }

    public static void main(String[] args) {
        Path dataDir = getDataDirectory();
        Path csvFile = dataDir.resolve("inventory.csv");
        Path reportFile = dataDir.resolve("inventory-report.txt");

        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

        System.out.println("==================================================");
        System.out.println("     CHƯƠNG TRÌNH QUẢN LÝ TỒN KHO (INVENTORY)     ");
        System.out.println("==================================================");

        // BƯỚC 1: Nhập danh sách sản phẩm từ bàn phím
        List<Product> inputList = inputProductsFromConsole(scanner);
        if (inputList.isEmpty()) {
            System.out.println("Chưa có sản phẩm nào được nhập. Kết thúc chương trình.");
            return;
        }   

        // BƯỚC 2: Lưu danh sách vào tệp data/inventory.csv
        System.out.println("\n--- Đang lưu danh sách vào tệp CSV ---");
        saveToCsv(csvFile, inputList);

        // BƯỚC 3: Đọc lại tệp CSV và tái tạo danh sách đối tượng Product
        System.out.println("\n--- Đọc lại dữ liệu từ tệp CSV để kiểm tra ---");
        List<Product> loadedList = loadFromCsv(csvFile);

        // BƯỚC 4: Hiển thị toàn bộ sản phẩm và tính tổng giá trị tồn kho
        System.out.println("\n--- DANH SÁCH SẢN PHẨM TỒN KHO ---");
        double totalValue = 0;
        for (Product p : loadedList) {
            System.out.println(p);
            totalValue += p.inventoryValue();
        }
        System.out.printf("==> TỔNG GIÁ TRỊ TỒN KHO: %,.0f VND%n", totalValue);

        // BƯỚC 5: Tìm sản phẩm có giá trị tồn kho cao nhất
        Product maxProduct = findMaxInventoryProduct(loadedList);
        if (maxProduct != null) {
            System.out.println("\n--- SẢN PHẨM CÓ TỒN KHO CAO NHẤT ---");
            System.out.printf("%s - Giá trị: %,.0f VND%n", maxProduct.getName(), maxProduct.inventoryValue());
        }

        // BƯỚC 6: Ghi báo cáo tổng hợp vào data/inventory-report.txt
        writeReport(reportFile, loadedList, totalValue, maxProduct);
    }

    /**
     * Chức năng 1: Nhập danh sách sản phẩm từ bàn phím có kiểm tra hợp lệ
     */
    private static List<Product> inputProductsFromConsole(Scanner scanner) {
        List<Product> list = new ArrayList<>();
        System.out.println("\n[1] BẮT ĐẦU NHẬP SẢN PHẨM (Nhập mã 'q' để dừng nhập)");

        int index = 1;
        while (true) {
            System.out.printf("%n--- Nhập sản phẩm thứ %d ---%n", index);
            System.out.print("Nhập mã sản phẩm (hoặc 'q' để kết thúc): ");
            String code = scanner.nextLine().trim();
            if (code.equalsIgnoreCase("q")) {
                break;
            }
            if (code.isBlank()) {
                System.err.println("Lỗi: Mã sản phẩm không được để trống!");
                continue;
            }

            System.out.print("Nhập tên sản phẩm: ");
            String name = scanner.nextLine().trim();
            if (name.isBlank()) {
                System.err.println("Lỗi: Tên sản phẩm không được để trống!");
                continue;
            }

            double price;
            try {
                System.out.print("Nhập đơn giá (> 0): ");
                price = Double.parseDouble(scanner.nextLine().trim());
                if (price <= 0) {
                    System.err.println("Lỗi: Đơn giá phải lớn hơn 0!");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.err.println("Lỗi: Đơn giá phải là số hợp lệ!");
                continue;
            }

            int quantity;
            try {
                System.out.print("Nhập số lượng (>= 0): ");
                quantity = Integer.parseInt(scanner.nextLine().trim());
                if (quantity < 0) {
                    System.err.println("Lỗi: Số lượng không được âm!");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.err.println("Lỗi: Số lượng phải là số nguyên!");
                continue;
            }

            try {
                Product p = new Product(code, name, price, quantity);
                list.add(p);
                System.out.println("-> Thêm sản phẩm thành công!");
                index++;
            } catch (IllegalArgumentException e) {
                System.err.println("Lỗi dữ liệu: " + e.getMessage());
            }
        }
        return list;
    }

    /**
     * Chức năng 2: Lưu danh sách vào tệp CSV bằng UTF-8 (dùng try-with-resources)
     */
    private static void saveToCsv(Path file, List<Product> list) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }

            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                // Ghi dòng tiêu đề
                writer.write("ma,ten,donGia,soLuong");
                writer.newLine();

                for (Product p : list) {
                    writer.write(p.toCsvLine());
                    writer.newLine();
                }
            }
            System.out.println("Đã lưu thành công " + list.size() + " sản phẩm vào: " + file.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Lỗi ghi tệp CSV " + file + ": " + e.getMessage());
        }
    }

    /**
     * Chức năng 3 & 7: Đọc lại tệp CSV, xử lý tệp thiếu, dòng lỗi và dữ liệu số sai
     */
    private static List<Product> loadFromCsv(Path file) {
        List<Product> products = new ArrayList<>();

        if (!Files.exists(file)) {
            System.err.println("Lỗi: Không tìm thấy tệp CSV -> " + file.toAbsolutePath());
            return products;
        }

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine(); // Bỏ qua tiêu đề
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length != 4) {
                    System.err.printf("Bỏ qua dòng %d trong '%s' (thiếu cột dữ liệu): %s%n",
                            lineNumber, file.getFileName(), line);
                    continue;
                }

                try {
                    String code = parts[0].trim();
                    String name = parts[1].trim();
                    double price = Double.parseDouble(parts[2].trim());
                    int quantity = Integer.parseInt(parts[3].trim());

                    products.add(new Product(code, name, price, quantity));
                } catch (NumberFormatException e) {
                    System.err.printf("Dòng %d trong '%s' sai định dạng số: %s%n",
                            lineNumber, file.getFileName(), e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.err.printf("Dòng %d trong '%s' không hợp lệ: %s%n",
                            lineNumber, file.getFileName(), e.getMessage());
                }
            }
            System.out.println("Đọc tệp thành công! Đã tải " + products.size() + " sản phẩm hợp lệ.");
        } catch (IOException e) {
            System.err.println("Lỗi đọc tệp " + file + ": " + e.getMessage());
        }

        return products;
    }

    /**
     * Chức năng 5: Tìm sản phẩm có giá trị tồn kho cao nhất
     */
    private static Product findMaxInventoryProduct(List<Product> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        Product max = list.get(0);
        for (Product p : list) {
            if (p.inventoryValue() > max.inventoryValue()) {
                max = p;
            }
        }
        return max;
    }

    /**
     * Chức năng 6: Ghi báo cáo tổng hợp vào file txt
     */
    private static void writeReport(Path file, List<Product> list, double totalValue, Product maxProduct) {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("==================================================");
            writer.newLine();
            writer.write("           BÁO CÁO TỔNG HỢP TỒN KHO               ");
            writer.newLine();
            writer.write("==================================================");
            writer.newLine();
            writer.write("Tổng số loại sản phẩm : " + list.size());
            writer.newLine();
            writer.write(String.format("Tổng giá trị tồn kho  : %,.0f VND", totalValue));
            writer.newLine();
            if (maxProduct != null) {
                writer.write(String.format("Sản phẩm giá trị nhất : %s - %s (%,.0f VND)",
                        maxProduct.getCode(), maxProduct.getName(), maxProduct.inventoryValue()));
                writer.newLine();
            }
            writer.write("==================================================");
            writer.newLine();
            System.out.println("\n-> Đã ghi báo cáo tổng hợp vào: " + file.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Lỗi ghi báo cáo " + file + ": " + e.getMessage());
        }
    }
}
