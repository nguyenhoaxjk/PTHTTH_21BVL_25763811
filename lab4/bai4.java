import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class bai4 {
    private static final int DEFAULT_PORT = 5000;
    private static final int MAX_CLIENTS = 20;

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("client")) {
            runClient(args);
        } else if (args.length > 0 && args[0].equalsIgnoreCase("test")) {
            runTests();
        } else {
            runServer(args);
        }
    }

    private static void runTests() {
        String[] cases = {
            "CALC + 100 200",
            "CALC / 10 0",
            "CALC % 10 3",
            "CALC + a 2",
            "CALC + 100",
            "CALC - 50 20",
            "CALC * 7 8",
            "CALC / 15 2",
            "QUIT"
        };
        for (String c : cases) {
            System.out.println(c + " => " + processRequest(c));
        }
    }

    // ==========================================
    // TCP SERVER (Máy tính từ xa - Multi-client)
    // ==========================================
    private static void runServer(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }

        ExecutorService pool = Executors.newFixedThreadPool(MAX_CLIENTS);
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Calculator Server listening on port " + port);

            while (true) {
                Socket socket = server.accept();
                pool.submit(() -> {
                    String clientAddr = String.valueOf(socket.getRemoteSocketAddress());
                    System.out.println("Connected: " + clientAddr);
                    try (Socket clientSocket = socket) {
                        serveClient(clientSocket);
                    } catch (IOException e) {
                        System.err.println("Client " + clientAddr + " error: " + e.getMessage());
                    } finally {
                        System.out.println("Disconnected: " + clientAddr);
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("Khong the khoi dong server: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    private static void serveClient(Socket socket) throws IOException {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                String response = processRequest(line);
                out.println(response);
                if (line.trim().equalsIgnoreCase("QUIT")) {
                    break;
                }
            }
        }
    }

    // ==========================================
    // XỬ LÝ GIAO THỨC CALC VÀ PHÂN TÍCH LỖI
    // ==========================================
    public static String processRequest(String request) {
        if (request == null) {
            return "ERR INVALID_FORMAT";
        }

        String trimmed = request.trim();
        if (trimmed.equalsIgnoreCase("QUIT")) {
            return "OK BYE";
        }

        String[] parts = trimmed.split("\\s+");
        if (parts.length != 4 || !parts[0].equalsIgnoreCase("CALC")) {
            return "ERR INVALID_FORMAT";
        }

        String op = parts[1];
        if (!op.equals("+") && !op.equals("-") && !op.equals("*") && !op.equals("/")) {
            return "ERR UNSUPPORTED_OPERATOR";
        }

        double num1;
        double num2;
        try {
            num1 = Double.parseDouble(parts[2]);
            num2 = Double.parseDouble(parts[3]);
        } catch (NumberFormatException e) {
            return "ERR INVALID_NUMBER";
        }

        if (op.equals("/") && num2 == 0) {
            return "ERR DIVIDE_BY_ZERO";
        }

        double result;
        switch (op) {
            case "+":
                result = num1 + num2;
                break;
            case "-":
                result = num1 - num2;
                break;
            case "*":
                result = num1 * num2;
                break;
            case "/":
                result = num1 / num2;
                break;
            default:
                return "ERR UNSUPPORTED_OPERATOR";
        }

        return "OK " + formatResult(result);
    }

    private static String formatResult(double val) {
        if (val == (long) val) {
            return String.valueOf((long) val);
        }
        return String.valueOf(val);
    }

    // ==========================================
    // TCP CLIENT (Kiểm thử tương tác)
    // ==========================================
    private static void runClient(String[] args) {
        String host = args.length > 1 ? args[1] : "localhost";
        int port = args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_PORT;

        try (Socket socket = new Socket(host, port);
             BufferedReader console = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));
             BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            System.out.println("Connected to Calculator Server at " + host + ":" + port);
            System.out.println("Enter command (e.g. CALC + 100 200) or QUIT to exit:");

            String line;
            while ((line = console.readLine()) != null) {
                out.println(line);
                String response = in.readLine();
                if (response == null) {
                    System.out.println("Server closed connection");
                    break;
                }
                System.out.println("Server: " + response);
                if (line.trim().equalsIgnoreCase("QUIT")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Loi ket noi client: " + e.getMessage());
        }
    }
}
