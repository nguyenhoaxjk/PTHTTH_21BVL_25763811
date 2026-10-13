package TK_13_10_2026;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

// Lớp phục vụ riêng cho từng Client kết nối đến Server
public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;
    private ChatRoom currentRoom = null; // Phòng hiện tại của client này

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

            // 1. Nhận tên của Client
            clientName = in.readLine();
            if (clientName == null || clientName.trim().isEmpty()) {
                clientName = "User_" + socket.getPort();
            } else {
                clientName = clientName.trim();
            }

            out.println("\n=== CHAO MUNG [" + clientName.toUpperCase() + "] DEN VOI TCP CHAT ROOM ===");

            // 2. Vòng lặp chính: Chọn phòng hoặc Chat
            while (true) {
                // Chọn phòng
                chooseRoom();
                if (currentRoom == null) {
                    break; // Thoát nếu người dùng chọn /exit khi đang ở sảnh
                }

                // Vòng lặp chat trong phòng
                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();

                    if (line.equalsIgnoreCase("/quit")) {
                        // Rời phòng -> về danh sách phòng
                        currentRoom.removeMember(this);
                        out.println("[Server]: Ban da roi phong [" + currentRoom.getName() + "].");
                        currentRoom = null;
                        break;
                    } else if (line.equalsIgnoreCase("/exit")) {
                        // Ngắt kết nối hoàn toàn
                        currentRoom.removeMember(this);
                        out.println("[Server]: Tam biet " + clientName + "!");
                        currentRoom = null;
                        return;
                    } else if (!line.isEmpty()) {
                        // Gửi tin nhắn đến mọi người trong phòng
                        currentRoom.broadcast("[" + clientName + "]: " + line, this);
                    }
                }

                if (line == null) {
                    break; // Client tắt đột ngột
                }
            }
        } catch (Exception e) {
            System.out.println("Client [" + clientName + "] da ngat ket noi.");
        } finally {
            cleanup();
        }
    }

    // Hiển thị danh sách phòng và cho người dùng chọn
    private void chooseRoom() throws Exception {
        while (currentRoom == null) {
            out.println("\n-----------------------------");
            out.println("DANH SACH PHONG CHAT:");
            out.println(ChatServer.getRoomListText());
            out.println("-> Chon so (1, 2, 3...) hoac nhap ten phong moi (Nhap /exit de thoat):");

            String choice = in.readLine();
            if (choice == null || choice.equalsIgnoreCase("/exit")) {
                out.println("[Server]: Tam biet!");
                return;
            }

            choice = choice.trim();
            if (choice.isEmpty()) continue;

            // Vào phòng
            this.currentRoom = ChatServer.getOrCreateRoom(choice);
            this.currentRoom.addMember(this);

            out.println("\n>>> BAN DA VAO PHONG [" + currentRoom.getName() + "] <<<");
            out.println("(Go tin nhan de chat | /quit de doi phong | /exit de thoat)");
        }
    }

    // Gửi tin nhắn riêng đến client này
    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    public String getClientName() {
        return clientName;
    }

    // Dọn dẹp kết nối khi thoát
    private void cleanup() {
        if (currentRoom != null) {
            currentRoom.removeMember(this);
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception ignored) {}
    }
}
