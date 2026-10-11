package bai3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TcpDateTimeClient {
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5000;

        try (Socket socket = new Socket(host, port);
             BufferedReader console = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));
             BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            System.out.println("Da ket noi toi TCP DateTime Server tai " + host + ":" + port);
            System.out.println("Cac lenh ho tro: DATE, TIME, DATETIME, QUIT");

            String request;
            while ((request = console.readLine()) != null) {
                out.println(request);
                String response = in.readLine();
                if (response == null) {
                    System.out.println("Server da dong ket noi hoac bi dung dot ngot.");
                    break;
                }
                System.out.println("Server: " + response);
                if (request.trim().equalsIgnoreCase("QUIT")) {
                    break;
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Port phai la so nguyen.");
        } catch (IOException e) {
            System.err.println("Loi ket noi TCP: " + e.getMessage());
        }
    }
}
