package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class UdpDateTimeServer {
    private static final int DEFAULT_PORT = 5001;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        byte[] buffer = new byte[4096];

        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("UDP DateTime Server listening on port " + port);

            while (true) {
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(requestPacket);

                String request = new String(
                        requestPacket.getData(),
                        requestPacket.getOffset(),
                        requestPacket.getLength(),
                        StandardCharsets.UTF_8
                ).trim();

                String response = process(request);
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

                DatagramPacket responsePacket = new DatagramPacket(
                        responseBytes,
                        responseBytes.length,
                        requestPacket.getAddress(),
                        requestPacket.getPort()
                );
                socket.send(responsePacket);
            }
        } catch (IOException e) {
            System.err.println("UDP Server error: " + e.getMessage());
        }
    }

    private static String process(String request) {
        String cmd = request.toUpperCase();
        switch (cmd) {
            case "DATE":
                return "OK " + LocalDate.now().format(DATE_FORMAT);
            case "TIME":
                return "OK " + LocalTime.now().format(TIME_FORMAT);
            case "DATETIME":
                return "OK " + LocalDateTime.now().format(DATETIME_FORMAT);
            default:
                return "ERR UNKNOWN_COMMAND";
        }
    }
}
