package info.kgeorgiy.ja.fadeev.bank;


import java.rmi.RemoteException;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

public class RemotePerson extends AbstractPerson {
    private final RemoteFunction<String, Account> getAccount;

    public RemotePerson(final String name, final String surname, final String passport,
                        final ConcurrentMap<String, Account> accounts,
                        final RemoteFunction<String, Account> getAccount) {
        super(name, surname, passport, accounts);
        this.getAccount = getAccount;
    }

    @Override
    public Account createAccount(String id) throws RemoteException {
        Account account = getAccount.apply(id);
        return getAccounts().putIfAbsent(id, account);
    }

}
