package bai10;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bài 10: DiscoveryClient
 * Khám phá dịch vụ qua UDP Broadcast và kết nối tới dịch vụ TCP tìm được.
 * Yêu cầu:
 * - Gửi broadcast "DISCOVER_SERVICE".
 * - Thiết lập timeout khi chờ phản hồi.
 * - Loại bỏ các phản hồi trùng lặp (Deduplication).
 * - Hiển thị danh sách tất cả các server được tìm thấy.
 * - Dùng địa chỉ IP nguồn từ Datagram phản hồi và tcp_port để kết nối tới TCP server.
 */
public class DiscoveryClient {
    public static final String DEFAULT_BROADCAST_IP = "255.255.255.255";
    public static final int DEFAULT_DISCOVERY_PORT = 5010;
    public static final int DEFAULT_TIMEOUT_MS = 2500;

    public static void main(String[] args) {
        String broadcastIp = args.length > 0 ? args[0] : DEFAULT_BROADCAST_IP;
        int discoveryPort = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_DISCOVERY_PORT;
        int timeoutMs = args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_TIMEOUT_MS;

        System.out.println("==================================================================");
        System.out.println("DISCOVERY CLIENT - TÌM KIẾM DỊCH VỤ TRÊN MẠNG");
        System.out.printf("Địa chỉ Broadcast: %s | Cổng UDP: %d | Timeout: %d ms%n",
                broadcastIp, discoveryPort, timeoutMs);
        System.out.println("==================================================================\n");

        // 1. Quét tìm kiếm các dịch vụ
        List<DiscoveredService> services = discoverServices(broadcastIp, discoveryPort, timeoutMs);

        // 2. Hiển thị danh sách dịch vụ tìm thấy
        if (services.isEmpty()) {
            System.out.println("Không tìm thấy dịch vụ nào đang hoạt động trong mạng sau timeout.");
            return;
        }

        System.out.println("\n========== DANH SÁCH DỊCH VỤ ĐÃ PHÁT HIỆN ==========");
        for (int i = 0; i < services.size(); i++) {
            DiscoveredService s = services.get(i);
            System.out.printf("[%d] Dịch vụ: %-18s | Địa chỉ: %s:%-5d | Phiên bản: %-6s (Gói tin: %d)%n",
                    (i + 1), s.getServiceName(), s.getIp(), s.getTcpPort(), s.getVersion(), s.getResponseCount());
        }
        System.out.println("====================================================\n");

        // 3. Kết nối TCP tới dịch vụ đầu tiên tìm được để minh họa giao tiếp
        DiscoveredService chosen = services.get(0);
        System.out.printf("-> Tự động kết nối TCP tới dịch vụ [%s] tại %s:%d...%n",
                chosen.getServiceName(), chosen.getIp(), chosen.getTcpPort());

        testTcpConnection(chosen.getIp(), chosen.getTcpPort());
    }

    public static List<DiscoveredService> discoverServices(int discoveryPort, int timeoutMs) {
        return discoverServices(DEFAULT_BROADCAST_IP, discoveryPort, timeoutMs);
    }

    /**
     * Gửi broadcast UDP để tìm kiếm dịch vụ và thu thập phản hồi với timeout & khử trùng.
     * 
     * @param broadcastTarget Địa chỉ broadcast (ví dụ 255.255.255.255 hoặc 127.0.0.1)
     * @param discoveryPort Cổng UDP Discovery của Server
     * @param timeoutMs Thời gian timeout (mili giây)
     * @return Danh sách các dịch vụ duy nhất được phát hiện
     */
    public static List<DiscoveredService> discoverServices(String broadcastTarget, int discoveryPort, int timeoutMs) {
        Map<String, DiscoveredService> serviceMap = new LinkedHashMap<>();
        int duplicateCount = 0;

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(timeoutMs);

            byte[] requestData = "DISCOVER_SERVICE".getBytes(StandardCharsets.UTF_8);

            // Gửi gói tin broadcast DISCOVER_SERVICE
            InetAddress targetAddr = InetAddress.getByName(broadcastTarget);
            DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, targetAddr, discoveryPort);
            socket.send(requestPacket);

            System.out.printf("Đã phát gói tin DISCOVER_SERVICE tới %s:%d. Đang chờ phản hồi (timeout %d ms)...%n",
                    broadcastTarget, discoveryPort, timeoutMs);

