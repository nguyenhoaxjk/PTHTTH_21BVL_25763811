import java.io.Serializable;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private double startingPrice;
    private double highestBid;
    private String highestBidder;

    public Product(int id, String name, double startingPrice) {
        this.id = id;
        this.name = name;
        this.startingPrice = startingPrice;
        this.highestBid = startingPrice;
        this.highestBidder = "Chua co";
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public double getHighestBid() {
        return highestBid;
    }

    public String getHighestBidder() {
        return highestBidder;
    }

    public void setHighestBid(double highestBid) {
        this.highestBid = highestBid;
    }

    public void setHighestBidder(String highestBidder) {
        this.highestBidder = highestBidder;
    }

    @Override
    public String toString() {
        return "ID: " + id + " | San pham: " + name + 
               " | Gia khoi diem: " + startingPrice + 
               " | Gia cao nhat: " + highestBid + 
               " (Nguoi tra: " + highestBidder + ")";
    }
}
