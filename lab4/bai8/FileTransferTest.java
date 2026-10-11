package bai8;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Random;

public class FileTransferTest {
    private static final int TEST_PORT = 5008;

    public static void main(String[] args) throws Exception {
        System.out.println("======================================================================");
        System.out.println("BỘ KIỂM THỬ TỰ ĐỘNG BÀI 8 - TRUYỀN FILE TCP AN TOÀN (SHA-256)");
        System.out.println("======================================================================\n");

        // Khởi động server ngầm
        Thread serverThread = new Thread(() -> SecureFileServer.startServer(TEST_PORT));
        serverThread.setDaemon(true);
        serverThread.start();
        Thread.sleep(500); // Chờ server sẵn sàng

        File testTempDir = new File("data" + File.separator + "test_source");
        if (!testTempDir.exists()) {
            testTempDir.mkdirs();
        }

        int passed = 0;
        int total = 0;

        // -------------------------------------------------------------
        // CA 1: Kiểm thử file rỗng (0 bytes)
        // -------------------------------------------------------------
        total++;
        File emptyFile = new File(testTempDir, "empty_file.txt");
        if (!emptyFile.exists()) {
            emptyFile.createNewFile();
        }
        System.out.println("[TEST 1] Kiem thu gui file rong (0 bytes)...");
        String res1 = SecureFileClient.uploadFile(emptyFile, "empty_file.txt", "localhost", TEST_PORT);
        File uploaded1 = new File(SecureFileServer.UPLOAD_DIR, "empty_file.txt");
        if (res1.startsWith("OK") && uploaded1.exists() && uploaded1.length() == 0) {
            System.out.println("✔ PASS CA 1: File rong truyen thanh cong va xac thuc SHA-256 dung.\n");
            passed++;
        } else {
            System.err.println("✘ FAIL CA 1: " + res1 + "\n");
        }

        // -------------------------------------------------------------
        // CA 2: Kiểm thử file văn bản tiếng Việt UTF-8
        // -------------------------------------------------------------
        total++;
        File textFile = new File(testTempDir, "tieng_viet.txt");
        String content = "Chào mừng bạn đến với môn Phát triển hệ thống tích hợp (IUH)!\n"
                + "Giao thức truyền file TCP có kiểm tra tính toàn vẹn bằng SHA-256.\n"
                + "Hỗ trợ đầy đủ tiếng Việt có dấu và ký tự Unicode đặc biệt: ☘ ✪ ★.";
        Files.write(textFile.toPath(), content.getBytes(StandardCharsets.UTF_8));
        System.out.println("[TEST 2] Kiem thu gui file van ban tieng Viet UTF-8...");
        String res2 = SecureFileClient.uploadFile(textFile, "tieng_viet.txt", "localhost", TEST_PORT);
        File uploaded2 = new File(SecureFileServer.UPLOAD_DIR, "tieng_viet.txt");
        if (res2.startsWith("OK") && uploaded2.exists() && uploaded2.length() == textFile.length()) {
            System.out.println("✔ PASS CA 2: File tieng Viet truyen nguyen ven, khop SHA-256.\n");
            passed++;
        } else {
            System.err.println("✘ FAIL CA 2: " + res2 + "\n");
        }

        // -------------------------------------------------------------
        // CA 3: Kiểm thử file nhị phân (giả lập file ảnh PNG)
        // -------------------------------------------------------------
        total++;
        File binaryFile = new File(testTempDir, "sample_image.png");
        byte[] fakePngBytes = new byte[16384]; // 16 KB
        // Header chuẩn PNG
        fakePngBytes[0] = (byte) 0x89;
        fakePngBytes[1] = 0x50; // P
        fakePngBytes[2] = 0x4E; // N
        fakePngBytes[3] = 0x47; // G
        new Random(42).nextBytes(fakePngBytes); // Dữ liệu ngẫu nhiên
        try (FileOutputStream fos = new FileOutputStream(binaryFile)) {
            fos.write(fakePngBytes);
        }
        System.out.println("[TEST 3] Kiem thu gui file nhi phan / anh (.png 16KB)...");
        String res3 = SecureFileClient.uploadFile(binaryFile, "sample_image.png", "localhost", TEST_PORT);
        File uploaded3 = new File(SecureFileServer.UPLOAD_DIR, "sample_image.png");
        if (res3.startsWith("OK") && uploaded3.exists() && uploaded3.length() == binaryFile.length()) {
            System.out.println("✔ PASS CA 3: File nhi phan (anh) truyen chinh xac tuyet doi.\n");
            passed++;
        } else {
            System.err.println("✘ FAIL CA 3: " + res3 + "\n");
        }

        // -------------------------------------------------------------
        // CA 4: Kiểm thử chống Path Traversal (tên file chứa '../../attack.txt')
        // -------------------------------------------------------------
        total++;
        System.out.println("[TEST 4] Kiem thu chong Path Traversal (ten file: '../../attack_script.txt')...");
        String maliciousName = "../../attack_script.txt";
        String res4 = SecureFileClient.uploadFile(textFile, maliciousName, "localhost", TEST_PORT);

        File outsideFile = new File("attack_script.txt");
        File parentDirFile = new File(".." + File.separator + "attack_script.txt");
        File insideUploadFile = new File(SecureFileServer.UPLOAD_DIR, "attack_script.txt");

        boolean notEscaped = !outsideFile.exists() && !parentDirFile.exists();
        boolean savedSafelyInsideUploads = insideUploadFile.exists();

        if (res4.startsWith("OK") && notEscaped && savedSafelyInsideUploads) {
            System.out.println("✔ PASS CA 4: Chặn thành công Path Traversal! File bị ép lưu an toàn vào data/uploads/attack_script.txt, không thoát ra ngoài.");
            passed++;
        } else {
            System.err.println("✘ FAIL CA 4: Path traversal khong duoc xu ly an toan: " + res4);
        }

        System.out.println("\n----------------------------------------------------------------------");
        System.out.println("KẾT QUẢ KIỂM THỬ BÀI 8: ĐẠT " + passed + "/" + total + " CA KIỂM THỬ.");
        System.out.println("======================================================================");
        System.exit(0);
    }
}
