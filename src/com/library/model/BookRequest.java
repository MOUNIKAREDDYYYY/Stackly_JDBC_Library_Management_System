package com.library.model;

import com.library.util.IdFormatter;
import java.sql.Date;

/**
 * Entity representing a Book Request in the `book_requests` table.
 */
public class BookRequest {
    private int requestId;
    private int bookId;
    private int memberId;
    private Date requestDate;
    private String status; // "PENDING", "APPROVED", "REJECTED"

    // Joined fields for display
    private String bookTitle;
    private String memberName;

    public BookRequest() {}

    public BookRequest(int bookId, int memberId, Date requestDate, String status) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.requestDate = requestDate;
        this.status = status;
    }

    public BookRequest(int requestId, int bookId, int memberId, Date requestDate, String status) {
        this.requestId = requestId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.requestDate = requestDate;
        this.status = status;
    }

    public int getRequestId() { return requestId; }
    public String getFormattedRequestId() { return IdFormatter.formatRequestId(requestId); }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getBookId() { return bookId; }
    public String getFormattedBookId() { return IdFormatter.formatBookId(bookId); }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getMemberId() { return memberId; }
    public String getFormattedMemberId() { return IdFormatter.formatMemberId(memberId); }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public Date getRequestDate() { return requestDate; }
    public void setRequestDate(Date requestDate) { this.requestDate = requestDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    @Override
    public String toString() {
        return String.format("Request [%s | Book: %s (%s) | Member: %s (%s) | Date: %s | Status: %s]",
                getFormattedRequestId(), getFormattedBookId(), (bookTitle != null ? bookTitle : "N/A"),
                getFormattedMemberId(), (memberName != null ? memberName : "N/A"),
                requestDate, status);
    }
}
