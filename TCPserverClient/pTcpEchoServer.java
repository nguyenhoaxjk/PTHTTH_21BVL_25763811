import java.net.ServerSocket;
import java.net.Socket;

public class pTcpEchoServer {
    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(6789); // bind(); listen()
            System.out.println("SERVER SONG SONG da duoc tao");
            while (true) {
                Socket s = ss.accept();

                t_Processing tp = new t_Processing(s);
                tp.start();

                // s.close(); // Không đóng ở đây vì đã giao cho Thread t_Processing xử lý
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
