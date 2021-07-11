package info.kgeorgiy.ja.fadeev.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    private <T extends Remote> T create(final String id,
                                        final T obj,
                                        final Function<String, T> get,
                                        final ConcurrentMap<String, T> map) throws RemoteException {
        if (map.putIfAbsent(id, obj) == null) {
            UnicastRemoteObject.exportObject(obj, port);
            return obj;
        } else {
            return get.apply(id);
        }
    }

    private Account createAccountInBank(final String id) throws RemoteException {
        return create(id, new AccountImpl(id), this::getAccount, accounts);
    }

    @Override
    public Account createAccount(final String id) throws RemoteException {
        String passport = id.split(":")[0];
        Person person = persons.get(passport);
        return person.createAccount(id);
    }

    @Override
    public Account getAccount(final String id) {
        return accounts.get(id);
    }

    @Override
    public LocalPerson getLocalPerson(String passport) throws RemoteException {
        Person person = persons.get(passport);
        if (person == null) {
            return null;
        }
        ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
        for (Map.Entry<String, Account> entry : person.getAccounts().entrySet()) {
            accounts.put(entry.getKey(), new AccountImpl(entry.getKey(), entry.getValue().getAmount()));
        }
        return new LocalPerson(person.getName(), person.getSurname(), passport, accounts);
    }

    @Override
    public RemotePerson getRemotePerson(String passport) {
        Person person = persons.get(passport);
        if (person == null) {
            return null;
        }
        return (RemotePerson) person;
    }

    @Override
    public Person createPerson(String name, String surname, String passport) throws RemoteException {
        return create(passport, new RemotePerson(name, surname, passport, new ConcurrentHashMap<>(), this::createAccountInBank), this::getRemotePerson, persons);
    }

    @Override
    public ConcurrentMap<String, Account> getAccounts(String passport) throws RemoteException {
        Person person = persons.get(passport);
        if (person == null) {
            return null;
        }
        return person.getAccounts();
    }

    @Override
    public boolean checkPerson(String name, String surname, String passport) throws RemoteException {
        Person person = getRemotePerson(passport);
        return person != null && person.getName().equals(name) && person.getSurname().equals(surname);
    }
}