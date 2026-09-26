import java.io.*;
import java.net.Socket;

public class tcpeClient2 {
    public final static String serverIP = "127.0.0.1";
    public final static int serverPort = 6789;

    public static void main(String[] args) throws InterruptedException, IOException {
        Socket s = null;
        try {
            s = new Socket(serverIP, serverPort);
            System.out.println("Client 2 da duoc tao va ket noi den Server");

            InputStream is = s.getInputStream();
            OutputStream os = s.getOutputStream();

            // Client 2 gửi các chữ cái từ 'a' đến 'j' để dễ phân biệt với Client 1 (gửi từ
            // '0' đến '9')
            for (int i = 'a'; i <= 'j'; i++) {
                os.write(i);
                int ch = is.read();
                System.out.println("Client 2 nhan phan hoi: " + (char) ch);
                Thread.sleep(2000); // Tạm dừng 2 giây giữa mỗi lần gửi
            }
        } catch (IOException ie) {
            System.out.println("Error: Can NOT create socket: " + ie);
        } finally {
            if (s != null) {
                s.close();
                System.out.println("Client 2 da dong ket noi.");
            }
        }
    }
}
