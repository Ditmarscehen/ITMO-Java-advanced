package info.kgeorgiy.ja.fadeev.bank;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(JUnit4.class)
public class BankTest {
    private static Bank bank;
    private static int len = 10;
    private static final int numOfPersons = 5;
    private static final int numOfAcc = 5;
    private static final Random random = new Random();


    @BeforeClass
    public static void beforeClass() {
        final int port = 8888;
        bank = new RemoteBank(port);
        try {
            UnicastRemoteObject.exportObject(bank, port);
            Naming.rebind("//localhost/bank", bank);
        } catch (RemoteException e) {
            System.err.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Malformed URL");
        }
    }

    @Test
    public void localPersonTest() throws RemoteException {
        personTest(bank::getLocalPerson);
    }

    @Test
    public void remotePersonTest() throws RemoteException {
        personTest(bank::getRemotePerson);
    }

    @Test
    public void checkPersonTest() throws RemoteException {
        final int length = len++;
        for (int i = 0; i < numOfPersons; i++) {
            final String name = getRandomString(length);
            final String surname = getRandomString(length);
            final String passport = getRandomString(length);
            Assert.assertFalse(bank.checkPerson(name, surname, passport));
            bank.createPerson(name, surname, passport);
            Assert.assertTrue(bank.checkPerson(name, surname, passport));
        }
    }


    @Test
    public void accountCreateGetTest() throws RemoteException {
        List<String> passports = createAccounts(true, len++);
        for (String p : passports) {
            for (int i = 0; i < numOfAcc; i++) {
                String id = getId(i, p);
                Account account = bank.getAccount(id);
                Assert.assertNotNull(account);
                Assert.assertEquals(id, account.getId());
                Assert.assertEquals(0, account.getAmount());
            }
        }
    }

    @Test
    public void accountSetAmountTest() throws RemoteException {
        List<String> passports = createAccounts(len++);
        for (int i = 0; i < numOfPersons; i++) {
            for (int j = 0; j < numOfAcc; j++) {
                Account account = bank.getAccount(getId(j, passports.get(i)));
                account.setAmount(1 + i + j);
            }
        }

        for (int i = 0; i < numOfPersons; i++) {
            for (int j = 0; j < numOfAcc; j++) {
                Assert.assertEquals(1 + i + j, bank.getAccount(getId(j, passports.get(i))).getAmount());
            }
        }
    }

    @Test
    public void getAccountsTest() throws RemoteException {
        List<String> passports = createAccounts(len++);
        for (final String p : passports) {
            Person person1 = bank.getRemotePerson(p);
            Person person2 = bank.getLocalPerson(p);
            Set<String> accountsIds1 = person1.getAccounts().keySet();
            Set<String> accountsIds2 = person2.getAccounts().keySet();
            Set<String> accountsIds3 = bank.getAccounts(p).keySet();
            Set<String> expected = IntStream.range(0, numOfAcc).mapToObj(i -> getId(i, p)).collect(Collectors.toSet());
            Assert.assertEquals(expected, accountsIds1);
            Assert.assertEquals(expected, accountsIds2);
            Assert.assertEquals(expected, accountsIds3);
        }
    }

    @Test
    public void accountLocalRemoteLocalTest() throws RemoteException {
        List<String> passports = createAccounts(len++);
        for (String p : passports) {
            LocalPerson localPerson1 = bank.getLocalPerson(p);
            for (int i = 0; i < numOfAcc; i++) {
                Account account = bank.getAccount(getId(i, p));
                account.setAmount(i + 2);
            }
            LocalPerson localPerson2 = bank.getLocalPerson(p);
            for (int i = 0; i < numOfAcc; i++) {
                Account account = bank.getAccount(getId(i, p));
                Assert.assertTrue(areAmountsEqual(localPerson2, account));
                Assert.assertFalse(areAmountsEqual(localPerson1, account));
            }
        }
    }

    @Test
    public void accountLocalTest() throws RemoteException {
        List<String> passports = createAccounts(len++);
        for (String p : passports) {
            LocalPerson localPerson = bank.getLocalPerson(p);
            ConcurrentMap<String, Account> accounts = localPerson.getAccounts();
            for (int i = 0; i < numOfAcc; i++) {
                Account localAccount = accounts.get(getId(i, p));
                localAccount.setAmount(i + 2);
                Account account = bank.getAccount(getId(i, p));
                Assert.assertNotEquals(account.getAmount(), localAccount.getAmount());
            }
        }
    }

    @Test
    public void serializationTest() throws IOException, ClassNotFoundException {
        bank.createPerson("hello", "hello", "hello");
        bank.createAccount("hello:1");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        LocalPerson person = bank.getLocalPerson("hello");
        new ObjectOutputStream(baos).writeObject(person);
        new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();

        Account localAccount = person.getAccounts().get("1:hello");
        new ObjectOutputStream(baos).writeObject(localAccount);
        new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
    }

    private String getRandomString(final int length) {
        int leftLimit = 'a';
        int rightLimit = 'z';
        return random.ints(leftLimit, rightLimit + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private List<String> createAccounts(boolean check, int length) throws RemoteException {
        List<String> passports = new ArrayList<>();
        for (int i = 0; i < numOfPersons; i++) {
            final String name = getRandomString(length);
            final String surname = getRandomString(length);
            final String passport = getRandomString(length);
            bank.createPerson(name, surname, passport);
            passports.add(passport);
            for (int j = 0; j < numOfAcc; j++) {
                if (check) {
                    Assert.assertNull(bank.getAccount(getId(j, passport)));
                }
                bank.createAccount(getId(j, passport));
            }
        }
        return passports;
    }

    private List<String> createAccounts(final int length) throws RemoteException {
        return createAccounts(false, length);
    }

    private String getId(final int i, final String passport) {
        return passport + ":" + i;
    }

    private boolean areAmountsEqual(LocalPerson person, Account account) throws RemoteException {
        Account localAccount = person.getAccounts().get(account.getId());
        return localAccount.getAmount() == account.getAmount();
    }

    private void personTest(RemoteFunction<String, Person> getP) throws RemoteException {
        final int length = len++;
        for (int i = 0; i < numOfPersons; i++) {
            final String name = getRandomString(length);
            final String surname = getRandomString(length);
            final String passport = getRandomString(length);
            Assert.assertNull(getP.apply(passport));
            bank.createPerson(name, surname, passport);
            Person person = getP.apply(passport);
            Assert.assertNotNull(person);
            Assert.assertEquals(name, person.getName());
            Assert.assertEquals(surname, person.getSurname());
            Assert.assertEquals(passport, person.getPassport());
        }
    }
}