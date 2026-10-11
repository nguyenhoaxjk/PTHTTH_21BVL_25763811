package bai7;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class MessageLogTest {
    private static final int TEST_PORT = 5007;

    public static void main(String[] args) throws Exception {
        System.out.println("==========================================================");
        System.out.println("BỘ KIỂM THỬ TỰ ĐỘNG BÀI 7 - MESSAGE LOG SERVER");
        System.out.println("==========================================================\n");

        // Khởi động server ngầm
        Thread serverThread = new Thread(() -> MessageLogServer.startServer(TEST_PORT));
        serverThread.setDaemon(true);
        serverThread.start();
        Thread.sleep(500); // Chờ server sẵn sàng

        int passed = 0;
        int total = 0;

        // Test Case 1: Gửi tin nhắn trước khi HELLO
        total++;
        try (Socket s = new Socket("localhost", TEST_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

            in.readLine(); // Đọc lời chào
            out.println("Xin chao");
            String resp = in.readLine();
            if (resp.startsWith("ERR MUST_HELLO_FIRST")) {
                System.out.println("✔ Test 1 PASS: Chặn gửi tin khi chưa HELLO: " + resp);
                passed++;
            } else {
                System.err.println("✘ Test 1 FAIL: Mong muốn ERR MUST_HELLO_FIRST nhưng nhận: " + resp);
            }
        }

        // Test Case 2: clientId chứa ký tự không hợp lệ (ví dụ chứa @, dấu chấm, dấu /)
        total++;
        try (Socket s = new Socket("localhost", TEST_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

            in.readLine();
            out.println("HELLO client@123");
            String resp = in.readLine();
            if (resp.startsWith("ERR INVALID_CLIENT_ID")) {
                System.out.println("✔ Test 2 PASS: Chặn clientId chứa ký tự đặc biệt (@, /): " + resp);
                passed++;
            } else {
                System.err.println("✘ Test 2 FAIL: Mong muốn ERR INVALID_CLIENT_ID nhưng nhận: " + resp);
            }
        }

        // Test Case 3: Chặn Path Traversal trong clientId (../../bad)
        total++;
        try (Socket s = new Socket("localhost", TEST_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

            in.readLine();
            out.println("HELLO ../../bad");
            String resp = in.readLine();
            if (resp.startsWith("ERR INVALID_CLIENT_ID")) {
                System.out.println("✔ Test 3 PASS: Chặn clientId chứa Path Traversal: " + resp);
                passed++;
            } else {
                System.err.println("✘ Test 3 FAIL: Mong muốn ERR INVALID_CLIENT_ID nhưng nhận: " + resp);
            }
        }

        // Test Case 4: clientId hợp lệ, ghi log tiếng Việt và nội dung chứa từ 'HET'
        total++;
        String validClient = "device_test-01";
        try (Socket s = new Socket("localhost", TEST_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

            in.readLine();
            out.println("HELLO " + validClient);
            String resp1 = in.readLine();

            out.println("Tin nhắn số 1: Xin chào server!");
            String resp2 = in.readLine();

            out.println("Tin nhắn số 2: HET TIEN ROI (chứa từ HET nhưng không bị ngắt kết nối)");
            String resp3 = in.readLine();

            out.println("QUIT");
            String resp4 = in.readLine();

            File logFile = new File("data" + File.separator + "logs", validClient + ".txt");
            boolean fileExists = logFile.exists();
            List<String> lines = fileExists ? Files.readAllLines(logFile.toPath(), StandardCharsets.UTF_8) : null;

            if (resp1.startsWith("OK HELLO") && resp2.equals("OK LOGGED") && resp3.equals("OK LOGGED")
                    && resp4.equals("OK BYE") && fileExists && lines.size() >= 2) {
                System.out.println("✔ Test 4 PASS: Ghi log thành công vào data/logs/" + validClient + ".txt:");
                for (String l : lines) {
                    System.out.println("    " + l);
                }
                passed++;
            } else {
                System.err.println("✘ Test 4 FAIL: Có lỗi xảy ra trong quá trình ghi log.");
            }
        }

        System.out.println("\n----------------------------------------------------------");
        System.out.println("KẾT QUẢ KIỂM THỬ BÀI 7: " + passed + "/" + total + " ca đạt.");
        System.out.println("==========================================================");
        System.exit(0);
    }
}
