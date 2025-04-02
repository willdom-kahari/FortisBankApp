package com.fortisbank.business.services;

import com.fortisbank.data.repositories.StorageMode;
import com.fortisbank.models.accounts.Account;
import com.fortisbank.models.transactions.Transaction;
import com.fortisbank.models.users.BankManager;
import com.fortisbank.models.users.Customer;
import com.fortisbank.models.users.User;
import com.fortisbank.models.others.Notification;
import com.fortisbank.models.others.NotificationType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Central service to manage and dispatch notifications to users.
 * Implemented as a singleton.
 */
public class NotificationService {

    private StorageMode storageMode;

    private static  NotificationService instance ;

    private NotificationService(StorageMode storageMode) {
        this.storageMode = storageMode;
    }

    public static NotificationService getInstance(StorageMode storageMode) {
        if (instance == null) {
            instance = new NotificationService(storageMode);
        }
        return instance;
    }

    // === Notification Dispatching ===

    public void sendNotification(User recipient, NotificationType type, String title, String message) {
        if (recipient == null) return;
        Notification notification = new Notification(type, title, message);
        recipient.getInbox().add(notification);

        // update users in the database (Customer and BankManager)
        if (recipient instanceof Customer) {
            CustomerService.getInstance(storageMode).updateCustomer((Customer) recipient);
        }

        if (recipient instanceof BankManager) {
            BankManagerService.getInstance(storageMode).updateBankManager((BankManager) recipient);
        }

    }

    public void sendNotification(User recipient, NotificationType type, String title, String message, Customer customer, Account account) {
        if (recipient == null) return;
        Notification notification = new Notification(type, title, message, customer, account);
        recipient.getInbox().add(notification);

        // update users in the database (Customer and BankManager)
        if (recipient instanceof Customer) {
            CustomerService.getInstance(storageMode).updateCustomer((Customer) recipient);
        }

        if (recipient instanceof BankManager) {
            BankManagerService.getInstance(storageMode).updateBankManager((BankManager) recipient);
        }
    }

    // === Predefined Notification Helpers ===

    public void notifyTransactionReceipt(User user, Transaction tx) {
        String title = "Transaction Completed";
        String message = String.format("Your %s of $%.2f on %s was successful.",
                tx.getTransactionType(), tx.getAmount(), tx.getTransactionDate());
        sendNotification(user, NotificationType.TRANSACTION_RECEIPT, title, message);
    }

    public void notifyAccountRequest(User manager, Customer customer, Account requestedAccount) {
        //debug check received parameters
        System.out.println("[NotificationService]Account request submitted:");
        System.out.println("Customer: " + customer);
        System.out.println("Requested Account: " + requestedAccount);
        System.out.println("Manager: " + manager);

        if (manager == null || customer == null || requestedAccount == null) return;
        String title = "New Account Request";
        String message = String.format("Customer %s requested a new %s account.",
                customer.getFullName(), requestedAccount.getAccountType());
        sendNotification(manager, NotificationType.ACCOUNT_OPENING_REQUEST, title, message, customer, requestedAccount);
        //debug
        System.out.println("[NotificationService]Account request passed notification to manager");

        // Confirmation to customer
        sendNotification(customer, NotificationType.INFO, "Request Sent",
                "Your account request was sent to the manager.", customer, requestedAccount);
        //debug
        System.out.println("[NotificationService]Account request passed notification to customer");
    }

    public void notifyApproval(Customer customer, Account approvedAccount) {
        String title = "Account Approved";
        String message = String.format("Your account (%s) has been approved.", approvedAccount.getAccountNumber());
        sendNotification(customer, NotificationType.ACCOUNT_APPROVAL, title, message, customer, approvedAccount);
    }

    public void notifyRejection(Customer customer, String reason, Account rejectedAccount) {
        String title = "Account Rejected";
        String message = "Your account request was declined: " + reason;
        sendNotification(customer, NotificationType.ACCOUNT_REJECTION, title, message, customer, rejectedAccount);
    }

    public void notifyNewMessage(User user, String fromName) {
        String title = "New Message";
        String message = String.format("You received a new message from %s.", fromName);
        sendNotification(user, NotificationType.NEW_MESSAGE, title, message);
    }

    public void notifySecurityAlert(User user, String details) {
        String title = "Security Alert";
        String message = "Important security notice: " + details;
        sendNotification(user, NotificationType.SECURITY_ALERT, title, message);
    }

    public void notifySystemUpdate(User user, String updateDetails) {
        String title = "System Update";
        String message = "Recent changes: " + updateDetails;
        sendNotification(user, NotificationType.SYSTEM_UPDATE, title, message);
    }

    public void notifyCustom(User user, String title, String message) {
        sendNotification(user, NotificationType.CUSTOM, title, message);
    }

    // === Inbox Helpers ===

    public List<Notification> getAllNotifications(User user) {
        if (user == null || user.getInbox() == null) return new ArrayList<>();
        return reverseCopy(user.getInbox());
    }

    public List<Notification> getUnreadNotifications(User user) {
        if (user == null || user.getInbox() == null) return new ArrayList<>();
        return user.getInbox().stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());
    }

    public void markAllAsRead(User user) {
        if (user == null || user.getInbox() == null) return;
        user.getInbox().forEach(Notification::markAsRead);
    }

    public void clearInbox(User user) {
        if (user == null || user.getInbox() == null) return;
        user.getInbox().clear();
    }

    private List<Notification> reverseCopy(List<Notification> list) {
        List<Notification> reversed = new ArrayList<>(list);
        reversed.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return reversed;
    }
}
