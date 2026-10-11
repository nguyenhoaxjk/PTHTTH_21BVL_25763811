package bai3;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class UdpDateTimeClient {
    private static final int TIMEOUT_MS = 3000;

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5001;

        System.out.println("Dang ket noi toi UDP DateTime Server tai " + host + ":" + port);
        System.out.println("Cac lenh ho tro: DATE, TIME, DATETIME (Nhap EXIT hoac QUIT o console de thoat client)");

        try (DatagramSocket socket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in, "UTF-8")) {

            socket.setSoTimeout(TIMEOUT_MS);
            InetAddress serverAddress = InetAddress.getByName(host);
            byte[] receiveBuffer = new byte[4096];

            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String command = scanner.nextLine().trim();
                if (command.equalsIgnoreCase("EXIT") || command.equalsIgnoreCase("QUIT")) {
                    System.out.println("Thoat UDP Client.");
                    break;
                }
                if (command.isEmpty()) {
                    continue;
                }

                byte[] sendData = command.getBytes(StandardCharsets.UTF_8);
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
                socket.send(sendPacket);

                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                try {
                    socket.receive(receivePacket);
                    String response = new String(
                            receivePacket.getData(),
                            receivePacket.getOffset(),
                            receivePacket.getLength(),
                            StandardCharsets.UTF_8
                    );
                    System.out.println("Server: " + response);
                } catch (SocketTimeoutException e) {
                    System.err.println("Het 3 giay nhung khong nhan duoc phan hoi (Server co the da dung hoac chua chay).");
                }
            }
        } catch (Exception e) {
            System.err.println("Loi UDP Client: " + e.getMessage());
        }
    }
}
