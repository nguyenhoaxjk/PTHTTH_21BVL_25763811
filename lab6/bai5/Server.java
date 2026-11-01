import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Server extends UnicastRemoteObject implements ChatService {
    private static final Scanner scanner = new Scanner(System.in);
    private static Registry registry;

    public Server() throws RemoteException {
        super();
    }

    @Override
    public void sendMessage(String message) throws RemoteException {
        System.out.println("\n[Client]: " + message);
        try {
            ChatService clientService = (ChatService) registry.lookup("ChatClient");
            System.out.print("[Server phan hoi]: ");
            String reply;
            if (scanner.hasNextLine()) {
                reply = scanner.nextLine();
            } else {
                reply = "Server da nhan: " + message;
            }
            clientService.sendMessage(reply);
        } catch (Exception e) {
            System.err.println("Chua the phan hoi den Client: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            try {
                registry = LocateRegistry.createRegistry(1099);
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry(1099);
            }
            Server server = new Server();
            registry.rebind("ChatServer", server);
            System.out.println("Server Bai 5 da san sang. Dang cho tin nhan tu Client...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
