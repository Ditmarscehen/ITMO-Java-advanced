package info.kgeorgiy.ja.fadeev.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public final class Client {
    /**
     * Utility class.
     */
    private Client() {
    }

    public static void main(final String... args) throws RemoteException {
        final Bank bank;
        try {
            bank = (Bank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }

        if (args == null || args.length != 5) {
            System.err.println("five not null arguments required");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.err.println("arguments must not be null");
                return;
            }
        }
        final String name = args[0];
        final String surname = args[1];
        final String passport = args[2];
        final String id = args[3];
        int delta;
        try {
            delta = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.err.println("fifth argument must be number");
            return;
        }

        try {
            if (bank.getRemotePerson(passport) == null) {
                bank.createPerson(name, surname, passport);
            }
            if (bank.checkPerson(name, surname, passport)) {
                Account account = bank.getAccount(id);
                if (account == null) {
                    account = bank.createAccount(id);
                    System.out.println("Created account with balance " + account.getAmount());
                }
                account.setAmount(account.getAmount() + delta);
                System.out.println("Current amount is " + account.getAmount());
            } else {
                System.err.println("Invalid person data");
            }
        } catch (RemoteException e) {
            System.err.println("Remote exception occurred " + e.getMessage());
        }
    }
}
