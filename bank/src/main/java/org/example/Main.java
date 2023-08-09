package com.example.bank;

import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.*;

public class Main {

    private static List<BankAccount> accounts = new ArrayList<>();
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> collection;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Connect to MongoDB
            mongoClient = MongoClients.create();
            database = mongoClient.getDatabase("mydb");
            collection = database.getCollection("accounts");

            // Load accounts from MongoDB
            MongoCursor<Document> cursor = collection.find().iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String accountType = doc.getString("accountType");
                String accountName = doc.getString("accountName");
                double balance = doc.getDouble("balance");
                BankAccount account = null;
                if (accountType.equals("b")) {
                    account = new BankAccount(accountName, balance);
                } else if (accountType.equals("s")) {
                    account = new SavingsAccount(accountName, balance);
                }
                accounts.add(account);
            }
        } catch (Exception e) {
            System.out.println("Error while connecting to the database or loading accounts: " + e.getMessage());
            return;
        } finally {
            if (mongoClient != null) {
                mongoClient.close();
            }
        }

        while (true) {
            System.out.println("Choose an option: (a)Add Account (l)Display Accounts (s)Save to database (d) Deposit funds (w)Withdraw funds (q)Quit");
            String option = scanner.nextLine();

            if (option.equals("q")) {
                try {
                    // Save accounts to MongoDB
                    for (BankAccount account : accounts) {
                        Document doc = new Document("accountType", account instanceof SavingsAccount ? "s" : "b")
                                .append("accountName", account.getName())
                                .append("balance", account.getBalance());
                        collection.insertOne(doc);
                    }
                } catch (Exception e) {
                    System.out.println("Error while saving accounts to the database: " + e.getMessage());
                } finally {
                    if (mongoClient != null) {
                        mongoClient.close();
                    }
                }
                break;
            } else if (option.equals("a")) {
                System.out.println("Choose the type of account: (b) BankAccount (s) SavingsAccount");
                String accountType = scanner.nextLine();

                System.out.println("Enter the account name: ");
                String accountName = scanner.nextLine();

                System.out.println("Enter the balance: ");
                double balance;
                try {
                    balance = Double.parseDouble(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid balance input. Please enter a number.");
                    continue;
                }

                BankAccount account = null;
                if (accountType.equals("b")) {
                    account = new BankAccount(accountName, balance);
                } else if (accountType.equals("s")) {
                    account = new SavingsAccount(accountName, balance);
                }

                accounts.add(account);
                System.out.println("Account created: " + account.getName() + " with balance: " + account.getBalance());
            } else if (option.equals("l")) {
                for (BankAccount account : accounts) {
                    System.out.println("Account: " + account.getName() + ", Balance: " + account.getBalance());
                }
            } else if (option.equals("s")) {
                try {
                    // Save accounts to MongoDB
                    for (BankAccount account : accounts) {
                        Document doc = new Document("accountType", account instanceof SavingsAccount ? "s" : "b")
                                .append("accountName", account.getName())
                                .append("balance", account.getBalance());
                        collection.insertOne(doc);
                    }
                } catch (Exception e) {
                    System.out.println("Error while saving accounts to the database: " + e.getMessage());
                }
            } else if (option.equals("d") || option.equals("w")) {
                System.out.println("Enter the account name: ");
                String accountName = scanner.nextLine();
                BankAccount selectedAccount = null;
                for (BankAccount account : accounts) {
                    if (account.getName().equals(accountName)) {
                        selectedAccount = account;
                        break;
                    }
                }

                if (selectedAccount == null) {
                    System.out.println("Account not found.");
                    continue;
                }

                System.out.println("Enter the amount: ");
                double amount;
                try {
                    amount = Double.parseDouble(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid amount input. Please enter a number.");
                    continue;
                }

                if (option.equals("d")) {
                    selectedAccount.deposit(amount);
                    System.out.println("Deposit successful. New balance: " + selectedAccount.getBalance());
                } else {
                    if (selectedAccount.getBalance() - amount < 0) {
                        System.out.println("Cannot withdraw: Insufficient balance");
                        continue;
                    }

                    selectedAccount.withdraw(amount);
                    System.out.println("Withdrawal successful. New balance: " + selectedAccount.getBalance());
                }
            }
        }
    }
}