            byte[] buffer = new byte[2048];
            long startTime = System.currentTimeMillis();

            // Vòng lặp nhận phản hồi cho tới khi hết timeout
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    // Địa chỉ IP nguồn của server phản hồi
                    String sourceIp = packet.getAddress().getHostAddress();
                    String message = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8).trim();

                    System.out.printf("[NHẬN PHẢN HỒI UDP] Từ %s:%d: \"%s\"%n",
                            sourceIp, packet.getPort(), message);

                    // Parse định dạng: SERVICE tên_dịch_vụ tcp_port phiên_bản
                    DiscoveredService service = parseServiceResponse(message, sourceIp);
                    if (service != null) {
                        // Khóa khử trùng: kết hợp ServiceName và TCP Port (hoặc ServiceName@IP:Port)
                        // Một dịch vụ duy nhất được xác định bởi định danh dịch vụ và cổng phục vụ TCP của nó
                        String key = service.getServiceName() + "@" + service.getTcpPort();

                        if (serviceMap.containsKey(key)) {
                            // Đã tồn tại trong danh sách -> Loại bỏ phản hồi trùng lặp
                            duplicateCount++;
                            serviceMap.get(key).incrementCount();
                            System.out.printf("  ↳ [DEDUPLICATION] Phát hiện phản hồi trùng cho '%s' (TCP Port %d). Đã gộp phản hồi (Tổng: %d).%n",
                                    service.getServiceName(), service.getTcpPort(), serviceMap.get(key).getResponseCount());
                        } else {
                            serviceMap.put(key, service);
                            System.out.printf("  ↳ [MỚI] Đã thêm dịch vụ: '%s' tại %s:%d%n",
                                    service.getServiceName(), service.getIp(), service.getTcpPort());
                        }
                    }
                } catch (SocketTimeoutException te) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    System.out.printf("✔ Quá trình Discovery kết thúc sau %d ms (Timeout). Tìm thấy %d dịch vụ (Đã lọc %d phản hồi trùng).%n",
                            elapsed, serviceMap.size(), duplicateCount);
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi socket Discovery: " + e.getMessage());
        }

        return new ArrayList<>(serviceMap.values());
    }

    /**
     * Phân tích chuỗi phản hồi từ server: "SERVICE tên_dịch_vụ tcp_port phiên_bản"
     */
    public static DiscoveredService parseServiceResponse(String raw, String defaultIp) {
        if (raw == null || !raw.startsWith("SERVICE ")) {
            return null;
        }

        String[] parts = raw.split("\\s+");
        if (parts.length < 4) {
            return null;
        }

        String serviceName = parts[1];
        try {
            int tcpPort = Integer.parseInt(parts[2]);
            String version = parts[3];
            return new DiscoveredService(serviceName, defaultIp, tcpPort, version);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Kết nối TCP tới Server vừa được khám phá bằng IP nguồn và TCP port.
     */
    public static boolean testTcpConnection(String ip, int tcpPort) {
        System.out.printf("--> Mở kết nối TCP Socket tới %s:%d...%n", ip, tcpPort);

        try (Socket socket = new Socket(ip, tcpPort);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            System.out.println("✔ KẾT NỐI TCP THÀNH CÔNG!");

            // 1. Gửi lệnh INFO
            out.println("INFO");
            String infoResp = in.readLine();
            System.out.println("[TCP Server trả lời INFO]: " + infoResp);

            // 2. Gửi lệnh PING
            out.println("PING");
            String pingResp = in.readLine();
            System.out.println("[TCP Server trả lời PING]: " + pingResp);

            // 3. Gửi lệnh tính toán CALC
            out.println("CALC + 150 250");
            String calcResp = in.readLine();
            System.out.println("[TCP Server trả lời CALC]: " + calcResp);

            // 4. Gửi lệnh QUIT để ngắt kết nối văn minh
            out.println("QUIT");
            String quitResp = in.readLine();
            System.out.println("[TCP Server trả lời QUIT]: " + quitResp);

            return true;
        } catch (IOException e) {
            System.err.printf("✘ Thất bại khi kết nối TCP tới %s:%d: %s%n", ip, tcpPort, e.getMessage());
            return false;
        }
    }
}
