package com.fortisbank.models.accounts;

import com.fortisbank.models.users.Customer;

import java.math.BigDecimal;
import java.util.Date;

public class CurrencyAccount extends Account {
    private String currencyCode;
    private Date lastActiveDate;

    public CurrencyAccount(String accountNumber, Customer customer, Date openedDate, BigDecimal initialBalance, String currencyCode) {
        super(accountNumber, customer, AccountType.CURRENCY, openedDate, initialBalance);
        this.currencyCode = currencyCode.toUpperCase();
        this.lastActiveDate = new Date();
    }

    @Override
    public BigDecimal getCreditLimit() {
        return null;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String newCurrencyCode) {
        this.currencyCode = newCurrencyCode.toUpperCase();
    }

    public Date getLastActiveDate() {
        return lastActiveDate;
    }

    public void updateLastActiveDate() {
        this.lastActiveDate = new Date();
    }
    @Override
    public String displayAccountInfo() {
        return "Account Number: " + getAccountNumber() + "\n" +
                "Account Type: " + getAccountType() + "\n" +
                "Opened Date: " + getOpenedDate() + "\n" +
                "Available Balance: " + getAvailableBalance() + "\n" +
                "Currency Code: " + getCurrencyCode() + "\n" +
                "Customer Name: " + getCustomer().getFullName();
    }
}
