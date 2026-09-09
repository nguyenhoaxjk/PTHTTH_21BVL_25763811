
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

public class viDu3 {

    public static void main(String[] args) {
        InputStream is = System.in;
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        while (true) {
            System.out.print("Xin moi nhap note: ");
            try {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                System.out.println(line);
            } catch (IOException e) {
                System.out.println(e);
            }
        }

    }

}
