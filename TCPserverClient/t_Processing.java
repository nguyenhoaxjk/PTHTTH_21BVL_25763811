import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class t_Processing extends Thread {
    private Socket s;

    // Nhận socket kết nối từ Server truyền sang
    public t_Processing(Socket s) {
        this.s = s;
    }

    // Phương thức run() sẽ chạy riêng biệt trên một luồng độc lập khi gọi
    // tp.start()
    @Override
    public void run() {
        try {
            OutputStream os = s.getOutputStream();
            InputStream is = s.getInputStream();
            int ch = 0;

            // Xử lý Echo dữ liệu nhận từ Client
            while (true) {
                ch = is.read();
                if (ch == -1) {
                    break;
                }
                System.out.println("Thread [" + Thread.currentThread().getName() + "] nhận: " + (char) ch);
                os.write(ch);
            }

            s.close();
            System.out.println("Thread [" + Thread.currentThread().getName() + "] da dong ket noi voi Client.");
        } catch (IOException ie) {
            System.out.println("Loi xu ly Thread: " + ie);
        }
    }
}
