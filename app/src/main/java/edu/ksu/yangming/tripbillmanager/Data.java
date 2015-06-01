package edu.ksu.yangming.tripbillmanager;

import android.app.Application;
import android.content.Context;
import android.location.Location;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by Ming on 5/28/2015.
 */
public class Data extends Application implements Serializable {
    final static String DATA_FILE_NAME = "accounts";
    ArrayList<Account> accounts;
    final static SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static class Account {
        ArrayList<Entry> entries= new ArrayList<Entry>();;
        HashMap<String, User> users= new HashMap<String, User>();
        String account_name;
        public Account(String acc) {
            account_name = acc;
        }
        public boolean addUser(String name) {
            if (users.containsKey(name)) return false;
            users.put(name, new User(name));
            return true;
        }

        public boolean changeUser(String beforeName, String afterName) {
            if (!users.containsKey(beforeName)) return false;
            if (users.containsKey(afterName)) return false;
            User user = users.get(beforeName);
            user.user_name = afterName;
            users.remove(beforeName);
            users.put(afterName, user);
            return true;
        }
        public ArrayList<String> users() {
            return new ArrayList<String> (users.keySet());
        }

        public void addEntry(Entry entry) {
            entries.add(entry);
            Collections.sort(entries);
            for (String user_name : entry.users) {
                if (!users.containsKey(user_name)) continue;
                User user = users.get(user_name);
                user.addEntry(entry);
            }

        }

        public boolean updateEntry(Entry entry) {
            if (!entries.contains(entry)) return false;
            return true;
        }
        public boolean removeEntry(Entry entry) {
            if (!entries.contains(entry)) return false;
            entries.remove(entry);
            for (String user_name : entry.users) {
                if (!users.containsKey(user_name)) continue;
                User user = users.get(user_name);
                user.removeEntry(entry);
            }
            return true;
        }

    }

    public static class User implements Serializable {
        String user_name;
        double paid;
        double consumed;
        ArrayList<Entry> user_entries;
        public User(String name) {
            user_name = name;
            paid = 0;
            consumed = 0;
            user_entries = new ArrayList<Entry>();
        }
        public void addEntry(Entry entry) {
            user_entries.add(entry);
            Collections.sort(user_entries);
            if (entry.billSplit.containsKey(user_name )) {
                double bill = entry.billSplit.get(user_name);
                consumed += bill;
            }
            if (entry.paymentSplit.containsKey(user_name)) {
                double pay = entry.paymentSplit.get(user_name);
                paid += pay;
            }
        }
        public boolean removeEntry(Entry entry) {
            if (!user_entries.contains(entry)) return false;
            user_entries.remove(entry);
            if (entry.billSplit.containsKey(user_name )) {
                double bill = entry.billSplit.get(user_name);
                consumed -= bill;
            }
            if (entry.paymentSplit.containsKey(user_name)) {
                double pay = entry.paymentSplit.get(user_name);
                paid -= pay;
            }
            return true;
        }

    }
    public static class Entry implements Serializable, Comparable<Entry>  {
        GregorianCalendar time;
        Location location;
        String locStr;
        double sum_bill;
        int accountID;
        String entry_name;
        ArrayList<String> imageUrls = new ArrayList<String>();
        HashMap<String, Double> billSplit = new HashMap<String, Double>(); //
        HashMap<String, Double> paymentSplit = new HashMap<String, Double>();
        HashSet<String> users = new HashSet<String>();

        public Entry(int acc, Account account, GregorianCalendar t, Location loc) {
            entry_name = "";
            location = loc;
            time = t;
            accountID = acc;
            sum_bill = 0;
            users.addAll(account.users()) ;
        }
        @Override
        public int compareTo(Entry another) {
            Entry that = (Entry) another;
            return this.time.compareTo(that.time);
        }

        public String toString() {
            String str = entry_name + "\n"
                    + "bill total:\t" + sum_bill + "\n"
                    + "time:\t" + timeFormatter.format(time.getTime())  + "\n"
                    + "locaton:\t" + locStr;
            return str;
        }
    }


    public Data() {

    }
    public Data(Data that) {
        this.accounts = that.accounts;
    }

    private void loadData(){
        try {
            FileInputStream fileIn =openFileInput(DATA_FILE_NAME);
            ObjectInputStream input = new ObjectInputStream(fileIn);
            accounts = (ArrayList<Account>) input.readObject();
            fileIn.close();
        } catch (Exception e) {
            accounts = new ArrayList<Account>();
        }

    }


    private void saveData() {
        try {
            FileOutputStream fileOut = openFileOutput(DATA_FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream output = new ObjectOutputStream(fileOut);
            output.writeObject(accounts);
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onCreate() {
        loadData();
    }

    public void onDestroy() {
        saveData();
    }
}
