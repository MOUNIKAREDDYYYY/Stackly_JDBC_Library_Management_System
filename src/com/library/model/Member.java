package com.library.model;

import com.library.util.IdFormatter;

/**
 * Entity representing a Member record in the `members` table.
 */
public class Member {
    private int memberId;
    private String name;
    private String email;
    private String phone;

    public Member() {}

    public Member(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public Member(int memberId, String name, String email, String phone) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public int getMemberId() { return memberId; }
    public String getFormattedMemberId() { return IdFormatter.formatMemberId(memberId); }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() {
        return String.format("Member [ID: %s | Name: '%s' | Email: '%s' | Phone: '%s']",
                getFormattedMemberId(), name, email, phone);
    }
}
