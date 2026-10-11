package bai5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final int DEFAULT_PORT = 5000;
    private static final int MAX_CLIENTS = 30;

    // Cấu trúc Thread-safe quản lý danh sách nickname -> PrintWriter gửi dữ liệu
    private static final ConcurrentHashMap<String, PrintWriter> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        ExecutorService pool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("TCP Chat Server dang lang nghe tai port " + port);

            while (true) {
                Socket socket = server.accept();
                pool.submit(() -> handleClient(socket));
            }
        } catch (IOException e) {
            System.err.println("Khong the mo Chat Server: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    private static void handleClient(Socket socket) {
        String clientAddress = String.valueOf(socket.getRemoteSocketAddress());
        System.out.println("Client moi ket noi tu: " + clientAddress);

        String nickname = null;
        try (Socket clientSocket = socket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            // Bước 1: Yêu cầu client chọn nickname duy nhất
            out.println("INFO Vui long nhap nickname (Cu phap: NICK <nickname>):");
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    return; // Client ngắt trước khi đăng ký
                }

                String trimmed = line.trim();
                if (trimmed.equalsIgnoreCase("QUIT")) {
                    out.println("OK BYE");
                    return;
                }

                if (trimmed.toUpperCase().startsWith("NICK ")) {
                    String requestedNick = trimmed.substring(5).trim();
                    if (requestedNick.isEmpty() || requestedNick.contains(" ")) {
                        out.println("ERR INVALID_NICKNAME (Nickname khong duoc chua khoang trang)");
                        continue;
                    }

                    // Kiểm tra và gán nickname nguyên tử (Atomic / Thread-safe)
                    if (clients.putIfAbsent(requestedNick, out) == null) {
                        nickname = requestedNick;
                        out.println("OK WELCOME " + nickname);
                        broadcast("INFO [" + nickname + "] da tham gia phong chat.", nickname);
                        System.out.println("User dang ky thanh cong: " + nickname + " (" + clientAddress + ")");
                        break;
                    } else {
                        out.println("ERR NICKNAME_EXISTS (Nickname nay da co nguoi su dung)");
                    }
                } else {
                    out.println("ERR Vui long dang ky nickname bang lenh: NICK <nickname>");
                }
            }

            // Bước 2: Nhận và xử lý các lệnh chat: USERS, MSG <nội dung>, QUIT
            String request;
            while ((request = in.readLine()) != null) {
                String trimmed = request.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                if (trimmed.equalsIgnoreCase("QUIT")) {
                    out.println("OK BYE");
                    break;
                } else if (trimmed.equalsIgnoreCase("USERS")) {
                    String userList = String.join(", ", clients.keySet());
                    out.println("OK USERS (" + clients.size() + "): " + userList);
                } else if (trimmed.toUpperCase().startsWith("MSG ")) {
                    String message = trimmed.substring(4).trim();
                    if (message.isEmpty()) {
                        out.println("ERR EMPTY_MESSAGE");
                    } else {
                        broadcast("MSG [" + nickname + "]: " + message, nickname);
                        out.println("OK SENT");
                    }
                } else {
                    out.println("ERR UNKNOWN_COMMAND (Ho tro: USERS, MSG <noi_dung>, QUIT)");
                }
            }
        } catch (IOException e) {
            System.err.println("Client [" + (nickname != null ? nickname : clientAddress) + "] mat ket noi dot ngot: " + e.getMessage());
        } finally {
            // Bước 3: Dọn dẹp tài nguyên và loại bỏ client khi ngắt kết nối
            if (nickname != null) {
                clients.remove(nickname);
                broadcast("INFO [" + nickname + "] da roi phong chat.", nickname);
                System.out.println("User da roi phong: " + nickname);
            }
        }
    }

    // Broadcast thông điệp tới tất cả các client còn lại (trừ người gửi excludeNick)
    private static void broadcast(String message, String excludeNick) {
        for (Map.Entry<String, PrintWriter> entry : clients.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase(excludeNick)) {
                try {
                    entry.getValue().println(message);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
