-- ============================================================================
-- MYSQL WORKBENCH DATABASE SCHEMA SCRIPT
-- Library Management System (books, members, book_requests, book_issues)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;

-- 1. Books Table
CREATE TABLE IF NOT EXISTS books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    total_copies INT NOT NULL CHECK (total_copies >= 0),
    available_copies INT NOT NULL CHECK (available_copies >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Members Table
CREATE TABLE IF NOT EXISTS members (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Book Requests Table
CREATE TABLE IF NOT EXISTS book_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    member_id INT NOT NULL,
    request_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT fk_req_book FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    CONSTRAINT fk_req_member FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Book Issues Table
CREATE TABLE IF NOT EXISTS book_issues (
    issue_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    member_id INT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE NULL,
    fine_amount DOUBLE DEFAULT 0.0,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ISSUED', 'RETURNED')),
    CONSTRAINT fk_issue_book FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    CONSTRAINT fk_issue_member FOREIGN KEY (member_id) REFERENCES members(member_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- SAMPLE SEED DATA
-- ============================================================================
INSERT INTO books (title, author, category, total_copies, available_copies) VALUES
('Clean Code', 'Robert C. Martin', 'Technology', 5, 5),
('Effective Java', 'Joshua Bloch', 'Technology', 3, 3),
('The Great Gatsby', 'F. Scott Fitzgerald', 'Fiction', 4, 4),
('Atomic Habits', 'James Clear', 'Self-Help', 6, 6)
ON DUPLICATE KEY UPDATE title=title;

INSERT INTO members (name, email, phone) VALUES
('Alice Smith', 'alice@example.com', '9876543210'),
('Bob Jones', 'bob@example.com', '9123456789')
ON DUPLICATE KEY UPDATE name=name;
