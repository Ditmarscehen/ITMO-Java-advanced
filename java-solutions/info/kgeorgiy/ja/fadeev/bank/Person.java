package info.kgeorgiy.ja.fadeev.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentMap;

public interface Person extends Remote {
    String getName() throws RemoteException;

    String getSurname() throws RemoteException;

    String getPassport() throws RemoteException;

    ConcurrentMap<String, Account> getAccounts() throws RemoteException;

    Account createAccount(String id) throws RemoteException;
}
