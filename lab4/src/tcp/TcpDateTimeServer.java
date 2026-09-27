package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpDateTimeServer {
    private static final int DEFAULT_PORT = 5000;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        ExecutorService pool = Executors.newFixedThreadPool(20);

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("TCP DateTime Server listening on port " + port);

            while (true) {
                Socket socket = server.accept();
                pool.submit(() -> {
                    String clientAddress = String.valueOf(socket.getRemoteSocketAddress());
                    System.out.println("Connected: " + clientAddress);
                    try (socket) {
                        serve(socket);
                    } catch (IOException e) {
                        System.err.println("Client " + clientAddress + " error: " + e.getMessage());
                    } finally {
                        System.out.println("Disconnected: " + clientAddress);
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("Khong mo duoc server: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    private static void serve(Socket socket) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(
                socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String request;
            while ((request = in.readLine()) != null) {
                String response = process(request);
                out.println(response);
                if (request.trim().equalsIgnoreCase("QUIT")) {
                    break;
                }
            }
        }
    }

    static String process(String request) {
        if (request == null) {
            return "ERR UNKNOWN_COMMAND";
        }

        String cmd = request.trim().toUpperCase();
        switch (cmd) {
            case "DATE":
                return "OK " + LocalDate.now().format(DATE_FORMAT);
            case "TIME":
                return "OK " + LocalTime.now().format(TIME_FORMAT);
            case "DATETIME":
                return "OK " + LocalDateTime.now().format(DATETIME_FORMAT);
            case "QUIT":
                return "OK BYE";
            default:
                return "ERR UNKNOWN_COMMAND";
        }
    }
}
