package com.hashedin.huspark.repository;

import com.hashedin.huspark.entity.BorrowingTransaction;
import com.hashedin.huspark.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowingTransactionRepository extends JpaRepository<BorrowingTransaction, Long> {

    // Find active borrowing for a user and book
    Optional<BorrowingTransaction> findByUserIdAndBookIdAndStatusIn(
        Long userId, Long bookId, List<TransactionStatus> statuses);

    // Find all active borrowings for a user
    List<BorrowingTransaction> findByUserIdAndStatusIn(Long userId, List<TransactionStatus> statuses);

    // Find all active borrowings for a book
    List<BorrowingTransaction> findByBookIdAndStatusIn(Long bookId, List<TransactionStatus> statuses);

    // Find overdue transactions
    @Query("SELECT bt FROM BorrowingTransaction bt WHERE bt.dueDate < :now AND bt.status = 'BORROWED'")
    List<BorrowingTransaction> findOverdueTransactions(@Param("now") LocalDateTime now);

    // Find transactions due for reminders
    @Query("SELECT bt FROM BorrowingTransaction bt WHERE bt.dueDate BETWEEN :startDate AND :endDate " +
           "AND bt.status = 'BORROWED' AND (bt.lastReminderSent IS NULL OR bt.lastReminderSent < :reminderThreshold)")
    List<BorrowingTransaction> findTransactionsDueForReminders(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("reminderThreshold") LocalDateTime reminderThreshold);

    // Count active borrowings for a user
    @Query("SELECT COUNT(bt) FROM BorrowingTransaction bt WHERE bt.user.id = :userId AND bt.status IN ('BORROWED', 'OVERDUE')")
    Long countActiveBorrowingsByUserId(@Param("userId") Long userId);

    // Find users who haven't borrowed in a long time
    @Query("SELECT DISTINCT bt.user.id FROM BorrowingTransaction bt WHERE bt.borrowedAt < :thresholdDate")
    List<Long> findInactiveUserIds(@Param("thresholdDate") LocalDateTime thresholdDate);

    // Find borrowing history for a user
    List<BorrowingTransaction> findByUserIdOrderByBorrowedAtDesc(Long userId);

    // Find borrowing history for a book
    List<BorrowingTransaction> findByBookIdOrderByBorrowedAtDesc(Long bookId);

    // Find transactions that need late fee calculation
    @Query("SELECT bt FROM BorrowingTransaction bt WHERE bt.returnedAt IS NOT NULL " +
           "AND bt.returnedAt > bt.dueDate AND bt.lateFee = 0")
    List<BorrowingTransaction> findTransactionsNeedingLateFeeCalculation();

    // Find transactions due on a specific date
    @Query("SELECT bt FROM BorrowingTransaction bt WHERE DATE(bt.dueDate) = DATE(:dueDate) " +
           "AND bt.status = 'BORROWED'")
    List<BorrowingTransaction> findTransactionsDueOn(@Param("dueDate") LocalDateTime dueDate);
}
