package info.kgeorgiy.ja.fadeev.bank;

import java.io.Serializable;

public class AccountImpl implements Account, Serializable {
    private final String id;
    private int amount;

    public AccountImpl(final String id, final int amount) {
        this.id = id;
        this.amount = amount;
    }

    public AccountImpl(final String id){
        this(id, 0);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized int getAmount() {
        return amount;
    }

    @Override
    public synchronized void setAmount(final int amount) {
        this.amount = amount;
    }
}
