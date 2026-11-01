import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Client extends UnicastRemoteObject implements ChatService {
    public Client() throws RemoteException {
        super();
    }

    @Override
    public void sendMessage(String message) throws RemoteException {
        System.out.println("\n[Server phan hoi]: " + message);
        System.out.print("Client: ");
    }

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            Client client = new Client();
            registry.rebind("ChatClient", client);

            ChatService serverService = (ChatService) registry.lookup("ChatServer");
            System.out.println("Ket noi Chat Server thanh cong. Nhap tin nhan (go 'exit' de thoat):");

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Client: ");
                String msg = scanner.nextLine();
                if (msg.equalsIgnoreCase("exit")) {
                    serverService.sendMessage("Client da thoat cuoc tro chuyen.");
                    break;
                }
                serverService.sendMessage(msg);
            }
            scanner.close();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
