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
            PrimeService service = new PrimeServiceImpl();
            registry.rebind("PrimeService", service);
            System.out.println("Server Bai 3 da san sang.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
