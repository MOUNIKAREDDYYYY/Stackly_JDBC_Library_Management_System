package com.library;

import com.library.config.DatabaseConfig;
import com.library.model.Book;
import com.library.model.BookIssue;
import com.library.model.BookRequest;
import com.library.model.Member;
import com.library.service.BookService;
import com.library.service.IssueReturnService;
import com.library.service.MemberService;
import com.library.util.IdFormatter;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class LibraryApplication {

    private final BookService bookService;
    private final MemberService memberService;
    private final IssueReturnService issueReturnService;
    private final Scanner scanner;

    public LibraryApplication() {
        this.bookService = new BookService();
        this.memberService = new MemberService();
        this.issueReturnService = new IssueReturnService();
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        DatabaseConfig.initializeDatabase();
        LibraryApplication app = new LibraryApplication();
        app.seedInitialData();
        app.startMenu();
    }

    private void seedInitialData() {
        if (bookService.getAllBooks().isEmpty()) {
            bookService.addBook("Clean Code", "Robert C. Martin", "Technology", 5);
            bookService.addBook("Effective Java", "Joshua Bloch", "Technology", 3);
            bookService.addBook("The Great Gatsby", "F. Scott Fitzgerald", "Fiction", 4);
            bookService.addBook("Atomic Habits", "James Clear", "Self-Help", 6);
            memberService.registerMember("Alice Smith", "alice@example.com", "9876543210");
            memberService.registerMember("Bob Jones", "bob@example.com", "9123456789");
        }
    }

    public void startMenu() {
        while (true) {
            System.out.println("\n=================================================");
            System.out.println("        LIBRARY MANAGEMENT SYSTEM");
            System.out.println("=================================================");
            System.out.println("[1] Book Management");
            System.out.println("[2] Member Management");
            System.out.println("[3] Request / Issue Book");
            System.out.println("[4] Return Book");
            System.out.println("[5] Issued Books Report");
            System.out.println("[6] Exit");
            System.out.print("\nEnter choice: ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> handleBookManagement();
                case "2" -> handleMemberManagement();
                case "3" -> handleIssueManagement();
                case "4" -> handleReturnBook();
                case "5" -> handleViewIssuedBooksReport();
                case "6" -> {
                    System.out.println("\nExiting...");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void handleBookManagement() {
        while (true) {
            System.out.println("\n---------------- BOOK MANAGEMENT ----------------");
            System.out.println("[1] Add Book");
            System.out.println("[2] Search by Title");
            System.out.println("[3] Search by Author");
            System.out.println("[4] Search by Category");
            System.out.println("[5] List Available Books");
            System.out.println("[6] List All Books");
            System.out.println("[7] Back");
            System.out.print("\nEnter choice: ");
            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> handleAddBook();
                case "2" -> {
                    System.out.print("Enter Title keyword: ");
                    displayBooks(bookService.searchByTitle(scanner.nextLine()));
                }
                case "3" -> {
                    System.out.print("Enter Author keyword: ");
                    displayBooks(bookService.searchByAuthor(scanner.nextLine()));
                }
                case "4" -> {
                    System.out.print("Enter Category name: ");
                    displayBooks(bookService.searchByCategory(scanner.nextLine()));
                }
                case "5" -> displayBooks(bookService.getAvailableBooks());
                case "6" -> displayBooks(bookService.getAllBooks());
                case "7" -> { return; }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void handleAddBook() {
        System.out.print("Enter Book Title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) return;
        System.out.print("Enter Author Name: ");
        String author = scanner.nextLine().trim();
        if (author.isEmpty()) return;
        System.out.print("Enter Category: ");
        String category = scanner.nextLine().trim();
        if (category.isEmpty()) return;
        System.out.print("Enter Total Copies: ");
        try {
            int copies = Integer.parseInt(scanner.nextLine().trim());
            if (copies <= 0) return;
            if (bookService.addBook(title, author, category, copies)) {
                System.out.println("SUCCESS: Book added!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
        }
    }

    private void displayBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        System.out.println("\nBook ID | Title                     | Author               | Category     | Total | Available");
        System.out.println("---------------------------------------------------------------------------------------------");
        for (Book b : books) {
            System.out.printf("%-7s | %-25s | %-20s | %-12s | %-5d | %-5d%n",
                    IdFormatter.formatBookId(b.getBookId()), truncate(b.getTitle(), 25), truncate(b.getAuthor(), 20), truncate(b.getCategory(), 12), b.getTotalCopies(), b.getAvailableCopies());
        }
    }

    private void handleMemberManagement() {
        while (true) {
            System.out.println("\n--------------- MEMBER MANAGEMENT ---------------");
            System.out.println("[1] Register Member");
            System.out.println("[2] Search Member");
            System.out.println("[3] List Members");
            System.out.println("[4] Back");
            System.out.print("\nEnter choice: ");
            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> {
                    while (true) {
                        System.out.print("Enter Member Name (or 'cancel' to abort): ");
                        String name = scanner.nextLine();
                        if (name.trim().equalsIgnoreCase("cancel")) break;
                        System.out.print("Enter Email Address: ");
                        String email = scanner.nextLine();
                        System.out.print("Enter Phone Number: ");
                        String phone = scanner.nextLine();
                        if (memberService.registerMember(name, email, phone)) {
                            System.out.println("SUCCESS: Member registered!");
                            break;
                        }
                        System.out.println("\nPlease try again.");
                    }
                }
                case "2" -> {
                    System.out.print("Enter Member Name keyword: ");
                    displayMembers(memberService.searchMembersByName(scanner.nextLine()));
                }
                case "3" -> displayMembers(memberService.getAllMembers());
                case "4" -> { return; }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void displayMembers(List<Member> members) {
        if (members.isEmpty()) {
            System.out.println("No members found.");
            return;
        }
        System.out.println("\nMember ID | Name                 | Email                     | Phone");
        System.out.println("--------------------------------------------------------------------------");
        for (Member m : members) {
            System.out.printf("%-9s | %-20s | %-25s | %-15s%n",
                    IdFormatter.formatMemberId(m.getMemberId()), truncate(m.getName(), 20), truncate(m.getEmail(), 25), m.getPhone());
        }
    }

    private void handleIssueManagement() {
        while (true) {
            System.out.println("\n------------- REQUEST / ISSUE BOOK --------------");
            System.out.println("[1] Request Book");
            System.out.println("[2] View Pending Requests");
            System.out.println("[3] Approve Request");
            System.out.println("[4] Direct Issue");
            System.out.println("[5] View Currently Issued Books");
            System.out.println("[6] Back");
            System.out.print("\nEnter choice: ");
            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> {
                    int bookId = readId("Book ID (e.g. B001)");
                    if (bookId <= 0) continue;
                    int memberId = readId("Member ID (e.g. M001)");
                    if (memberId <= 0) continue;
                    issueReturnService.requestBook(bookId, memberId);
                }
                case "2" -> {
                    List<BookRequest> pending = issueReturnService.getPendingRequestsReport();
                    if (pending.isEmpty()) {
                        System.out.println("No pending book requests.");
                    } else {
                        displayRequests(pending);
                    }
                }
                case "3" -> {
                    List<BookRequest> pending = issueReturnService.getPendingRequestsReport();
                    if (pending.isEmpty()) {
                        System.out.println("No pending book requests.");
                        System.out.println("Returning to Request / Issue menu...");
                        continue;
                    }
                    displayRequests(pending);
                    int issueId = readId("Request ID to approve (e.g. R001)");
                    if (issueId > 0) issueReturnService.approveAndIssueBook(issueId);
                }
                case "4" -> {
                    int bookId = readId("Book ID (e.g. B001)");
                    if (bookId <= 0) continue;
                    int memberId = readId("Member ID (e.g. M001)");
                    if (memberId <= 0) continue;
                    issueReturnService.issueBook(bookId, memberId);
                }
                case "5" -> displayIssuedBooksOnly();
                case "6" -> { return; }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void displayRequests(List<BookRequest> pending) {
        System.out.println("\nPending Requests");
        System.out.println("Request ID | Book Title                | Member Name          | Requested Date");
        System.out.println("------------------------------------------------------------------------------");
        for (BookRequest p : pending) {
            System.out.printf("%-10s | %-25s | %-20s | %-12s%n",
                    IdFormatter.formatRequestId(p.getRequestId()), truncate(p.getBookTitle(), 25), truncate(p.getMemberName(), 20), p.getRequestDate());
        }
    }

    private boolean displayIssuedBooksOnly() {
        List<BookIssue> issued = issueReturnService.getAllIssuedBooksReport().stream()
                .filter(b -> "ISSUED".equalsIgnoreCase(b.getStatus())).toList();
        if (issued.isEmpty()) {
            System.out.println("No books are currently issued.\nNothing to return.\n\nReturning to Main Menu...");
            return false;
        }
        System.out.println("\nCurrently Issued Books:");
        System.out.println("Issue ID | Book Title                | Member Name          | Due Date");
        System.out.println("--------------------------------------------------------------------------");
        for (BookIssue b : issued) {
            System.out.printf("%-8s | %-25s | %-20s | %-12s%n",
                    IdFormatter.formatIssueId(b.getIssueId()), truncate(b.getBookTitle(), 25), truncate(b.getMemberName(), 20), b.getDueDate());
        }
        return true;
    }

    private void handleReturnBook() {
        System.out.println("\n------------- RETURN BOOK -----------------------");
        if (!displayIssuedBooksOnly()) return;
        int issueId = readId("Issue ID to return (e.g. I001)");
        if (issueId <= 0) return;

        BookIssue issue = issueReturnService.getIssueById(issueId);
        if (issue == null) {
            System.out.println("ERROR: Issue ID " + IdFormatter.formatIssueId(issueId) + " does not exist.");
            return;
        }
        if ("RETURNED".equalsIgnoreCase(issue.getStatus())) {
            System.out.println("ERROR: Issue " + IdFormatter.formatIssueId(issueId) + " is already RETURNED.");
            System.out.println("No further action required.");
            return;
        }

        System.out.println("Enter Return Simulation Days:");
        System.out.println("0 = Return today");
        System.out.println("1 = 1 day after due date");
        System.out.println("5 = 5 days after due date");
        System.out.print("Choice: ");
        
        int overdueDays = 0;
        try {
            overdueDays = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid days.");
            return;
        }
        if (overdueDays < 0) return;

        LocalDate returnDate = LocalDate.now();
        if (overdueDays > 0) {
            returnDate = issue.getDueDate().toLocalDate().plusDays(overdueDays);
        }

        issueReturnService.returnBook(issueId, returnDate);
    }

    private void handleViewIssuedBooksReport() {
        System.out.println("\n------------- ISSUED BOOKS REPORT ---------------");
        List<BookIssue> report = issueReturnService.getAllIssuedBooksReport();
        if (report.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        System.out.println("Issue ID | Book Title           | Member Name          | Issue Date | Due Date   | Return Date | Status   | Fine");
        System.out.println("----------------------------------------------------------------------------------------------------------------------");
        for (BookIssue bi : report) {
            String retDate = bi.getReturnDate() != null ? bi.getReturnDate().toString() : "-";
            System.out.printf("%-8s | %-20s | %-20s | %-10s | %-10s | %-11s | %-8s | ₹%-6.2f%n",
                    IdFormatter.formatIssueId(bi.getIssueId()), truncate(bi.getBookTitle(), 20), truncate(bi.getMemberName(), 20),
                    bi.getIssueDate(), bi.getDueDate(), retDate, bi.getStatus(), bi.getFineAmount());
        }
    }

    private int readId(String prompt) {
        System.out.print("Enter " + prompt + ": ");
        try {
            return IdFormatter.parseId(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format.");
            return -1;
        }
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "N/A";
        return text.length() > maxLen ? text.substring(0, maxLen - 3) + "..." : text;
    }
}
