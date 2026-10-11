package bai6;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BenchmarkRunner {
    private static final String HOST = "localhost";
    private static final int TCP_PORT = 6000;
    private static final int UDP_PORT = 6001;

    private static final int NUM_MESSAGES = 1000;
    private static final int NUM_ROUNDS = 5;
    private static final int PAYLOAD_SIZE = 512; // Kích thước thông điệp: 512 bytes
    private static final int UDP_TIMEOUT_MS = 1000;

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================================");
        System.out.println("THỰC NGHIỆM SO SÁNH HIỆU NĂNG TCP VÀ UDP (BÀI 6 - LAB 4)");
        System.out.println("Môi trường: " + System.getProperty("os.name") + " | Java: " + System.getProperty("java.version"));
        System.out.println("Kích thước thông điệp: " + PAYLOAD_SIZE + " bytes");
        System.out.println("Số lượng thông điệp mỗi lần: " + NUM_MESSAGES);
        System.out.println("Số lần lặp lại: " + NUM_ROUNDS);
        System.out.println("Timeout UDP: " + UDP_TIMEOUT_MS + " ms");
        System.out.println("================================================================================\n");

        // Khởi động server trong background nếu chưa chạy
        Thread tcpServerThread = new Thread(() -> TcpEchoServer.startServer(TCP_PORT));
        tcpServerThread.setDaemon(true);
        tcpServerThread.start();

        Thread udpServerThread = new Thread(() -> UdpEchoServer.startServer(UDP_PORT));
        udpServerThread.setDaemon(true);
        udpServerThread.start();

        Thread.sleep(500); // Chờ server khởi động

        // Tạo chuỗi thông điệp mẫu có kích thước cố định PAYLOAD_SIZE
        char[] pad = new char[PAYLOAD_SIZE - 10];
        Arrays.fill(pad, 'X');
        String messagePayload = "BENCH_" + new String(pad);

        long[] tcpTimes = new long[NUM_ROUNDS];
        int[] tcpReceived = new int[NUM_ROUNDS];

        long[] udpTimes = new long[NUM_ROUNDS];
        int[] udpReceived = new int[NUM_ROUNDS];

        // 1. Thực nghiệm TCP
        System.out.println(">>> ĐANG ĐO THỰC NGHIỆM TCP (5 LẦN)...");
        for (int round = 0; round < NUM_ROUNDS; round++) {
            Result res = runTcpTest(messagePayload);
            tcpTimes[round] = res.durationMs;
            tcpReceived[round] = res.receivedCount;
            System.out.printf("  [TCP] Lần %d: Thời gian = %4d ms | Nhận = %d/%d (%.1f%%)%n",
                    (round + 1), res.durationMs, res.receivedCount, NUM_MESSAGES,
                    (res.receivedCount * 100.0 / NUM_MESSAGES));
            Thread.sleep(100);
        }

        System.out.println();

        // 2. Thực nghiệm UDP
        System.out.println(">>> ĐANG ĐO THỰC NGHIỆM UDP (5 LẦN)...");
        for (int round = 0; round < NUM_ROUNDS; round++) {
            Result res = runUdpTest(messagePayload);
            udpTimes[round] = res.durationMs;
            udpReceived[round] = res.receivedCount;
            System.out.printf("  [UDP] Lần %d: Thời gian = %4d ms | Nhận = %d/%d (%.1f%%)%n",
                    (round + 1), res.durationMs, res.receivedCount, NUM_MESSAGES,
                    (res.receivedCount * 100.0 / NUM_MESSAGES));
            Thread.sleep(100);
        }

        // 3. Tổng kết thống kê
        printSummaryTable(tcpTimes, tcpReceived, udpTimes, udpReceived);
        System.exit(0);
    }

    private static Result runTcpTest(String message) {
        int received = 0;
        long start = System.nanoTime();

        try (Socket socket = new Socket(HOST, TCP_PORT);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            for (int i = 0; i < NUM_MESSAGES; i++) {
                out.println(message);
                String response = in.readLine();
                if (response != null) {
                    received++;
                }
            }
            out.println("QUIT");
        } catch (Exception e) {
            System.err.println("Lỗi TCP benchmark: " + e.getMessage());
        }

        long durationMs = (System.nanoTime() - start) / 1_000_000;
        return new Result(durationMs, received);
    }

    private static Result runUdpTest(String message) {
        int received = 0;
        byte[] sendData = message.getBytes(StandardCharsets.UTF_8);
        byte[] recvBuffer = new byte[4096];
        long start = System.nanoTime();

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(UDP_TIMEOUT_MS);
            InetAddress address = InetAddress.getByName(HOST);

            for (int i = 0; i < NUM_MESSAGES; i++) {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, UDP_PORT);
                socket.send(sendPacket);

                DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);
                try {
                    socket.receive(recvPacket);
                    received++;
                } catch (SocketTimeoutException ignored) {
                    // Mất gói hoặc timeout
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi UDP benchmark: " + e.getMessage());
        }

        long durationMs = (System.nanoTime() - start) / 1_000_000;
        return new Result(durationMs, received);
    }

    private static void printSummaryTable(long[] tcpTimes, int[] tcpRecv, long[] udpTimes, int[] udpRecv) {
        System.out.println("\n================================================================================");
        System.out.println("BẢNG TỔNG HỢP SO SÁNH THỰC NGHIỆM");
        System.out.println("================================================================================");
        System.out.printf("%-8s | %-15s | %-15s | %-15s | %-15s%n",
                "Lần đo", "TCP Thời gian", "TCP Phản hồi", "UDP Thời gian", "UDP Phản hồi");
        System.out.println("--------------------------------------------------------------------------------");

        long sumTcpTime = 0;
        int sumTcpRecv = 0;
        long sumUdpTime = 0;
        int sumUdpRecv = 0;

        for (int i = 0; i < NUM_ROUNDS; i++) {
            System.out.printf("Lần %-4d | %8d ms      | %8d/1000   | %8d ms      | %8d/1000%n",
                    (i + 1), tcpTimes[i], tcpRecv[i], udpTimes[i], udpRecv[i]);
            sumTcpTime += tcpTimes[i];
            sumTcpRecv += tcpRecv[i];
            sumUdpTime += udpTimes[i];
            sumUdpRecv += udpRecv[i];
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-8s | %8.1f ms      | %8.1f/1000   | %8.1f ms      | %8.1f/1000%n",
                "TRUNG BÌNH",
                (sumTcpTime * 1.0 / NUM_ROUNDS),
                (sumTcpRecv * 1.0 / NUM_ROUNDS),
                (sumUdpTime * 1.0 / NUM_ROUNDS),
                (sumUdpRecv * 1.0 / NUM_ROUNDS));
        System.out.println("================================================================================\n");
    }

    private static class Result {
        final long durationMs;
        final int receivedCount;

        Result(long durationMs, int receivedCount) {
            this.durationMs = durationMs;
            this.receivedCount = receivedCount;
        }
    }
}
