package bai6;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpEchoServer {
    public static final int PORT = 6001;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : PORT;
        startServer(port);
    }

    public static void startServer(int port) {
        byte[] buffer = new byte[4096];
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("UDP Echo Server (Benchmark) dang lang nghe tai port " + port);

            while (running) {
                try {
                    DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(requestPacket);

                    // Echo lại nguyên vẹn dữ liệu về địa chỉ và port người gửi
                    DatagramPacket responsePacket = new DatagramPacket(
                            requestPacket.getData(),
                            requestPacket.getOffset(),
                            requestPacket.getLength(),
                            requestPacket.getAddress(),
                            requestPacket.getPort()
                    );
                    socket.send(responsePacket);
                } catch (IOException e) {
                    if (!running) break;
                }
            }
        } catch (IOException e) {
            System.err.println("UDP Echo Server loi: " + e.getMessage());
        }
    }

    public static void stopServer() {
        running = false;
    }
}
