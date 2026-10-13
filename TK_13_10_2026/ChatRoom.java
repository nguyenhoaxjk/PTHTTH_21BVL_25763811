package TK_13_10_2026;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Lớp đại diện cho 1 phòng chat
public class ChatRoom {
    private String name; // Tên phòng (Study, Game...)
    private List<ClientHandler> members = new CopyOnWriteArrayList<>(); // Danh sách người trong phòng

    public ChatRoom(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getMemberCount() {
        return members.size();
    }

    // Thêm người vào phòng và thông báo cho cả phòng biết
    public void addMember(ClientHandler client) {
        members.add(client);
        broadcast("[Server]: " + client.getClientName() + " da tham gia phong " + name, client);
    }

    // Xóa người khỏi phòng và thông báo cho cả phòng biết
    public void removeMember(ClientHandler client) {
        members.remove(client);
        broadcast("[Server]: " + client.getClientName() + " da roi phong " + name, client);
    }

    // Gửi tin nhắn đến tất cả thành viên trong phòng (trừ người gửi)
    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : members) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }
}
