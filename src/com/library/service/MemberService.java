package com.library.service;

import com.library.dao.MemberDAO;
import com.library.model.Member;

import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for Member-related business logic.
 */
public class MemberService {
    private final MemberDAO memberDAO;

    public MemberService() {
        this.memberDAO = new MemberDAO();
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return phone.trim().matches("^[6-9][0-9]{9}$");
    }

    public boolean registerMember(String name, String email, String phone) {
        if (name == null || name.isBlank()) {
            System.err.println("[MemberService] Error: Name cannot be empty.");
            return false;
        }
        if (!isValidEmail(email)) {
            System.err.println("[MemberService] Error: Invalid email format.");
            return false;
        }
        if (!isValidPhone(phone)) {
            System.err.println("[MemberService] Error: Invalid phone number. Enter a valid 10-digit phone number.");
            return false;
        }
        Member member = new Member(name.trim(), email.trim(), phone.trim());
        try {
            return memberDAO.addMember(member);
        } catch (SQLException e) {
            if (e.getMessage() != null && (e.getMessage().toLowerCase().contains("unique") || e.getMessage().toLowerCase().contains("duplicate"))) {
                System.out.println("\nERROR: Email address already registered.");
                System.out.println("Please use a different email address.");
            } else {
                System.out.println("Database error registering member: " + e.getMessage());
            }
            return false;
        }
    }

    public List<Member> getAllMembers() {
        try {
            return memberDAO.getAllMembers();
        } catch (SQLException e) {
            System.err.println("[MemberService] Database error listing members: " + e.getMessage());
            return List.of();
        }
    }

    public Member getMemberById(int memberId) {
        try {
            return memberDAO.findMemberById(memberId);
        } catch (SQLException e) {
            System.err.println("[MemberService] Database error getting member: " + e.getMessage());
            return null;
        }
    }

    public List<Member> searchMembersByName(String name) {
        try {
            return memberDAO.searchMembersByName(name);
        } catch (SQLException e) {
            System.err.println("[MemberService] Database error searching member by name: " + e.getMessage());
            return List.of();
        }
    }

    public Member searchMemberByEmail(String email) {
        try {
            return memberDAO.findMemberByEmail(email);
        } catch (SQLException e) {
            System.err.println("[MemberService] Database error searching member by email: " + e.getMessage());
            return null;
        }
    }
}
