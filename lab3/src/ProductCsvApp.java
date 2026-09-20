import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProductCsvApp {
    public static void main(String[] args) {
        // Xác định đường dẫn file đầu vào và file đầu ra
        Path input = Path.of("..","data", "products.csv");
        Path report = Path.of("..","data", "report.txt");

        // Danh sách để chứa các đối tượng Product sau khi đọc từ file
        List<Product> products = new ArrayList<>();

        // Đọc dữ liệu từ file CSV
        try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {

            // 1. Đọc và bỏ qua dòng tiêu đề (ma,ten,donGia,soLuong)
            reader.readLine();

            String line;
            int lineNumber = 1; // Để đếm số dòng phục vụ báo lỗi nếu có
            // 2. Vòng lặp đọc từng dòng cho đến hết file
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Bỏ qua dòng trống nếu có
                if (line.isBlank()) {
                    continue;
                }

                // Tách dòng thành các cột theo dấu phẩy
                String[] parts = line.split(",", -1);

                // Kiểm tra xem dòng có đủ 4 cột hay không
                if (parts.length != 4) {
                    System.err.println("Bỏ qua dòng " + lineNumber + ": Không đủ 4 cột");
                    continue;
                }

                try {
                    String code = parts[0].trim();
                    String name = parts[1].trim();
                    double price = Double.parseDouble(parts[2].trim());
                    int quantity = Integer.parseInt(parts[3].trim());

                    // Tạo đối tượng Product và thêm vào danh sách
                    products.add(new Product(code, name, price, quantity));
                } catch (NumberFormatException e) {
                    System.err.println("Dòng " + lineNumber + " sai định dạng số: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.err.println("Dòng " + lineNumber + " dữ liệu không hợp lệ: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Không đọc được tệp CSV: " + e.getMessage());
            return; // Nếu không đọc được file thì dừng chương trình
        }
        // In danh sách ra màn hình console và tính tổng tồn kho
        double totalValue = 0;
        System.out.println("--- DANH SÁCH SẢN PHẨM ---");
        for (Product product : products) {
            System.out.println(product);
            totalValue += product.inventoryValue();
        }

        // Ghi kết quả tổng hợp ra file data/report.txt
        try (BufferedWriter writer = Files.newBufferedWriter(report, StandardCharsets.UTF_8)) {
            writer.write("Số sản phẩm: " + products.size());
            writer.newLine(); // Xuống dòng an toàn trên mọi hệ điều hành
            writer.write("Tổng giá trị tồn kho: %,.0f VND".formatted(totalValue));
            writer.newLine();
            System.out.println("\n-> Đã ghi báo cáo thành công vào: " + report.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Không ghi được báo cáo: " + e.getMessage());
        }
    } // kết thúc main
}
