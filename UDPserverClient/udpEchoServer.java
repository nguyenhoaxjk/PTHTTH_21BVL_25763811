import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class udpEchoServer {
    public static void main(String[] args) {
        try {
            DatagramSocket ds = new DatagramSocket(6789);
            System.out.println("UDP SERVER da duoc tao");
            while (true) {
                for (int i = 0; i < 10; i++) {
                    byte[] receiveBuffer = new byte[1024];

                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    ds.receive(receivePacket);

                    // lay dlieu trong receivePacket --->...
                    String receiveData = new String(receivePacket.getData(), receivePacket.getOffset(),
                            receivePacket.getLength());
                    System.out.println("Server nhan: " + receiveData);

                    // xu ly???
                    // tao goi tin chua dliey sendPacket to client
                    byte[] sendBuffer = receiveData.getBytes();
                    DatagramPacket dp = new DatagramPacket(sendBuffer, sendBuffer.length, receivePacket.getAddress(),
                            receivePacket.getPort());
                    ds.send(dp);

                }
                System.out.println("Server da phan hoi du 10 goi tin ");
            }

        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
