import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class tcpeServer {
    public final static int serverPort = 7;

    public static void main(String[] args) {
        System.out.println("Server dang khoi dong va cho ket noi...");
        try {
            ServerSocket ss = new ServerSocket(serverPort);
            System.out.println("server da được tạo");
            while (true) {
                try {

                    Socket s = ss.accept();
                    OutputStream os = s.getOutputStream();
                    InputStream is = s.getInputStream();
                    int ch = 0;
                    while (true) {
                        ch = is.read();
                        if (ch == -1)
                            break;
                        System.out.println((char) ch);
                        os.write(ch);
                    }
                    s.close();

                } catch (IOException ie1) {
                    System.out.println("Connection Error: " + ie1);
                }
            }

        } catch (IOException ie) {
            System.out.println("server creation Error" + ie);
        }

    }
}