import java.net.ServerSocket;
import java.net.Socket;

public class pTcpEchoServer {
    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(6789);
            System.out.println("SERVER SONG SONG da duoc tao");
            while (true) {
                Socket s = ss.accept();

                t_Processing tp = new t_Processing(s);
                tp.start();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
