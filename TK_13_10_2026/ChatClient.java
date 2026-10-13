package TK_13_10_2026;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        System.out.println("Dang ket noi toi Chat Server (" + SERVER_HOST + ":" + SERVER_PORT + ")...");

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
             Scanner scanner = new Scanner(System.in, "UTF-8")) {

            System.out.println("Ket noi thanh cong!\n");

            // 1. Nhập tên người dùng
            System.out.print("Nhap ten cua ban: ");
            String name = scanner.nextLine().trim();
            while (name.isEmpty()) {
                System.out.print("Ten khong duoc de trong, vui long nhap lai: ");
                name = scanner.nextLine().trim();
            }
            out.println(name);

            // Luồng phụ: Liên tục nhận tin nhắn từ Server và in ra màn hình
            Thread receiveThread = new Thread(() -> {
                try {
                    String serverMsg;
                    while ((serverMsg = in.readLine()) != null) {
                        System.out.println(serverMsg);
                    }
                } catch (Exception e) {
                    System.out.println("\n[Thong bao]: Da ngat ket noi voi Server.");
                }
            });
            receiveThread.setDaemon(true);
            receiveThread.start();

            // Luồng chính: Đọc dữ liệu người dùng nhập từ bàn phím và gửi lên Server
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                out.println(input);

                // Nếu người dùng nhập /exit thì thoát chương trình
                if (input.equalsIgnoreCase("/exit")) {
                    System.out.println("Dang thoat khoi chuong trinh...");
                    try {
                        Thread.sleep(500); // Đợi server phản hồi trước khi đóng
                    } catch (InterruptedException ignored) {}
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Loi ket noi: " + e.getMessage());
        }
    }
}
