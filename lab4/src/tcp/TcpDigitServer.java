package tcp;

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

public class TcpDigitServer {
    private static final int DEFAULT_PORT = 5000;

    // Dung ma Unicode Escape de dam bao 100% khong bao gio bi loi font khi bien dich tren moi he dieu hanh
    private static final String[] DIGIT_WORDS = {
        "kh\u00F4ng", // không
        "m\u1ED9t",  // một
        "hai",        // hai
        "ba",         // ba
        "b\u1ED1n",   // bốn
        "n\u0103m",   // năm
        "s\u00E1u",   // sáu
        "b\u1EA3y",   // bảy
        "t\u00E1m",   // tám
        "ch\u00EDn"   // chín
    };

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        ExecutorService pool = Executors.newFixedThreadPool(20);

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("TCP Digit Server listening on port " + port);

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
                if (request.equalsIgnoreCase("QUIT")) {
                    break;
                }
            }
        }
    }

    static String process(String request) {
        if (request == null) {
            return "ERR INVALID_DIGIT";
        }

        // Lenh thoat
        if (request.equalsIgnoreCase("QUIT")) {
            return "OK BYE";
        }

        // Kiem tra do dai dung 1 ky tu (loai bo chuoi rong, 10, khoang trang " 5 ")
        if (request.length() != 1) {
            return "ERR INVALID_DIGIT";
        }

        char ch = request.charAt(0);
        if (ch >= '0' && ch <= '9') {
            int digit = ch - '0';
            return DIGIT_WORDS[digit];
        }

        return "ERR INVALID_DIGIT";
    }
}
