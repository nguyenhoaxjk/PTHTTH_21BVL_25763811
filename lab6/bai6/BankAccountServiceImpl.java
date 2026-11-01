import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class BankAccountServiceImpl extends UnicastRemoteObject implements BankAccountService {
    private double balance;

    public BankAccountServiceImpl(double initialBalance) throws RemoteException {
        super();
        this.balance = initialBalance;
    }

    @Override
    public synchronized double getBalance() throws RemoteException {
        return balance;
    }

    @Override
    public synchronized void deposit(double amount) throws RemoteException {
        if (amount > 0) {
            balance += amount;
        }
    }

    @Override
    public synchronized boolean withdraw(double amount) throws RemoteException {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }
}
