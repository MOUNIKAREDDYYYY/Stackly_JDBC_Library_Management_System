package com.library.model;

import com.library.util.IdFormatter;
import java.sql.Date;

/**
 * Entity representing an Issue transaction in the `book_issues` table.
 */
public class BookIssue {
    private int issueId;
    private int bookId;
    private int memberId;
    private Date issueDate;
    private Date dueDate;
    private Date returnDate;
    private double fineAmount;
    private String status; // "ISSUED" or "RETURNED"

    // Optional fields populated when performing SQL JOIN queries
    private String bookTitle;
    private String memberName;

    public BookIssue() {}

    public BookIssue(int bookId, int memberId, Date issueDate, Date dueDate, String status) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.status = status;
        this.fineAmount = 0.0;
    }

    public BookIssue(int issueId, int bookId, int memberId, Date issueDate, Date dueDate, Date returnDate, double fineAmount, String status) {
        this.issueId = issueId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fineAmount = fineAmount;
        this.status = status;
    }

    public int getIssueId() { return issueId; }
    public String getFormattedIssueId() { return IdFormatter.formatIssueId(issueId); }
    public void setIssueId(int issueId) { this.issueId = issueId; }

    public int getBookId() { return bookId; }
    public String getFormattedBookId() { return IdFormatter.formatBookId(bookId); }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getMemberId() { return memberId; }
    public String getFormattedMemberId() { return IdFormatter.formatMemberId(memberId); }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public Date getIssueDate() { return issueDate; }
    public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public Date getReturnDate() { return returnDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }

    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    @Override
    public String toString() {
        return String.format("IssueRecord [ID: %s | Book: %s (%s) | Member: %s (%s) | Issued: %s | Due: %s | Returned: %s | Fine: ₹%.2f | Status: %s]",
                getFormattedIssueId(), getFormattedBookId(), (bookTitle != null ? bookTitle : "N/A"),
                getFormattedMemberId(), (memberName != null ? memberName : "N/A"),
                issueDate, dueDate, (returnDate != null ? returnDate.toString() : "-"), fineAmount, status);
    }
}
