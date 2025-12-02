package com.example.taixiu.util;

public class Account {
    private long balance;

    public Account() {
        // Tài khoản ban đầu: 2 triệu
        this.balance = 2_000_000;
    }

    public long getBalance() {
        return balance;
    }

    public void add(long amount) {
        this.balance += amount;
    }

    public boolean subtract(long amount) {
        if (amount > balance) {
            return false;
        }
        this.balance -= amount;
        return true;
    }
}
