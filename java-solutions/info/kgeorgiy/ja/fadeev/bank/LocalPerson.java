package info.kgeorgiy.ja.fadeev.bank;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentMap;

public class LocalPerson extends AbstractPerson {

    public LocalPerson(final String name, final String surname, final String passport, final ConcurrentMap<String, Account> accounts) {
        super(name, surname, passport, accounts);
    }

    @Override
    public synchronized Account createAccount(String id) throws RemoteException {
        return getAccounts().putIfAbsent(id, new AccountImpl(id));
    }

}
