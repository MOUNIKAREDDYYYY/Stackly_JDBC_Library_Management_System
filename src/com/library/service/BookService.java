package com.library.service;

import com.library.dao.BookDAO;
import com.library.model.Book;

import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for Book-related business logic.
 */
public class BookService {
    private final BookDAO bookDAO;

    public BookService() {
        this.bookDAO = new BookDAO();
    }

    public boolean addBook(String title, String author, String category, int totalCopies) {
        if (title == null || title.isBlank() || author == null || author.isBlank()) {
            System.err.println("[BookService] Error: Title and Author cannot be empty.");
            return false;
        }
        if (totalCopies <= 0) {
            System.err.println("[BookService] Error: Total copies must be greater than 0.");
            return false;
        }

        Book book = new Book(title.trim(), author.trim(), category.trim(), totalCopies, totalCopies);
        try {
            return bookDAO.addBook(book);
        } catch (SQLException e) {
            System.err.println("[BookService] Database error adding book: " + e.getMessage());
            return false;
        }
    }

    public List<Book> searchByTitle(String title) {
        try {
            return bookDAO.searchBooksByTitle(title);
        } catch (SQLException e) {
            System.err.println("[BookService] Database error searching by title: " + e.getMessage());
            return List.of();
        }
    }

    public List<Book> searchByCategory(String category) {
        try {
            return bookDAO.searchBooksByCategory(category);
        } catch (SQLException e) {
            System.err.println("[BookService] Database error searching by category: " + e.getMessage());
            return List.of();
        }
    }

    public List<Book> searchByAuthor(String author) {
        try {
            return bookDAO.searchBooksByAuthor(author);
        } catch (SQLException e) {
            System.err.println("[BookService] Database error searching by author: " + e.getMessage());
            return List.of();
        }
    }

    public List<Book> getAvailableBooks() {
        try {
            return bookDAO.getAvailableBooks();
        } catch (SQLException e) {
            System.err.println("[BookService] Database error listing available books: " + e.getMessage());
            return List.of();
        }
    }

    public List<Book> getAllBooks() {
        try {
            return bookDAO.getAllBooks();
        } catch (SQLException e) {
            System.err.println("[BookService] Database error listing books: " + e.getMessage());
            return List.of();
        }
    }

    public Book getBookById(int bookId) {
        try {
            return bookDAO.findBookById(bookId);
        } catch (SQLException e) {
            System.err.println("[BookService] Database error getting book: " + e.getMessage());
            return null;
        }
    }
}
