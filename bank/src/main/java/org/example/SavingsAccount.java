package com.example.bank;

public class SavingsAccount extendsx BankAccount {
    public SavingsAccount(String name, double balance) {
        super(name, balance);
    }

    @Override
    public void withdraw(double amount) {
        if (getBalance() - amount < 100) {
            System.out.println("Cannot withdraw: Insufficient balance");
        } else {
            super.withdraw(amount);
        }
    }
}
