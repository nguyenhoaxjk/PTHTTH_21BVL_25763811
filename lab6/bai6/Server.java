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
            BankAccountService service = new BankAccountServiceImpl(1000.0);
            registry.rebind("BankAccountService", service);
            System.out.println("Server Bai 6 da san sang voi so du ban dau: 1000.0");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
