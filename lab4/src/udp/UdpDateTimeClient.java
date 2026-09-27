package udp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class UdpDateTimeClient {
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5001;

        InetAddress serverAddress = InetAddress.getByName(host);
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

        try (DatagramSocket socket = new DatagramSocket()) {
            // Thiet lap timeout 3 giay de tranh treo chuong trinh vo han neu server dung
            socket.setSoTimeout(3000);

            System.out.println("Connected to UDP DateTime Client (" + host + ":" + port + ").");
            System.out.println("Available commands: DATE, TIME, DATETIME (type QUIT to exit client)");

            String command;
            while ((command = console.readLine()) != null) {
                String trimmed = command.trim();
                if (trimmed.equalsIgnoreCase("QUIT") || trimmed.equalsIgnoreCase("EXIT")) {
                    System.out.println("Client closed.");
                    break;
                }

                if (trimmed.isEmpty()) {
                    continue;
                }

                byte[] sendData = trimmed.getBytes(StandardCharsets.UTF_8);
                DatagramPacket sendPacket = new DatagramPacket(
                        sendData,
                        sendData.length,
                        serverAddress,
                        port
                );
                socket.send(sendPacket);

                byte[] receiveBuffer = new byte[4096];
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
                    System.err.println("Loi: Het 3 giay khong nhan duoc phan hoi tu server (Server co the da dung hoac sai port).");
                }
            }
        }
    }
}
