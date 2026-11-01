import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            HelloService service = (HelloService) registry.lookup("HelloService");
            String result = service.sayHello();
            System.out.println("Ket qua tu server: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
