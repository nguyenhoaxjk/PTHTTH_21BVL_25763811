package bai7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MessageLogClient {
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : MessageLogServer.DEFAULT_PORT;

        try (Socket socket = new Socket(host, port);
             BufferedReader console = new BufferedReader(
                     new InputStreamReader(System.in, StandardCharsets.UTF_8));
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            System.out.println("=== KET NOI THANH CONG TOI MESSAGE LOG SERVER (" + host + ":" + port + ") ===");

            // Đọc thông báo chào từ Server
            String serverGreeting = in.readLine();
            System.out.println("Server: " + serverGreeting);

            // Giao tiếp với Server
            String inputLine;
            while ((inputLine = console.readLine()) != null) {
                out.println(inputLine);

                String response = in.readLine();
                if (response == null) {
                    System.out.println("Server da dong ket noi.");
                    break;
                }

                System.out.println("Server: " + response);

                if (inputLine.trim().equalsIgnoreCase("QUIT")) {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Loi ket noi toi Log Server: " + e.getMessage());
        }
    }
}
