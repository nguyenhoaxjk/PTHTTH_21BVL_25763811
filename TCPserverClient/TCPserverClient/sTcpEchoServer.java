import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class sTcpEchoServer {
    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(6789);
            System.out.println("SERVER TUAN TU da duoc tao");
            while (true) {
                Socket s = ss.accept();
                System.out.println("Client da ket noi (Tuan tu)");

                OutputStream os = s.getOutputStream();
                InputStream is = s.getInputStream();
                int ch = 0;
                while (true) {
                    ch = is.read();
                    if (ch == -1) {
                        break;
                    }
                    System.out.println((char) ch);
                    os.write(ch);
                }
                s.close();
                System.out.println("Client da ngat ket noi.");
            }
        } catch (IOException ie) {
            System.out.println("Server Error: " + ie);
        }
    }
}
