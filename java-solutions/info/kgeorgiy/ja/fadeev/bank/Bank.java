package info.kgeorgiy.ja.fadeev.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentMap;

public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it is not already exists.
     *
     * @param id account id
     * @return created or existing account.
     */


    Account createAccount(String id) throws RemoteException;

    /**
     * Returns account by identifier.
     *
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exists.
     */
    Account getAccount(String id) throws RemoteException;

    LocalPerson getLocalPerson(String passport) throws RemoteException;

    RemotePerson getRemotePerson(String passport) throws RemoteException;

    Person createPerson(String name, String surname, String passport) throws RemoteException;

    ConcurrentMap<String, Account> getAccounts(String passport) throws RemoteException;

    boolean checkPerson(String name, String surname, String passport) throws RemoteException;
}
