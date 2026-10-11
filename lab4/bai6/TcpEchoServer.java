package bai6;

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

public class TcpEchoServer {
    public static final int PORT = 6000;
    private static volatile boolean running = true;

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : PORT;
        startServer(port);
    }

    public static void startServer(int port) {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("TCP Echo Server (Benchmark) dang lang nghe tai port " + port);

            while (running) {
                try {
                    Socket socket = server.accept();
                    pool.submit(() -> handleClient(socket));
                } catch (IOException e) {
                    if (!running) break;
                }
            }
        } catch (IOException e) {
            System.err.println("TCP Echo Server loi: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    private static void handleClient(Socket socket) {
        try (Socket clientSocket = socket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("QUIT")) {
                    break;
                }
                out.println(line); // Echo lại đúng nội dung
            }
        } catch (IOException ignored) {
        }
    }

    public static void stopServer() {
        running = false;
    }
}
