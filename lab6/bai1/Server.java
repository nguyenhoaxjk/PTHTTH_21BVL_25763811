import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) {
        try {
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(1099);
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry(1099);
            }
            HelloService service = new HelloServiceImpl();
            registry.rebind("HelloService", service);
            System.out.println("Server Bai 1 da san sang.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
