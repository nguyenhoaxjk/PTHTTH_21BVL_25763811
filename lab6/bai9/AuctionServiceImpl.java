import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AuctionServiceImpl extends UnicastRemoteObject implements AuctionService {
    private final Map<Integer, Product> products;
    private int nextId;

    public AuctionServiceImpl() throws RemoteException {
        super();
        this.products = new LinkedHashMap<>();
        this.nextId = 1;
    }

    @Override
    public synchronized int addProduct(String name, double startingPrice) throws RemoteException {
        int id = nextId++;
        Product product = new Product(id, name, startingPrice);
        products.put(id, product);
        System.out.println("[SERVER] Seller da them san pham: " + name + " (ID: " + id + ", Gia khoi diem: " + startingPrice + ")");
        return id;
    }

    @Override
    public synchronized List<Product> getProducts() throws RemoteException {
        return new ArrayList<>(products.values());
    }

    @Override
    public synchronized boolean placeBid(int productId, String bidderName, double bidAmount) throws RemoteException {
        Product product = products.get(productId);
        if (product == null) {
            return false;
        }

        if (bidAmount > product.getHighestBid()) {
            product.setHighestBid(bidAmount);
            product.setHighestBidder(bidderName);
            // Server thong bao khi co nguoi dat gia thau cao hon
            System.out.println("[THONG BAO SERVER] San pham '" + product.getName() + "' (ID: " + productId + 
                               ") co nguoi dat gia cao hon: " + bidAmount + " boi " + bidderName);
            return true;
        }
        return false;
    }
}
