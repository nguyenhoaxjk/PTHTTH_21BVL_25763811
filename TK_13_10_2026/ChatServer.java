package TK_13_10_2026;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Lớp Server chính: mở cổng và quản lý danh sách phòng
public class ChatServer {
    private static final int PORT = 5000;

    // Danh sách các phòng: Tên phòng -> Đối tượng ChatRoom
    private static final Map<String, ChatRoom> rooms = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        // Tạo sẵn 3 phòng mặc định theo đề bài
        rooms.put("Study", new ChatRoom("Study"));
        rooms.put("Game", new ChatRoom("Game"));
        rooms.put("TraiNghiem", new ChatRoom("TraiNghiem"));

        System.out.println("=== TCP CHAT SERVER DANG KHOI DONG ===");
        System.out.println("Server lang nghe tai cong: " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client moi ket noi tu: " + socket.getRemoteSocketAddress());
                // Mỗi client tạo 1 Thread riêng để phục vụ
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            System.err.println("Loi Server: " + e.getMessage());
        }
    }

    // In danh sách các phòng để Client chọn
    public static synchronized String getRoomListText() {
        StringBuilder sb = new StringBuilder();
        int idx = 1;
        for (String roomName : rooms.keySet()) {
            ChatRoom room = rooms.get(roomName);
            sb.append(idx++).append(". ").append(roomName)
              .append(" (").append(room.getMemberCount()).append(" nguoi)\n");
        }
        return sb.toString().trim();
    }

    // Tìm phòng theo số thứ tự (1, 2, 3) hoặc tên phòng (nếu chưa có thì tự tạo mới)
    public static synchronized ChatRoom getOrCreateRoom(String input) {
        List<String> roomNames = new ArrayList<>(rooms.keySet());
        try {
            int num = Integer.parseInt(input);
            if (num >= 1 && num <= roomNames.size()) {
                return rooms.get(roomNames.get(num - 1));
            }
        } catch (NumberFormatException ignored) {}

        // Người dùng nhập tên phòng mới -> tự động tạo phòng mới
        rooms.putIfAbsent(input, new ChatRoom(input));
        return rooms.get(input);
    }
}
