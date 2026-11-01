import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface AuctionService extends Remote {
    int addProduct(String name, double startingPrice) throws RemoteException;
    List<Product> getProducts() throws RemoteException;
    boolean placeBid(int productId, String bidderName, double bidAmount) throws RemoteException;
}
