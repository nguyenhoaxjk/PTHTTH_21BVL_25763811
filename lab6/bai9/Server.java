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
            AuctionService service = new AuctionServiceImpl();
            registry.rebind("AuctionService", service);
            System.out.println("Auction Server Bai 9 da san sang.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
