package com.library.service;

import com.library.config.DatabaseConfig;
import com.library.dao.BookDAO;
import com.library.dao.BookIssueDAO;
import com.library.dao.BookRequestDAO;
import com.library.dao.MemberDAO;
import com.library.model.Book;
import com.library.model.BookIssue;
import com.library.model.BookRequest;
import com.library.model.Member;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class IssueReturnService {

    private static final double FINE_PER_DAY = 10.0;
    private static final int ISSUE_PERIOD_DAYS = 14;

    private final BookDAO bookDAO;
    private final MemberDAO memberDAO;
    private final BookIssueDAO bookIssueDAO;
    private final BookRequestDAO bookRequestDAO;

    public IssueReturnService() {
        this.bookDAO = new BookDAO();
        this.memberDAO = new MemberDAO();
        this.bookIssueDAO = new BookIssueDAO();
        this.bookRequestDAO = new BookRequestDAO();
    }

    public boolean requestBook(int bookId, int memberId) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            Member member = memberDAO.findMemberById(conn, memberId);
            if (member == null) {
                System.out.println("ERROR: Member ID " + memberId + " not found.");
                return false;
            }

            Book book = bookDAO.findBookById(conn, bookId);
            if (book == null) {
                System.out.println("ERROR: Book ID " + bookId + " not found.");
                return false;
            }

            System.out.println("\nBook: " + book.getTitle());
            System.out.println("Member: " + member.getName());
            System.out.println("Available Copies: " + book.getAvailableCopies());

            if (book.getAvailableCopies() <= 0) {
                System.out.println("\nERROR: No copies of '" + book.getTitle() + "' are currently available.");
                return false;
            }

            if (bookIssueDAO.hasActiveIssue(conn, bookId, memberId)) {
                System.out.println("\nERROR: " + member.getName() + " already has this book issued.");
                return false;
            }

            LocalDate today = LocalDate.now();
            BookRequest requestRecord = new BookRequest(bookId, memberId, Date.valueOf(today), "PENDING");

            boolean inserted = bookRequestDAO.createRequest(requestRecord);
            if (inserted) {
                System.out.println("\nSUCCESS: Request " + requestRecord.getFormattedRequestId() + " created.");
                System.out.println("Status: PENDING");
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.out.println("ERROR submitting request: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean approveAndIssueBook(int requestId) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            BookRequest request = bookRequestDAO.findRequestById(conn, requestId);
            if (request == null) {
                System.out.println("ERROR: Request ID " + requestId + " not found.");
                conn.rollback();
                return false;
            }

            if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
                System.out.println("ERROR: Request ID " + requestId + " is not in 'PENDING' status (Current: " + request.getStatus() + ").");
                conn.rollback();
                return false;
            }

            Book book = bookDAO.findBookById(conn, request.getBookId());
            if (book == null || book.getAvailableCopies() <= 0) {
                System.out.println("ERROR: No copies of book ID " + request.getBookId() + " are currently available.");
                conn.rollback();
                return false;
            }

            Member member = memberDAO.findMemberById(conn, request.getMemberId());

            boolean copiesUpdated = bookDAO.updateAvailableCopies(conn, request.getBookId(), -1);
            if (!copiesUpdated) {
                System.out.println("ERROR: Failed to update book copy count.");
                conn.rollback();
                return false;
            }

            boolean approved = bookRequestDAO.updateRequestStatus(conn, requestId, "APPROVED");
            if (!approved) {
                System.out.println("ERROR: Failed to approve request record.");
                conn.rollback();
                return false;
            }

            LocalDate today = LocalDate.now();
            LocalDate dueDate = today.plusDays(ISSUE_PERIOD_DAYS);
            BookIssue issueRecord = new BookIssue(
                request.getBookId(),
                request.getMemberId(),
                Date.valueOf(today),
                Date.valueOf(dueDate),
                "ISSUED"
            );

            boolean recordInserted = bookIssueDAO.insertIssueRecord(conn, issueRecord);
            if (!recordInserted) {
                System.out.println("ERROR: Failed to insert book issue record.");
                conn.rollback();
                return false;
            }

            conn.commit();
            System.out.println("\nSUCCESS: Request " + request.getFormattedRequestId() + " approved.");
            System.out.println("\nIssue ID : " + issueRecord.getFormattedIssueId());
            System.out.println("Book     : " + book.getTitle());
            System.out.println("Member   : " + member.getName());
            System.out.println("Due Date : " + dueDate);
            System.out.println("Status   : ISSUED");
            return true;

        } catch (SQLException e) {
            System.out.println("Transaction Failed: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {}
            }
        }
    }

    public List<BookRequest> getPendingRequestsReport() {
        try {
            return bookRequestDAO.getPendingRequests();
        } catch (SQLException e) {
            System.out.println("Database error fetching pending requests: " + e.getMessage());
            return List.of();
        }
    }

    public boolean issueBook(int bookId, int memberId) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            Member member = memberDAO.findMemberById(conn, memberId);
            if (member == null) {
                System.out.println("ERROR: Member ID " + memberId + " not found.");
                conn.rollback();
                return false;
            }

            Book book = bookDAO.findBookById(conn, bookId);
            if (book == null) {
                System.out.println("ERROR: Book ID " + bookId + " not found.");
                conn.rollback();
                return false;
            }

            if (book.getAvailableCopies() <= 0) {
                System.out.println("ERROR: No copies of '" + book.getTitle() + "' are currently available.");
                conn.rollback();
                return false;
            }

            if (bookIssueDAO.hasActiveIssue(conn, bookId, memberId)) {
                System.out.println("ERROR: " + member.getName() + " already has this book issued.");
                conn.rollback();
                return false;
            }

            boolean copiesUpdated = bookDAO.updateAvailableCopies(conn, bookId, -1);
            if (!copiesUpdated) {
                System.out.println("ERROR: Failed to update book copy count.");
                conn.rollback();
                return false;
            }

            LocalDate today = LocalDate.now();
            LocalDate dueDate = today.plusDays(ISSUE_PERIOD_DAYS);
            BookIssue issueRecord = new BookIssue(
                bookId,
                memberId,
                Date.valueOf(today),
                Date.valueOf(dueDate),
                "ISSUED"
            );

            boolean recordInserted = bookIssueDAO.insertIssueRecord(conn, issueRecord);
            if (!recordInserted) {
                System.out.println("ERROR: Failed to insert book issue record.");
                conn.rollback();
                return false;
            }

            conn.commit();
            System.out.println("\nSUCCESS: Direct issue created.");
            System.out.println("Issue ID : " + issueRecord.getFormattedIssueId());
            System.out.println("Book     : " + book.getTitle());
            System.out.println("Member   : " + member.getName());
            System.out.println("Due Date : " + dueDate);
            System.out.println("Status   : ISSUED");
            return true;

        } catch (SQLException e) {
            System.out.println("Transaction Failed: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {}
            }
        }
    }

    public boolean returnBook(int issueId, LocalDate actualReturnDate) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            BookIssue issue = bookIssueDAO.findIssueById(conn, issueId);
            if (issue == null) {
                System.out.println("ERROR: Issue ID " + issueId + " not found.");
                conn.rollback();
                return false;
            }

            if ("RETURNED".equalsIgnoreCase(issue.getStatus())) {
                System.out.println("ERROR: Book for Issue ID " + issueId + " has already been returned.");
                conn.rollback();
                return false;
            }

            LocalDate dueDate = issue.getDueDate().toLocalDate();
            double fineAmount = calculateFine(dueDate, actualReturnDate);

            boolean copiesUpdated = bookDAO.updateAvailableCopies(conn, issue.getBookId(), 1);
            if (!copiesUpdated) {
                System.out.println("ERROR: Failed to update available copies.");
                conn.rollback();
                return false;
            }

            boolean recordUpdated = bookIssueDAO.updateReturnDetails(
                conn, 
                issueId, 
                Date.valueOf(actualReturnDate), 
                fineAmount
            );

            if (!recordUpdated) {
                System.out.println("ERROR: Failed to update return details in database.");
                conn.rollback();
                return false;
            }

            conn.commit();
            
            Book book = bookDAO.findBookById(conn, issue.getBookId());
            Member member = memberDAO.findMemberById(conn, issue.getMemberId());
            
            long overdueDays = 0;
            if (actualReturnDate.isAfter(dueDate)) {
                overdueDays = ChronoUnit.DAYS.between(dueDate, actualReturnDate);
            }

            System.out.println("\nSUCCESS: Book returned successfully!");
            System.out.println("Issue ID      : " + issue.getFormattedIssueId());
            System.out.println("Book          : " + (book != null ? book.getTitle() : "N/A"));
            System.out.println("Member        : " + (member != null ? member.getName() : "N/A"));
            System.out.println("Due Date      : " + dueDate);
            System.out.println("Return Date   : " + actualReturnDate);
            System.out.println("Overdue Days  : " + overdueDays);
            System.out.printf("Fine          : ₹%.2f%n", fineAmount);
            System.out.println("Status        : RETURNED");

            return true;

        } catch (SQLException e) {
            System.out.println("Transaction Failed: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {}
            }
        }
    }

    public double calculateFine(LocalDate dueDate, LocalDate returnDate) {
        if (returnDate.isAfter(dueDate)) {
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
            return daysOverdue * FINE_PER_DAY;
        }
        return 0.0;
    }

    public List<BookIssue> getAllIssuedBooksReport() {
        try {
            return bookIssueDAO.getAllIssuedBooksWithDetails();
        } catch (SQLException e) {
            System.out.println("Database error fetching report: " + e.getMessage());
            return List.of();
        }
    }

    public BookIssue getIssueById(int issueId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return bookIssueDAO.findIssueById(conn, issueId);
        } catch (SQLException e) {
            System.out.println("Database error finding issue: " + e.getMessage());
            return null;
        }
    }
}
