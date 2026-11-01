import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class ContactServiceImpl extends UnicastRemoteObject implements ContactService {
    private final HashMap<String, String> contacts;

    public ContactServiceImpl() throws RemoteException {
        super();
        this.contacts = new HashMap<>();
    }

    @Override
    public synchronized void addContact(String name, String phone) throws RemoteException {
        contacts.put(name, phone);
    }

    @Override
    public synchronized String findContact(String name) throws RemoteException {
        return contacts.get(name);
    }

    @Override
    public synchronized boolean deleteContact(String name) throws RemoteException {
        return contacts.remove(name) != null;
    }
}
