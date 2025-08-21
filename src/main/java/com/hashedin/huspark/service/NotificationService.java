package com.hashedin.huspark.service;

import com.hashedin.huspark.entity.BorrowingTransaction;
import com.hashedin.huspark.entity.Notification;
import com.hashedin.huspark.entity.User;
import com.hashedin.huspark.repository.BorrowingTransactionRepository;
import com.hashedin.huspark.repository.NotificationRepository;
import com.hashedin.huspark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final BorrowingTransactionRepository borrowingTransactionRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    @Scheduled(cron = "0 0 9 * * ?") // Run daily at 9 AM
    public void sendOverdueNotifications() {
        log.info("Starting overdue book notification job");
        
        LocalDateTime now = LocalDateTime.now();
        List<BorrowingTransaction> overdueTransactions = borrowingTransactionRepository
                .findOverdueTransactions(now);

        for (BorrowingTransaction transaction : overdueTransactions) {
            try {
                sendOverdueNotification(transaction);
            } catch (Exception e) {
                log.error("Failed to send overdue notification for transaction: {}", 
                         transaction.getId(), e);
            }
        }
        
        log.info("Completed overdue book notification job. Sent {} notifications", 
                overdueTransactions.size());
    }

    @Scheduled(cron = "0 0 10 * * ?") // Run daily at 10 AM
    public void sendDueDateReminders() {
        log.info("Starting due date reminder job");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        
        List<BorrowingTransaction> dueTomorrowTransactions = borrowingTransactionRepository
                .findTransactionsDueOn(tomorrow);

        for (BorrowingTransaction transaction : dueTomorrowTransactions) {
            try {
                sendDueDateReminder(transaction);
            } catch (Exception e) {
                log.error("Failed to send due date reminder for transaction: {}", 
                         transaction.getId(), e);
            }
        }
        
        log.info("Completed due date reminder job. Sent {} reminders", 
                dueTomorrowTransactions.size());
    }

    public void sendOverdueNotification(BorrowingTransaction transaction) {
        User user = transaction.getUser();
        String message = String.format(
            "Dear %s,\n\nYour book '%s' by %s is overdue. " +
            "It was due on %s. Please return it as soon as possible to avoid any penalties.\n\n" +
            "Thank you,\nLibrary Management System",
            user.getName(),
            transaction.getBook().getTitle(),
            transaction.getBook().getAuthor(),
            transaction.getDueDate().toLocalDate()
        );

        Notification notification = createNotification(
            user.getId(),
            transaction.getBook().getId(),
            transaction.getId(),
            Notification.NotificationType.OVERDUE_BOOK,
            message,
            Notification.DeliveryMethod.BOTH
        );

        // Send email notification
        try {
            emailService.sendEmail(user.getEmail(), "Book Overdue Notice", message);
            notification.setDeliveryStatus(Notification.DeliveryStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send email notification", e);
            notification.setDeliveryStatus(Notification.DeliveryStatus.FAILED);
        }

        // Send SMS notification (if phone number is available)
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            try {
                smsService.sendSms(user.getPhone(), 
                    "Your book '" + transaction.getBook().getTitle() + "' is overdue. Please return it soon.");
            } catch (Exception e) {
                log.error("Failed to send SMS notification", e);
            }
        }

        notificationRepository.save(notification);
    }

    public void sendDueDateReminder(BorrowingTransaction transaction) {
        User user = transaction.getUser();
        String message = String.format(
            "Dear %s,\n\nThis is a friendly reminder that your book '%s' by %s is due tomorrow (%s). " +
            "Please return it on time to avoid overdue charges.\n\n" +
            "Thank you,\nLibrary Management System",
            user.getName(),
            transaction.getBook().getTitle(),
            transaction.getBook().getAuthor(),
            transaction.getDueDate().toLocalDate()
        );

        Notification notification = createNotification(
            user.getId(),
            transaction.getBook().getId(),
            transaction.getId(),
            Notification.NotificationType.DUE_DATE_REMINDER,
            message,
            Notification.DeliveryMethod.EMAIL
        );

        try {
            emailService.sendEmail(user.getEmail(), "Book Due Tomorrow Reminder", message);
            notification.setDeliveryStatus(Notification.DeliveryStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send due date reminder email", e);
            notification.setDeliveryStatus(Notification.DeliveryStatus.FAILED);
        }

        notificationRepository.save(notification);
    }

    public void sendBookReturnedNotification(BorrowingTransaction transaction) {
        User user = transaction.getUser();
        String message = String.format(
            "Dear %s,\n\nThank you for returning '%s' by %s. " +
            "Your book has been successfully returned and your account is up to date.\n\n" +
            "Thank you,\nLibrary Management System",
            user.getName(),
            transaction.getBook().getTitle(),
            transaction.getBook().getAuthor()
        );

        Notification notification = createNotification(
            user.getId(),
            transaction.getBook().getId(),
            transaction.getId(),
            Notification.NotificationType.BOOK_RETURNED,
            message,
            Notification.DeliveryMethod.EMAIL
        );

        try {
            emailService.sendEmail(user.getEmail(), "Book Return Confirmation", message);
            notification.setDeliveryStatus(Notification.DeliveryStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send book returned notification", e);
            notification.setDeliveryStatus(Notification.DeliveryStatus.FAILED);
        }

        notificationRepository.save(notification);
    }

    public void sendBookBorrowedNotification(BorrowingTransaction transaction) {
        User user = transaction.getUser();
        String message = String.format(
            "Dear %s,\n\nYou have successfully borrowed '%s' by %s. " +
            "The book is due on %s. Please return it on time.\n\n" +
            "Thank you,\nLibrary Management System",
            user.getName(),
            transaction.getBook().getTitle(),
            transaction.getBook().getAuthor(),
            transaction.getDueDate().toLocalDate()
        );

        Notification notification = createNotification(
            user.getId(),
            transaction.getBook().getId(),
            transaction.getId(),
            Notification.NotificationType.BOOK_BORROWED,
            message,
            Notification.DeliveryMethod.EMAIL
        );

        try {
            emailService.sendEmail(user.getEmail(), "Book Borrowed Confirmation", message);
            notification.setDeliveryStatus(Notification.DeliveryStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send book borrowed notification", e);
            notification.setDeliveryStatus(Notification.DeliveryStatus.FAILED);
        }

        notificationRepository.save(notification);
    }

    private Notification createNotification(Long userId, Long bookId, Long transactionId,
                                          Notification.NotificationType type, String message,
                                          Notification.DeliveryMethod deliveryMethod) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setBookId(bookId);
        notification.setTransactionId(transactionId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setDeliveryMethod(deliveryMethod);
        notification.setDeliveryStatus(Notification.DeliveryStatus.PENDING);
        
        // Set recipient details
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            notification.setRecipientEmail(user.getEmail());
            notification.setRecipientPhone(user.getPhone());
        }
        
        return notification;
    }
}
