package com.library.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ============================================================================
 * DATABASE CONFIGURATION LAYER (MySQL Workbench / MySQL & H2 Support)
 * ============================================================================
 */
public class DatabaseConfig {

    private static final String MYSQL_HOST = "localhost";
    private static final String MYSQL_PORT = "3306";
    private static final String MYSQL_DB_NAME = "library_db";
    
    private static final String MYSQL_USER = System.getenv().getOrDefault("MYSQL_USER", "root");
    private static final String MYSQL_PASSWORD = System.getenv().getOrDefault("MYSQL_PASSWORD", "root");

    private static final String JDBC_URL_MYSQL = "jdbc:mysql://" + MYSQL_HOST + ":" + MYSQL_PORT + "/" + MYSQL_DB_NAME 
            + "?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    
    private static final String JDBC_URL_H2 = "jdbc:h2:./library_jdbc_db;DB_CLOSE_DELAY=-1";
    
    private static boolean useMySQL = true;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("org.h2.Driver");
                useMySQL = false;
                System.out.println("[DatabaseConfig] MySQL driver not found. Falling back to embedded H2 Database.");
            } catch (ClassNotFoundException ex) {
                System.err.println("[DatabaseConfig] No JDBC driver found.");
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        if (useMySQL) {
            try {
                return DriverManager.getConnection(JDBC_URL_MYSQL, MYSQL_USER, MYSQL_PASSWORD);
            } catch (SQLException e) {
                // If MySQL connection fails (e.g. server offline), switch to embedded H2
                System.err.println("[DatabaseConfig] Could not connect to MySQL server. Falling back to embedded H2 Database.");
                useMySQL = false;
                return DriverManager.getConnection(JDBC_URL_H2, "sa", "");
            }
        } else {
            return DriverManager.getConnection(JDBC_URL_H2, "sa", "");
        }
    }

    public static void initializeDatabase() {
        String createBooksTable = """
            CREATE TABLE IF NOT EXISTS books (
                book_id INT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                author VARCHAR(255) NOT NULL,
                category VARCHAR(100) NOT NULL,
                total_copies INT NOT NULL CHECK (total_copies >= 0),
                available_copies INT NOT NULL CHECK (available_copies >= 0)
            );
        """;

        String createMembersTable = """
            CREATE TABLE IF NOT EXISTS members (
                member_id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL UNIQUE,
                phone VARCHAR(50) NOT NULL
            );
        """;

        String createBookRequestsTable = """
            CREATE TABLE IF NOT EXISTS book_requests (
                request_id INT AUTO_INCREMENT PRIMARY KEY,
                book_id INT NOT NULL,
                member_id INT NOT NULL,
                request_date DATE NOT NULL,
                status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
                FOREIGN KEY (book_id) REFERENCES books(book_id),
                FOREIGN KEY (member_id) REFERENCES members(member_id)
            );
        """;

        String createBookIssuesTable = """
            CREATE TABLE IF NOT EXISTS book_issues (
                issue_id INT AUTO_INCREMENT PRIMARY KEY,
                book_id INT NOT NULL,
                member_id INT NOT NULL,
                issue_date DATE NOT NULL,
                due_date DATE NOT NULL,
                return_date DATE,
                fine_amount DOUBLE DEFAULT 0.0,
                status VARCHAR(20) NOT NULL CHECK (status IN ('ISSUED', 'RETURNED')),
                FOREIGN KEY (book_id) REFERENCES books(book_id),
                FOREIGN KEY (member_id) REFERENCES members(member_id)
            );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createBooksTable);
            stmt.execute(createMembersTable);
            stmt.execute(createBookRequestsTable);
            stmt.execute(createBookIssuesTable);
            
            System.out.println("[DatabaseConfig] Database tables (books, members, book_requests, book_issues) checked/initialized successfully.");
        } catch (SQLException e) {
            System.err.println("[DatabaseConfig] Database initialization error: " + e.getMessage());
        }
    }
}
