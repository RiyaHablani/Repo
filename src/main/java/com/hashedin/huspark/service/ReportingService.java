package com.hashedin.huspark.service;

import com.hashedin.huspark.dto.MostBorrowedBookReport;
import com.hashedin.huspark.dto.OverdueItemReport;
import com.hashedin.huspark.dto.SystemHealthReport;
import com.hashedin.huspark.entity.Book;
import com.hashedin.huspark.entity.BorrowingTransaction;
import com.hashedin.huspark.entity.TransactionStatus;
import com.hashedin.huspark.repository.BookRepository;
import com.hashedin.huspark.repository.BorrowingTransactionRepository;
import com.hashedin.huspark.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating various reports and monitoring system health
 */
@Service
public class ReportingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportingService.class);
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private BorrowingTransactionRepository borrowingTransactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${spring.application.name:huSpark}")
    private String applicationName;
    
    @Value("${app.version:1.0.0}")
    private String applicationVersion;
    
    /**
     * Generate report of most borrowed books
     */
    public List<MostBorrowedBookReport> getMostBorrowedBooks(int limit) {
        logger.info("Generating most borrowed books report with limit: {}", limit);
        
        try {
            List<Object[]> results = borrowingTransactionRepository.findMostBorrowedBooks(limit);
            
            List<MostBorrowedBookReport> reports = results.stream()
                .map(row -> {
                    Long bookId = (Long) row[0];
                    String title = (String) row[1];
                    String author = (String) row[2];
                    String isbn = (String) row[3];
                    Long borrowCount = (Long) row[4];
                    
                    return new MostBorrowedBookReport(bookId, title, author, isbn, borrowCount);
                })
                .collect(Collectors.toList());
            
            logger.info("Generated most borrowed books report with {} entries", reports.size());
            return reports;
            
        } catch (Exception e) {
            logger.error("Error generating most borrowed books report", e);
            throw new RuntimeException("Failed to generate most borrowed books report", e);
        }
    }
    
    /**
     * Generate report of overdue items
     */
    public List<OverdueItemReport> getOverdueItems() {
        logger.info("Generating overdue items report");
        
        try {
            List<BorrowingTransaction> overdueTransactions = borrowingTransactionRepository
                .findByStatusAndDueDateBefore(TransactionStatus.BORROWED, LocalDateTime.now());
            
            List<OverdueItemReport> reports = overdueTransactions.stream()
                .map(transaction -> {
                    Book book = transaction.getBook();
                    return new OverdueItemReport(
                        transaction.getId(),
                        book.getId(),
                        book.getTitle(),
                        book.getIsbn(),
                        transaction.getUser().getId(),
                        transaction.getUser().getName(),
                        transaction.getUser().getEmail(),
                        transaction.getBorrowedAt(),
                        transaction.getDueDate()
                    );
                })
                .collect(Collectors.toList());
            
            logger.info("Generated overdue items report with {} entries", reports.size());
            return reports;
            
        } catch (Exception e) {
            logger.error("Error generating overdue items report", e);
            throw new RuntimeException("Failed to generate overdue items report", e);
        }
    }
    
    /**
     * Generate system health report
     */
    public SystemHealthReport getSystemHealthReport() {
        logger.debug("Generating system health report");
        
        try {
            SystemHealthReport report = new SystemHealthReport("UP", applicationName, applicationVersion);
            
            // Add memory status
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            Map<String, Object> memoryStatus = Map.of(
                "heapUsed", memoryBean.getHeapMemoryUsage().getUsed(),
                "heapMax", memoryBean.getHeapMemoryUsage().getMax(),
                "nonHeapUsed", memoryBean.getNonHeapMemoryUsage().getUsed(),
                "nonHeapMax", memoryBean.getNonHeapMemoryUsage().getMax()
            );
            report.setMemoryStatus(memoryStatus);
            
            // Add database status
            long totalBooks = bookRepository.count();
            long totalUsers = userRepository.count();
            long totalTransactions = borrowingTransactionRepository.count();
            
            Map<String, Object> databaseStatus = Map.of(
                "totalBooks", totalBooks,
                "totalUsers", totalUsers,
                "totalTransactions", totalTransactions,
                "status", "UP"
            );
            report.setDatabaseStatus(databaseStatus);
            
            // Add custom metrics
            Map<String, Object> customMetrics = Map.of(
                "activeBorrowings", borrowingTransactionRepository.countByStatus(TransactionStatus.BORROWED),
                "overdueItems", borrowingTransactionRepository.countByStatusAndDueDateBefore(TransactionStatus.BORROWED, LocalDateTime.now()),
                "uptime", ManagementFactory.getRuntimeMXBean().getUptime()
            );
            report.setCustomMetrics(customMetrics);
            
            logger.debug("Generated system health report successfully");
            return report;
            
        } catch (Exception e) {
            logger.error("Error generating system health report", e);
            SystemHealthReport errorReport = new SystemHealthReport("DOWN", applicationName, applicationVersion);
            errorReport.setDatabaseStatus(Map.of("status", "ERROR", "message", e.getMessage()));
            return errorReport;
        }
    }
    
    /**
     * Get borrowing statistics
     */
    public Map<String, Object> getBorrowingStatistics() {
        logger.info("Generating borrowing statistics");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastMonth = now.minusMonths(1);
            LocalDateTime lastWeek = now.minusWeeks(1);
            
            long totalBorrowings = borrowingTransactionRepository.count();
            long borrowingsThisMonth = borrowingTransactionRepository.countByBorrowedAtAfter(lastMonth);
            long borrowingsThisWeek = borrowingTransactionRepository.countByBorrowedAtAfter(lastWeek);
            long overdueItems = borrowingTransactionRepository.countByStatusAndDueDateBefore(TransactionStatus.BORROWED, now);
            
            Map<String, Object> statistics = Map.of(
                "totalBorrowings", totalBorrowings,
                "borrowingsThisMonth", borrowingsThisMonth,
                "borrowingsThisWeek", borrowingsThisWeek,
                "overdueItems", overdueItems,
                "generatedAt", now
            );
            
            logger.info("Generated borrowing statistics successfully");
            return statistics;
            
        } catch (Exception e) {
            logger.error("Error generating borrowing statistics", e);
            throw new RuntimeException("Failed to generate borrowing statistics", e);
        }
    }
}
