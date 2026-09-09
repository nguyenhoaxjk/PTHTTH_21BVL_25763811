import static java.lang.Thread.*;
import java.io.IOException;
import java.io.InputStream;

public class viDu2 {

    public static void main(String[] args) throws InterruptedException, IOException {
        InputStream is = System.in;

        try {
            while (true) {
                if (is.available() > 0) {
                    // (Khi có gõ phím)
                    byte[] buffer = new byte[is.available()];
                    int bytesRead = is.read(buffer);
                    if (bytesRead == -1) {
                        break;
                    }
                    String str = new String(buffer, 0, bytesRead);
                    System.out.print(str);

                } else {
                    // (Khi không gõ phím)
                    System.out.print('.');
                    sleep(100);
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }

    }

}
