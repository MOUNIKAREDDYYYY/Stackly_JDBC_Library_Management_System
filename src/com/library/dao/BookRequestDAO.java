package com.library.dao;

import com.library.config.DatabaseConfig;
import com.library.model.BookRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DATA ACCESS OBJECT (DAO): BookRequestDAO
 */
public class BookRequestDAO {

    public boolean createRequest(BookRequest request) throws SQLException {
        String sql = "INSERT INTO book_requests (book_id, member_id, request_date, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, request.getBookId());
            stmt.setInt(2, request.getMemberId());
            stmt.setDate(3, request.getRequestDate());
            stmt.setString(4, request.getStatus());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        request.setRequestId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public BookRequest findRequestById(Connection conn, int requestId) throws SQLException {
        String sql = """
            SELECT br.*, b.title AS book_title, m.name AS member_name
            FROM book_requests br
            INNER JOIN books b ON br.book_id = b.book_id
            INNER JOIN members m ON br.member_id = m.member_id
            WHERE br.request_id = ?
        """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BookRequest req = extractBookRequest(rs);
                    req.setBookTitle(rs.getString("book_title"));
                    req.setMemberName(rs.getString("member_name"));
                    return req;
                }
            }
        }
        return null;
    }

    public List<BookRequest> getPendingRequests() throws SQLException {
        List<BookRequest> list = new ArrayList<>();
        String sql = """
            SELECT br.*, b.title AS book_title, m.name AS member_name
            FROM book_requests br
            INNER JOIN books b ON br.book_id = b.book_id
            INNER JOIN members m ON br.member_id = m.member_id
            WHERE br.status = 'PENDING'
            ORDER BY br.request_id ASC
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                BookRequest req = extractBookRequest(rs);
                req.setBookTitle(rs.getString("book_title"));
                req.setMemberName(rs.getString("member_name"));
                list.add(req);
            }
        }
        return list;
    }

    public boolean updateRequestStatus(Connection conn, int requestId, String status) throws SQLException {
        String sql = "UPDATE book_requests SET status = ? WHERE request_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, requestId);
            return stmt.executeUpdate() > 0;
        }
    }

    private BookRequest extractBookRequest(ResultSet rs) throws SQLException {
        return new BookRequest(
            rs.getInt("request_id"),
            rs.getInt("book_id"),
            rs.getInt("member_id"),
            rs.getDate("request_date"),
            rs.getString("status")
        );
    }
}
