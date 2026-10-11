package bai5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChatClient {
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5000;

        try {
            Socket socket = new Socket(host, port);
            System.out.println("=== KET NOI THANH CONG TOI CHAT SERVER (" + host + ":" + port + ") ===");
            System.out.println("Huong dan lenh:");
            System.out.println("- Dang ky: NICK <nickname>");
            System.out.println("- Gui tin : MSG <noi dung>");
            System.out.println("- Xem ai online: USERS");
            System.out.println("- Thoat: QUIT\n");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader console = new BufferedReader(
                    new InputStreamReader(System.in, StandardCharsets.UTF_8));

            // Luồng nhận tin từ Server chạy nền
            Thread receiverThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    // Socket đóng khi thoát
                } finally {
                    System.out.println("\n[He thong] Da ngat ket noi toi server.");
                }
            });
            receiverThread.setDaemon(true);
            receiverThread.start();

            // Luồng chính: đọc bàn phím và gửi lên Server
            String line;
            while ((line = console.readLine()) != null) {
                out.println(line);
                if (line.trim().equalsIgnoreCase("QUIT")) {
                    break;
                }
            }

            socket.close();
        } catch (IOException e) {
            System.err.println("Loi ket noi toi server: " + e.getMessage());
        }
    }
}
