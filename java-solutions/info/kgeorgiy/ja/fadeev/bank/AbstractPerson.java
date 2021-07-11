package info.kgeorgiy.ja.fadeev.bank;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;


public abstract class AbstractPerson implements Person, Serializable {
    private final String name;
    private final String surname;
    private final String passport;
    private final ConcurrentMap<String, Account> accounts;


    public AbstractPerson(final String name, final String surname, final String passport, final ConcurrentMap<String, Account> accounts) {
        this.name = name;
        this.surname = surname;
        this.passport = passport;
        this.accounts = accounts;
    }

    @Override
    public String getName()  {
        return name;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public String getPassport() {
        return passport;
    }

    @Override
    public ConcurrentMap<String, Account> getAccounts() {
        return accounts;
    }

}
