package com.library.dao;

import com.library.config.DatabaseConfig;
import com.library.model.Member;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * DATA ACCESS OBJECT (DAO): MemberDAO
 * ============================================================================
 * 
 * JDBC Concepts Demonstrated:
 * 1. CRUD Operations on `members` table.
 * 2. PreparedStatements with generated keys retrieval.
 * ============================================================================
 */
public class MemberDAO {

    /**
     * CREATE: Registers a new member.
     */
    public boolean addMember(Member member) throws SQLException {
        String sql = "INSERT INTO members (name, email, phone) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, member.getName());
            stmt.setString(2, member.getEmail());
            stmt.setString(3, member.getPhone());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        member.setMemberId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * READ: Finds a member by ID (Standalone connection).
     */
    public Member findMemberById(int memberId) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return findMemberById(conn, memberId);
        }
    }

    /**
     * READ: Finds a member by ID (Transaction-compatible).
     */
    public Member findMemberById(Connection conn, int memberId) throws SQLException {
        String sql = "SELECT * FROM members WHERE member_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractMemberFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * READ: Retrieves all members sorted by name.
     */
    public List<Member> getAllMembers() throws SQLException {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members ORDER BY name ASC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                members.add(extractMemberFromResultSet(rs));
            }
        }
        return members;
    }

    /**
     * READ: Searches members by name.
     */
    public List<Member> searchMembersByName(String name) throws SQLException {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members WHERE LOWER(name) LIKE LOWER(?) ORDER BY name ASC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + name.trim() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    members.add(extractMemberFromResultSet(rs));
                }
            }
        }
        return members;
    }

    /**
     * READ: Finds a member by exact email.
     */
    public Member findMemberByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM members WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractMemberFromResultSet(rs);
                }
            }
        }
        return null;
    }

    private Member extractMemberFromResultSet(ResultSet rs) throws SQLException {
        return new Member(
            rs.getInt("member_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone")
        );
    }
}
