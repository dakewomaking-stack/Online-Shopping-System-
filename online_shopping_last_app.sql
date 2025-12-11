-- Create the database
CREATE DATABASE IF NOT EXISTS online_shopping_CSd;
USE online_shopping_CSd;

-- Users table (for customers, sellers, and admins)
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role ENUM('customer', 'seller', 'admin') NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(20),
    address TEXT,
    managed_product_id INT NULL, -- Only for sellers (product they manage)
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    category VARCHAR(50),
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Cart table
CREATE TABLE IF NOT EXISTS cart (
    cart_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    UNIQUE KEY (user_id, product_id) -- Ensure one product per user in cart
);

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('pending', 'processing', 'ready_for_pickup', 'shipped_by_seller', 'shipped', 'delivered', 'cancelled') DEFAULT 'pending',
    shipping_address TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Order items (products in each order)
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price_at_purchase DECIMAL(10,2) NOT NULL, -- Price at time of purchase (may differ from current price)
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(100) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('pending', 'completed', 'failed', 'refunded') DEFAULT 'pending',
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

-- Insert initial admin user
INSERT INTO users (username, password, role, full_name, email) 
VALUES ('admin', '1122', 'admin', 'System Administrator', 'admin@onlineshop.com');

-- Insert sample seller
INSERT INTO users (username, password, role, full_name, email, phone_number) 
VALUES ('seller1', 'sellerpass', 'seller', 'John Seller', 'seller1@onlineshop.com', '+251911223344');

-- Insert sample customer
INSERT INTO users (username, password, role, full_name, email, phone_number, address) 
VALUES ('cust1', '2211', 'customer', 'Alice Customer', 'cust1@onlineshop.com', '+251944556677', '123 Main St, Addis Ababa');

-- Insert sample products
INSERT INTO products (name, description, price, stock_quantity, category) 
VALUES 
('Smartphone X', 'Latest smartphone with 128GB storage', 15000.00, 50, 'Electronics'),
('Laptop Pro', '15-inch laptop with 16GB RAM', 45000.00, 20, 'Electronics'),
('Wireless Headphones', 'Noise-cancelling Bluetooth headphones', 3500.00, 100, 'Accessories'),
('Coffee Maker', 'Automatic drip coffee machine', 2500.00, 30, 'Home Appliances');

-- Assign a product to the seller (update the seller's managed_product_id)
UPDATE users SET managed_product_id = 1 WHERE username = 'seller1';