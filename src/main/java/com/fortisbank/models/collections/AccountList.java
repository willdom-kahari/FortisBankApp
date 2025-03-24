package com.fortisbank.models.collections;

import com.fortisbank.models.accounts.Account;
import com.fortisbank.utils.AccountComparators;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AccountList extends ArrayList<Account> {

    public AccountList() {
        super();
    }

    public AccountList(Iterable<Account> accounts) {
        accounts.forEach(this::add);
    }

   //Sorting by...

    public void sortByBalance() {
        this.sort(AccountComparators.BY_BALANCE);
    }

    public void sortByType() {
        this.sort(AccountComparators.BY_TYPE);
    }

    public void sortByCreatedDate() {
        this.sort(AccountComparators.BY_CREATED_DATE);
    }

    public AccountList filterByMinBalance(BigDecimal minBalance) {
        return this.stream()
                .filter(account -> account.getAvailableBalance().compareTo(minBalance) >= 0)
                .collect(Collectors.toCollection(AccountList::new));
    }


    public AccountList filterByType(String type) {
        return this.stream()
                .filter(account -> account.getAccountType().name().equalsIgnoreCase(type))
                .collect(Collectors.toCollection(AccountList::new));
    }

    public AccountList filterByActive(){
        return this.stream()
                .filter(Account::isActive)
                .collect(Collectors.toCollection(AccountList::new));
    }

   //toString generates string that displays the list of accounts
    @Override
    public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("AccountList:\n");
         for (Account account : this) {
             builder.append(account.toString()).append("\n");
         }
        return builder.toString();

    }
}
