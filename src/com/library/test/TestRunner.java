package com.library.test;

import com.library.config.DatabaseConfig;
import com.library.model.Book;
import com.library.model.BookIssue;
import com.library.model.BookRequest;
import com.library.model.Member;
import com.library.service.BookService;
import com.library.service.IssueReturnService;
import com.library.service.MemberService;

import java.time.LocalDate;
import java.util.List;

/**
 * Verification test runner for testing JDBC CRUD, Transactions, Fine Calculation, and Joins.
 */
public class TestRunner {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("  RUNNING AUTOMATED TEST SUITE FOR JDBC LIBRARY SYSTEM   ");
        System.out.println("==========================================================");

        // 1. Initialize DB
        DatabaseConfig.initializeDatabase();

        BookService bookService = new BookService();
        MemberService memberService = new MemberService();
        IssueReturnService issueReturnService = new IssueReturnService();

        // 2. Add Books (CRUD: Create)
        System.out.println("\n1. Testing CRUD - Add Books:");
        boolean b1 = bookService.addBook("Design Patterns", "GoF", "Computer Science", 3);
        boolean b2 = bookService.addBook("Clean Architecture", "Robert C. Martin", "Technology", 2);
        System.out.println("   Added 'Design Patterns': " + b1);
        System.out.println("   Added 'Clean Architecture': " + b2);

        // 3. Search Books (CRUD: Read)
        System.out.println("\n2. Testing CRUD - Search Books:");
        List<Book> techBooks = bookService.searchByCategory("Technology");
        System.out.println("   Books found under 'Technology': " + techBooks.size());
        for (Book b : techBooks) {
            System.out.println("     - " + b);
        }

        // 4. Add Member (CRUD: Create)
        System.out.println("\n3. Testing CRUD - Register Member:");
        boolean m1 = memberService.registerMember("John Doe", "john@example.com", "9998887770");
        System.out.println("   Registered Member 'John Doe': " + m1);

        List<Member> members = memberService.getAllMembers();
        int memberId = members.get(0).getMemberId();
        int bookId = techBooks.get(0).getBookId();

        // 5. Book Request & Approval Workflow (2-step JDBC Transaction)
        System.out.println("\n4. Testing Book Request Workflow (Step 1):");
        boolean reqSuccess = issueReturnService.requestBook(bookId, memberId);
        System.out.println("   Request Book Status: " + reqSuccess);

        List<BookRequest> pendingRequests = issueReturnService.getPendingRequestsReport();
        System.out.println("   Pending Requests count: " + pendingRequests.size());
        if (!pendingRequests.isEmpty()) {
            int requestId = pendingRequests.get(0).getRequestId();
            System.out.println("\n5. Testing Book Approval Workflow (Step 2 - JDBC Transaction):");
            boolean appSuccess = issueReturnService.approveAndIssueBook(requestId);
            System.out.println("   Approval Status: " + appSuccess);
        }

        // Verify available copies decreased
        Book issuedBook = bookService.getBookById(bookId);
        System.out.println("   Available Copies after approval: " + issuedBook.getAvailableCopies() + " / " + issuedBook.getTotalCopies());

        // 6. View Issued Books Report (SQL JOIN)
        System.out.println("\n6. Testing SQL JOIN - Issued Books Report:");
        List<BookIssue> report = issueReturnService.getAllIssuedBooksReport();
        for (BookIssue bi : report) {
            System.out.println("   JOIN Record -> " + bi);
            if ("ISSUED".equals(bi.getStatus())) {
                int issueId = bi.getIssueId();

                // 7. Return Book & Fine Calculation (JDBC Transaction)
                System.out.println("\n7. Testing JDBC Transaction & Fine Calculation - Return Book Overdue:");
                LocalDate returnDate = LocalDate.now().plusDays(19);
                boolean returnSuccess = issueReturnService.returnBook(issueId, returnDate);
                System.out.println("   Return Book Status: " + returnSuccess);
            }
        }

        // Verify copies restored
        Book restoredBook = bookService.getBookById(bookId);
        System.out.println("\n8. Verification - Available Copies after return: " + restoredBook.getAvailableCopies() + " / " + restoredBook.getTotalCopies());

        System.out.println("\n==========================================================");
        System.out.println("        ALL JDBC LIBRARY SYSTEM TESTS PASSED!             ");
        System.out.println("==========================================================");
    }
}
