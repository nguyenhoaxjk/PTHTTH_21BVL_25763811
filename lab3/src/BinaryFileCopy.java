import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class BinaryFileCopy {
    private static final int BUFFER_SIZE = 8192;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Cách dùng: java BinaryFileCopy <nguồn> <đích>");
            System.out.println("Ví dụ: java BinaryFileCopy data/source.jpg data/copy.jpg");
            return;
        }

        Path source = Path.of(args[0]);
        Path target = Path.of(args[1]);

        if (!Files.exists(source)) {
            System.err.println("Lỗi: Tệp nguồn không tồn tại -> " + source.toAbsolutePath());
            return;
        }

        long totalBytes = 0;
        try (InputStream input = new BufferedInputStream(Files.newInputStream(source));
             OutputStream output = new BufferedOutputStream(Files.newOutputStream(target))) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            // Đọc và ghi đúng số byte thực tế đọc được
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
        } catch (IOException e) {
            System.err.println("Sao chép thất bại: " + e.getMessage());
            return;
        }

        // Sau khi khối try kết thúc, output stream đã tự động đóng (close) và xả (flush) 100% dữ liệu
        System.out.println("Đã sao chép thành công: " + totalBytes + " byte.");
        try {
            boolean isMatch = Files.size(source) == Files.size(target);
            System.out.printf("Kích thước nguồn: %d byte | Kích thước đích: %d byte%n", 
                    Files.size(source), Files.size(target));
            System.out.printf("Khớp kích thước: %b%n", isMatch);
        } catch (IOException e) {
            System.err.println("Lỗi kiểm tra kích thước: " + e.getMessage());
        }
    }
}
