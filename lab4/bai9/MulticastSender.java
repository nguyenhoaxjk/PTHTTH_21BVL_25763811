package bai9;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Bài 9: MulticastSender
 * Phát thông báo tới địa chỉ multicast 239.255.0.1 (thuộc phạm vi quản trị cục bộ).
 * Yêu cầu:
 * - Gửi gói tin UTF-8 tới nhóm multicast.
 * - Chỉ định NetworkInterface phát gói tin.
 * - Hỗ trợ cả chế độ dòng lệnh (args) và tương tác Console (nhập từ bàn phím).
 */
public class MulticastSender {
    public static final String DEFAULT_GROUP_IP = "239.255.0.1";
    public static final int DEFAULT_PORT = 5009;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public static void main(String[] args) {
        String groupIp = args.length > 1 ? args[1] : DEFAULT_GROUP_IP;
        int port = args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_PORT;
        String ifName = args.length > 3 ? args[3] : null;

        // Nếu có truyền thông điệp qua args[0]
        if (args.length > 0 && !"-i".equalsIgnoreCase(args[0])) {
            String message = args[0];
            try {
                sendMessage(message, groupIp, port, ifName);
                System.out.println("✔ Đã gửi xong thông điệp đơn lẻ.");
            } catch (IOException e) {
                System.err.println("Lỗi khi gửi thông điệp: " + e.getMessage());
            }
            return;
        }

        // Chế độ tương tác Console
        System.out.println("==================================================================");
        System.out.println("MULTICAST SENDER - PHÁT THÔNG BÁO TỚI NHÓM " + groupIp + ":" + port);
        System.out.println("Nhập nội dung thông báo để phát tới tất cả Receiver trong nhóm.");
        System.out.println("Nhập 'QUIT' để thoát, hoặc 'SHUTDOWN' để yêu cầu các Receiver dừng.");
        System.out.println("==================================================================");

        try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            while (true) {
                System.out.print("\nNhập thông báo > ");
                String line = console.readLine();
                if (line == null || "QUIT".equalsIgnoreCase(line.trim())) {
                    System.out.println("Đang dừng Multicast Sender...");
                    break;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    sendMessage(line.trim(), groupIp, port, ifName);
                } catch (IOException e) {
                    System.err.println("Lỗi khi gửi: " + e.getMessage());
                }

                if ("SHUTDOWN".equalsIgnoreCase(line.trim())) {
                    System.out.println("Đã phát lệnh SHUTDOWN tới nhóm. Thoát sender.");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc console: " + e.getMessage());
        }
    }

    /**
     * Gửi một thông điệp Multicast tới nhóm chỉ định trên NetworkInterface cụ thể.
     */
    public static void sendMessage(String message, String groupIp, int port, String interfaceName) throws IOException {
        InetAddress group = InetAddress.getByName(groupIp);
        NetworkInterface netIf = NetworkInterfaceHelper.getSuitableInterface(interfaceName);

        try (MulticastSocket socket = new MulticastSocket()) {
            // Chỉ định NetworkInterface phát gói tin ra đúng card mạng mà receiver đang lắng nghe
            socket.setNetworkInterface(netIf);
            socket.setTimeToLive(1); // Phạm vi cục bộ trong mạng con / subnet

            byte[] data = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(data, data.length, group, port);

            String timestamp = LocalTime.now().format(TIME_FMT);
            socket.send(packet);

            System.out.printf("[%s] [SENDER] Đã phát tới %s:%d trên card [%s] (%d bytes): \"%s\"%n",
                    timestamp, groupIp, port, netIf.getName(), data.length, message);
        }
    }
}
