package bai9;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Bài 9: MulticastReceiver
 * Lắng nghe thông báo từ nhóm Multicast (mặc định 239.255.0.1:5009).
 * Yêu cầu:
 * - Chỉ rõ NetworkInterface khi joinGroup.
 * - Rời group (leaveGroup) trước khi đóng socket.
 * - Hỗ trợ chạy nhiều receiver độc lập cùng lúc.
 */
public class MulticastReceiver {
    public static final String DEFAULT_GROUP_IP = "239.255.0.1";
    public static final int DEFAULT_PORT = 5009;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final String receiverId;
    private final String groupIp;
    private final int port;
    private final String interfaceName;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private MulticastSocket socket;
    private SocketAddress groupAddress;
    private NetworkInterface netIf;

    public MulticastReceiver(String receiverId, String groupIp, int port, String interfaceName) {
        this.receiverId = receiverId != null ? receiverId : "Receiver-1";
        this.groupIp = groupIp != null ? groupIp : DEFAULT_GROUP_IP;
        this.port = port > 0 ? port : DEFAULT_PORT;
        this.interfaceName = interfaceName;
    }

    public static void main(String[] args) {
        String receiverId = args.length > 0 ? args[0] : "Receiver-1";
        String groupIp = args.length > 1 ? args[1] : DEFAULT_GROUP_IP;
        int port = args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_PORT;
        String ifName = args.length > 3 ? args[3] : null;

        MulticastReceiver receiver = new MulticastReceiver(receiverId, groupIp, port, ifName);
        receiver.start();
    }

    public void start() {
        running.set(true);
        try {
            // 1. Khởi tạo socket và cho phép dùng lại địa chỉ (reuseAddress = true)
            // Cần thiết để nhiều receiver trên cùng một máy có thể cùng bind vào port multicast
            socket = new MulticastSocket(null);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(port));

            // 2. Xác định NetworkInterface
            netIf = NetworkInterfaceHelper.getSuitableInterface(interfaceName);
            groupAddress = new InetSocketAddress(InetAddress.getByName(groupIp), port);

            System.out.printf("[%s] Đang tham gia nhóm multicast %s:%d trên card mạng [%s - %s]...%n",
                    receiverId, groupIp, port, netIf.getName(), netIf.getDisplayName());

            // 3. Chỉ rõ NetworkInterface khi joinGroup (Chuẩn Java hiện đại)
            socket.joinGroup(groupAddress, netIf);
            System.out.printf("[%s] ✔ ĐÃ THAM GIA THÀNH CÔNG NHÓM MULTICAST %s:%d%n", receiverId, groupIp, port);
            System.out.printf("[%s] Đang chờ nhận thông điệp từ Sender... (Nhấn Ctrl+C để thoát)%n", receiverId);

            // Đăng ký shutdown hook để luôn rời group trước khi đóng ứng dụng
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.printf("%n[%s] Shutdown hook được kích hoạt. Đang rời group và giải phóng tài nguyên...%n", receiverId);
                stop();
            }));

            // 4. Vòng lặp nhận dữ liệu
            byte[] buffer = new byte[4096];
            while (running.get() && !socket.isClosed()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String content = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
                String senderInfo = packet.getAddress().getHostAddress() + ":" + packet.getPort();
                String timestamp = LocalTime.now().format(TIME_FMT);

                System.out.printf("[%s] [%s] NHẬN TỪ [%s]: %s%n", receiverId, timestamp, senderInfo, content);

                // Nếu nhận lệnh dừng SHUTDOWN thì dừng receiver
                if ("SHUTDOWN".equalsIgnoreCase(content.trim()) || "QUIT".equalsIgnoreCase(content.trim())) {
                    System.out.printf("[%s] Nhận lệnh dừng từ hệ thống. Chuẩn bị thoát...%n", receiverId);
                    break;
                }
            }
        } catch (IOException e) {
            if (running.get()) {
                System.err.printf("[%s] Lỗi nhận Multicast: %s%n", receiverId, e.getMessage());
                System.err.println("Ghi chú: Nếu không nhận được gói tin, hãy kiểm tra Firewall Windows hoặc chỉ định card mạng Loopback.");
            }
        } finally {
            stop();
        }
    }

    /**
     * Rời group trước khi đóng socket (Yêu cầu bắt buộc của bài).
     */
    public synchronized void stop() {
        if (!running.getAndSet(false)) {
            return;
        }

        if (socket != null && !socket.isClosed()) {
            try {
                if (groupAddress != null && netIf != null) {
                    System.out.printf("[%s] -> Đang gọi socket.leaveGroup(%s, %s)...%n", receiverId, groupIp, netIf.getName());
                    socket.leaveGroup(groupAddress, netIf);
                    System.out.printf("[%s] ✔ Đã rời khỏi nhóm Multicast an toàn.%n", receiverId);
                }
            } catch (IOException e) {
                System.err.printf("[%s] Lỗi khi rời nhóm Multicast: %s%n", receiverId, e.getMessage());
            } finally {
                socket.close();
                System.out.printf("[%s] ✔ Socket đã được đóng.%n", receiverId);
            }
        }
    }

    public boolean isRunning() {
        return running.get();
    }
}
