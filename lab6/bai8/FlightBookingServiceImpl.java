import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class FlightBookingServiceImpl extends UnicastRemoteObject implements FlightBookingService {
    private final Map<String, Integer> flights;

    public FlightBookingServiceImpl() throws RemoteException {
        super();
        flights = new HashMap<>();
        flights.put("VN123", 50);
        flights.put("VJ456", 30);
        flights.put("QH789", 20);
    }

    @Override
    public synchronized int availableSeats(String flightNumber) throws RemoteException {
        if (flightNumber == null) return -1;
        return flights.getOrDefault(flightNumber.toUpperCase(), -1);
    }

    @Override
    public synchronized boolean bookTicket(String flightNumber, int seats) throws RemoteException {
        if (flightNumber == null || seats <= 0) return false;
        String fn = flightNumber.toUpperCase();
        if (!flights.containsKey(fn)) return false;

        int current = flights.get(fn);
        if (current >= seats) {
            flights.put(fn, current - seats);
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean cancelBooking(String flightNumber, int seats) throws RemoteException {
        if (flightNumber == null || seats <= 0) return false;
        String fn = flightNumber.toUpperCase();
        if (!flights.containsKey(fn)) return false;

        flights.put(fn, flights.get(fn) + seats);
        return true;
    }
}
