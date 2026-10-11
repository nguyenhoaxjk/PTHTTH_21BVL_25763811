package bai10;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Bài 10: DiscoveryServer
 * Kết hợp dịch vụ TCP với cơ chế tự động khám phá dịch vụ qua UDP (Zero-configuration).
 * Hoạt động:
 * 1. UDP Discovery Responder: Lắng nghe trên cổng UDP 5010.
 *    Khi nhận DISCOVER_SERVICE, trả về: "SERVICE tên_dịch_vụ tcp_port phiên_bản"
 * 2. TCP Service: Lắng nghe trên tcp_port, phục vụ các yêu cầu từ Client (INFO, PING, CALC, QUIT).
 */
public class DiscoveryServer {
    public static final int DEFAULT_DISCOVERY_PORT = 5010;
    public static final int DEFAULT_TCP_PORT = 6001;
    public static final String DEFAULT_SERVICE_NAME = "EchoService";
    public static final String DEFAULT_VERSION = "1.0.0";

    private final String serviceName;
    private final int tcpPort;
    private final int discoveryUdpPort;
    private final String version;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private DatagramSocket udpSocket;
    private ServerSocket tcpServerSocket;
    private ExecutorService threadPool;
    private Thread udpThread;
    private Thread tcpThread;

    public DiscoveryServer(String serviceName, int tcpPort, int discoveryUdpPort, String version) {
        this.serviceName = serviceName;
        this.tcpPort = tcpPort;
        this.discoveryUdpPort = discoveryUdpPort;
        this.version = version;
    }

    public static void main(String[] args) {
        String name = args.length > 0 ? args[0] : DEFAULT_SERVICE_NAME;
        int tcpPort = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_TCP_PORT;
        int udpPort = args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_DISCOVERY_PORT;
        String version = args.length > 3 ? args[3] : DEFAULT_VERSION;

        DiscoveryServer server = new DiscoveryServer(name, tcpPort, udpPort, version);
        server.start();

        // Đăng ký shutdown hook để dọn dẹp tài nguyên
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }

    /**
     * Khởi động đồng thời cả 2 dịch vụ UDP Discovery và TCP Service.
     */
    public synchronized void start() {
        if (running.get()) return;
        running.set(true);

        threadPool = Executors.newCachedThreadPool();

        // 1. Khởi động luồng UDP Discovery Responder
        startUdpDiscoveryResponder();

        // 2. Khởi động luồng TCP Service
        startTcpService();

        System.out.println("==================================================================");
        System.out.printf("DỊCH VỤ '%s' (v%s) ĐÃ SẴN SÀNG!%n", serviceName, version);
        System.out.printf("- TCP Service:       Port %d%n", tcpPort);
        System.out.printf("- UDP Discovery:     Port %d%n", discoveryUdpPort);
        System.out.println("==================================================================");
    }

