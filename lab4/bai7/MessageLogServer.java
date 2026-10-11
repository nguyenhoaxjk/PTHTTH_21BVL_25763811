package bai7;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class MessageLogServer {
    public static final int DEFAULT_PORT = 5007;
    private static final int MAX_CLIENTS = 20;
    private static final String LOG_DIR = "data" + File.separator + "logs";
    private static final Pattern CLIENT_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        startServer(port);
    }

    public static void startServer(int port) {
        // Đảm bảo thư mục lưu log tồn tại
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        ExecutorService pool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Message Log Server dang lang nghe tai port " + port);
            System.out.println("Thu muc luu tru log: " + dir.getAbsolutePath());

            while (true) {
                Socket socket = server.accept();
                pool.submit(() -> handleClient(socket));
            }
        } catch (IOException e) {
            System.err.println("Khong the khoi dong Log Server: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    private static void handleClient(Socket socket) {
        String remoteAddress = String.valueOf(socket.getRemoteSocketAddress());
        System.out.println("Ket noi moi tu: " + remoteAddress);

        String clientId = null;
        File logFile = null;

        try (Socket clientSocket = socket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            // Bước 1: Yêu cầu bắt buộc client phải gửi HELLO <clientId> trước
            out.println("INFO Vui long gui 'HELLO <clientId>' de bat dau phien lam viec.");

            while (true) {
                String line = in.readLine();
                if (line == null) {
                    return; // Client đóng kết nối
                }

                String trimmed = line.trim();
                if (trimmed.equalsIgnoreCase("QUIT")) {
                    out.println("OK BYE");
                    return;
                }

                if (trimmed.toUpperCase().startsWith("HELLO ")) {
                    String candidateId = trimmed.substring(6).trim();

                    // Kiểm tra clientId chỉ gồm chữ, số, gạch ngang, gạch dưới
                    if (CLIENT_ID_PATTERN.matcher(candidateId).matches()) {
                        clientId = candidateId;
                        logFile = new File(LOG_DIR, clientId + ".txt");
                        out.println("OK HELLO " + clientId);
                        System.out.println("Client xac thuc thanh cong clientId: " + clientId + " [" + remoteAddress + "]");
                        break;
                    } else {
                        out.println("ERR INVALID_CLIENT_ID (Chi chap nhan chu, so, dau '-' va '_')");
                    }
                } else {
                    out.println("ERR MUST_HELLO_FIRST (Cu phap: HELLO <clientId>)");
                }
            }

            // Bước 2: Nhận từng tin nhắn và lưu vào file log tương ứng
            String message;
            while ((message = in.readLine()) != null) {
                String trimmed = message.trim();

                // Kết thúc bằng QUIT thay cho chuỗi HET nằm lẫn trong nội dung
                if (trimmed.equalsIgnoreCase("QUIT")) {
                    out.println("OK BYE");
                    break;
                }

                if (trimmed.isEmpty()) {
                    continue;
                }

                // Ghi vào file log: [timestamp] [remoteAddress] nội_dung
                String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
                String logEntry = String.format("[%s] [%s] %s%n", timestamp, remoteAddress, message);

                // Ghi đồng bộ (thread-safe trên file)
                synchronized (logFile.getPath().intern()) {
                    try (BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(new FileOutputStream(logFile, true), StandardCharsets.UTF_8))) {
                        writer.write(logEntry);
                    }
                }

                out.println("OK LOGGED");
            }

        } catch (IOException e) {
            System.err.println("Client [" + (clientId != null ? clientId : remoteAddress) + "] gap loi: " + e.getMessage());
        } finally {
            System.out.println("Ngat ket noi: [" + (clientId != null ? clientId : remoteAddress) + "]");
        }
    }
}
