package com.library.dao;

import com.library.config.DatabaseConfig;
import com.library.model.BookIssue;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * DATA ACCESS OBJECT (DAO): BookIssueDAO
 * ============================================================================
 * 
 * JDBC Concepts Demonstrated:
 * 1. Transactional SQL Operations (Connection passed into methods).
 * 2. SQL JOINS: Combines `book_issues`, `books`, and `members` tables.
 * ============================================================================
 */
public class BookIssueDAO {

    /**
     * CREATE: Inserts a new book issue record (Transaction-compatible).
     */
    public boolean insertIssueRecord(Connection conn, BookIssue issue) throws SQLException {
        String sql = "INSERT INTO book_issues (book_id, member_id, issue_date, due_date, status, fine_amount) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, issue.getBookId());
            stmt.setInt(2, issue.getMemberId());
            stmt.setDate(3, issue.getIssueDate());
            stmt.setDate(4, issue.getDueDate());
            stmt.setString(5, issue.getStatus());
            stmt.setDouble(6, issue.getFineAmount());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        issue.setIssueId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * READ: Retrieves an issue record by ID (Transaction-compatible).
     */
    public BookIssue findIssueById(Connection conn, int issueId) throws SQLException {
        String sql = "SELECT * FROM book_issues WHERE issue_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, issueId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractBookIssueFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * READ: Checks if a member already has an active (ISSUED or REQUESTED) copy of the same book.
     */
    public boolean hasActiveIssue(Connection conn, int bookId, int memberId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM book_issues WHERE book_id = ? AND member_id = ? AND status IN ('ISSUED', 'REQUESTED')";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * UPDATE: Approves a pending book request (Transaction-compatible).
     */
    public boolean approveRequestTransaction(Connection conn, int issueId, Date issueDate, Date dueDate) throws SQLException {
        String sql = "UPDATE book_issues SET issue_date = ?, due_date = ?, status = 'ISSUED' WHERE issue_id = ? AND status = 'REQUESTED'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, issueDate);
            stmt.setDate(2, dueDate);
            stmt.setInt(3, issueId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * UPDATE: Rejects a pending book request.
     */
    public boolean rejectRequest(int issueId) throws SQLException {
        String sql = "UPDATE book_issues SET status = 'REJECTED' WHERE issue_id = ? AND status = 'REQUESTED'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, issueId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * UPDATE: Updates return details & fine amount on book return (Transaction-compatible).
     */
    public boolean updateReturnDetails(Connection conn, int issueId, Date returnDate, double fineAmount) throws SQLException {
        String sql = "UPDATE book_issues SET return_date = ?, fine_amount = ?, status = 'RETURNED' WHERE issue_id = ? AND status = 'ISSUED'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, returnDate);
            stmt.setDouble(2, fineAmount);
            stmt.setInt(3, issueId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * JDBC JOIN DEMONSTRATION:
     * Performs an INNER JOIN across `book_issues`, `books`, and `members` tables.
     * Selects issue info along with the Book Title and Member Name.
     *
     * @return List of BookIssue objects with populated bookTitle and memberName fields.
     */
    public List<BookIssue> getAllIssuedBooksWithDetails() throws SQLException {
        List<BookIssue> list = new ArrayList<>();
        String sql = """
            SELECT 
                bi.issue_id, 
                bi.book_id, 
                b.title AS book_title, 
                bi.member_id, 
                m.name AS member_name, 
                bi.issue_date, 
                bi.due_date, 
                bi.return_date, 
                bi.fine_amount, 
                bi.status
            FROM book_issues bi
            INNER JOIN books b ON bi.book_id = b.book_id
            INNER JOIN members m ON bi.member_id = m.member_id
            ORDER BY bi.issue_id DESC
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                BookIssue issue = extractBookIssueFromResultSet(rs);
                issue.setBookTitle(rs.getString("book_title"));
                issue.setMemberName(rs.getString("member_name"));
                list.add(issue);
            }
        }
        return list;
    }

    /**
     * READ: Retrieves pending book requests with status 'REQUESTED'.
     */
    public List<BookIssue> getPendingRequestsWithDetails() throws SQLException {
        List<BookIssue> list = new ArrayList<>();
        String sql = """
            SELECT 
                bi.issue_id, 
                bi.book_id, 
                b.title AS book_title, 
                bi.member_id, 
                m.name AS member_name, 
                bi.issue_date, 
                bi.due_date, 
                bi.return_date, 
                bi.fine_amount, 
                bi.status
            FROM book_issues bi
            INNER JOIN books b ON bi.book_id = b.book_id
            INNER JOIN members m ON bi.member_id = m.member_id
            WHERE bi.status = 'REQUESTED'
            ORDER BY bi.issue_id ASC
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                BookIssue issue = extractBookIssueFromResultSet(rs);
                issue.setBookTitle(rs.getString("book_title"));
                issue.setMemberName(rs.getString("member_name"));
                list.add(issue);
            }
        }
        return list;
    }

    private BookIssue extractBookIssueFromResultSet(ResultSet rs) throws SQLException {
        return new BookIssue(
            rs.getInt("issue_id"),
            rs.getInt("book_id"),
            rs.getInt("member_id"),
            rs.getDate("issue_date"),
            rs.getDate("due_date"),
            rs.getDate("return_date"),
            rs.getDouble("fine_amount"),
            rs.getString("status")
        );
    }
}