    /**
     * Lắng nghe UDP Discovery trên discoveryUdpPort.
     */
    private void startUdpDiscoveryResponder() {
        udpThread = new Thread(() -> {
            try {
                udpSocket = new DatagramSocket(null);
                udpSocket.setReuseAddress(true);
                udpSocket.bind(new InetSocketAddress(discoveryUdpPort));

                byte[] buffer = new byte[2048];
                while (running.get() && !udpSocket.isClosed()) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);

                    String request = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8).trim();

                    // Kiểm tra lệnh discovery: DISCOVER_SERVICE
                    if (request.startsWith("DISCOVER_SERVICE")) {
                        // Định dạng phản hồi yêu cầu: SERVICE tên_dịch_vụ tcp_port phiên_bản
                        String responseText = String.format("SERVICE %s %d %s", serviceName, tcpPort, version);
                        byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);

                        DatagramPacket responsePacket = new DatagramPacket(
                                responseBytes, responseBytes.length, packet.getAddress(), packet.getPort());

                        udpSocket.send(responsePacket);

                        System.out.printf("[DISCOVERY-UDP] Gửi phản hồi tới %s:%d: \"%s\"%n",
                                packet.getAddress().getHostAddress(), packet.getPort(), responseText);
                    }
                }
            } catch (IOException e) {
                if (running.get()) {
                    System.err.printf("[%s] Lỗi UDP Discovery: %s%n", serviceName, e.getMessage());
                }
            }
        }, "UdpDiscovery-" + serviceName);
        udpThread.setDaemon(true);
        udpThread.start();
    }

    /**
     * Lắng nghe các kết nối TCP từ Client trên tcpPort.
     */
    private void startTcpService() {
        tcpThread = new Thread(() -> {
            try {
                tcpServerSocket = new ServerSocket(tcpPort);
                while (running.get() && !tcpServerSocket.isClosed()) {
                    Socket clientSocket = tcpServerSocket.accept();
                    threadPool.submit(() -> handleTcpClient(clientSocket));
                }
            } catch (IOException e) {
                if (running.get()) {
                    System.err.printf("[%s] Lỗi TCP ServerSocket: %s%n", serviceName, e.getMessage());
                }
            }
        }, "TcpService-" + serviceName);
        tcpThread.setDaemon(true);
        tcpThread.start();
    }

    /**
     * Xử lý giao tiếp TCP với một client.
     */
    private void handleTcpClient(Socket socket) {
        String remoteAddress = socket.getRemoteSocketAddress().toString();
        System.out.printf("[TCP-SERVICE] Client %s đã kết nối tới '%s' (port %d)%n",
                remoteAddress, serviceName, tcpPort);

        try (Socket client = socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                String cmd = line.trim();
                System.out.printf("[%s] [TCP %s] Yêu cầu: %s%n", serviceName, remoteAddress, cmd);

                if ("PING".equalsIgnoreCase(cmd)) {
                    out.println("OK PONG");
                } else if ("INFO".equalsIgnoreCase(cmd)) {
                    out.printf("OK SERVICE=%s PORT=%d VERSION=%s STATUS=RUNNING%n", serviceName, tcpPort, version);
                } else if ("TIME".equalsIgnoreCase(cmd)) {
                    out.println("OK " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                } else if (cmd.toUpperCase().startsWith("ECHO ")) {
                    out.println("OK " + cmd.substring(5));
                } else if (cmd.toUpperCase().startsWith("CALC ")) {
                    out.println(handleCalcCommand(cmd.substring(5).trim()));
                } else if ("QUIT".equalsIgnoreCase(cmd)) {
                    out.println("OK BYE");
                    break;
                } else {
                    out.println("ERR UNKNOWN_COMMAND: " + cmd);
                }
            }
        } catch (IOException e) {
            System.err.printf("[%s] [TCP %s] Ngắt kết nối: %s%n", serviceName, remoteAddress, e.getMessage());
        } finally {
            System.out.printf("[TCP-SERVICE] Đã đóng phiên làm việc với client %s%n", remoteAddress);
        }
    }

    private String handleCalcCommand(String expr) {
        try {
            String[] parts = expr.split("\\s+");
            if (parts.length != 3) {
                return "ERR INVALID_CALC_SYNTAX. Cú pháp: CALC <toán_tử> <số1> <số2>";
            }
            String op = parts[0];
            double a = Double.parseDouble(parts[1]);
            double b = Double.parseDouble(parts[2]);

            return switch (op) {
                case "+" -> "OK " + (a + b);
                case "-" -> "OK " + (a - b);
                case "*" -> "OK " + (a * b);
                case "/" -> {
                    if (b == 0) yield "ERR DIVIDE_BY_ZERO";
                    yield "OK " + (a / b);
                }
                default -> "ERR UNSUPPORTED_OPERATOR: " + op;
            };
        } catch (NumberFormatException e) {
            return "ERR INVALID_NUMBER";
        }
    }

    /**
     * Dừng server an toàn và giải phóng tài nguyên.
     */
    public synchronized void stop() {
        if (!running.getAndSet(false)) return;

        System.out.printf("Đang dừng dịch vụ '%s'...%n", serviceName);
        try {
            if (udpSocket != null && !udpSocket.isClosed()) {
                udpSocket.close();
            }
            if (tcpServerSocket != null && !tcpServerSocket.isClosed()) {
                tcpServerSocket.close();
            }
            if (threadPool != null) {
                threadPool.shutdownNow();
            }
            System.out.printf("✔ Dịch vụ '%s' đã dừng hoàn tất.%n", serviceName);
        } catch (IOException e) {
            System.err.printf("Lỗi khi dừng server '%s': %s%n", serviceName, e.getMessage());
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public String getVersion() {
        return version;
    }
}
