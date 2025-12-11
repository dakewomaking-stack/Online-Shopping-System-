package onlineshopping_last_app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CustomerDashboardFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private JButton logoutButton;
    private JTable productTable, cartTable, orderHistoryTable;
    private DefaultTableModel productTableModel, cartTableModel, orderHistoryTableModel;
    private final String BACKGROUND_PATH = "C:\\Users\\ABCD\\Desktop\\java\\GUI\\Cphoto_2024-05-06_11-11-03.jpg";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public CustomerDashboardFrame() {
        setTitle("Customer Dashboard - Online Shopping CSd");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel viewProductsPanel = createViewProductsPanel();
        tabbedPane.addTab("View Products", viewProductsPanel);

        JPanel viewCartPanel = createViewCartPanel();
        tabbedPane.addTab("My Cart", viewCartPanel);

        JPanel orderHistoryPanel = createOrderHistoryPanel();
        tabbedPane.addTab("My Orders", orderHistoryPanel);

        logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.addActionListener(e -> {
            OnlineShopping_last_App.currentUserId = -1;
            OnlineShopping_last_App.currentUserRole = "";
            OnlineShopping_last_App.currentUsername = "";
            new LoginFrame().setVisible(true); // Changed
            this.dispose();
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBackground(new Color(50,50,50));
        JLabel loggedInAsLabel = new JLabel("Logged in as: " + OnlineShopping_last_App.currentUsername + " (Customer)  ");
        loggedInAsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        loggedInAsLabel.setForeground(Color.WHITE);
        topPanel.add(loggedInAsLabel);
        topPanel.add(logoutButton);

        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        loadProducts();
        loadCart();
        loadOrderHistory();
    }

    private BackgroundPanel createBackgroundPanelWithContent() {
        BackgroundPanel panel = new BackgroundPanel(BACKGROUND_PATH);
        panel.setLayout(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        return panel;
    }

    private JPanel createViewProductsPanel() {
        BackgroundPanel panel = createBackgroundPanelWithContent();
        JLabel titleLabel = new JLabel("Available Products", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22)); titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.NORTH);

        productTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Description", "Price", "Stock", "Category"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        productTable = new JTable(productTableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(productTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        JTextField quantityField = new JTextField("1", 5);
        quantityField.setFont(new Font("Arial", Font.PLAIN, 14));
        JButton addToCartButton = new JButton("Add to Cart");
        addToCartButton.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel qtyLabel = new JLabel("Quantity:");
        qtyLabel.setForeground(Color.WHITE); qtyLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        bottomPanel.add(qtyLabel); bottomPanel.add(quantityField); bottomPanel.add(addToCartButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        addToCartButton.addActionListener(e -> {
            int selectedRowView = productTable.getSelectedRow();
            if (selectedRowView == -1) {
                JOptionPane.showMessageDialog(this, "Please select a product to add to cart.", "No Product Selected", JOptionPane.WARNING_MESSAGE); return;
            }
            try {
                int selectedRowModel = productTable.convertRowIndexToModel(selectedRowView);
                int productId = (int) productTableModel.getValueAt(selectedRowModel, 0);
                int quantity = Integer.parseInt(quantityField.getText());
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(this, "Quantity must be positive.", "Invalid Quantity", JOptionPane.ERROR_MESSAGE); return;
                }
                addProductToCart(productId, quantity);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid quantity format.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }

    private JPanel createViewCartPanel() {
        BackgroundPanel panel = createBackgroundPanelWithContent();
        JLabel titleLabel = new JLabel("My Shopping Cart", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22)); titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.NORTH);

        cartTableModel = new DefaultTableModel(new String[]{"Product ID", "Product Name", "Quantity", "Unit Price", "Total Price"}, 0){
            @Override public boolean isCellEditable(int row, int column) { return column == 2; }
        };
        cartTable = new JTable(cartTableModel);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(cartTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        JButton removeFromCartButton = new JButton("Remove Selected");
        removeFromCartButton.setFont(new Font("Arial", Font.BOLD, 14));
        JButton updateQuantityButton = new JButton("Update Quantity");
        updateQuantityButton.setFont(new Font("Arial", Font.BOLD, 14));
        JButton checkoutButton = new JButton("Proceed to Checkout");
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 16));
        checkoutButton.setBackground(new Color(0,128,0)); checkoutButton.setForeground(Color.WHITE);
        bottomPanel.add(removeFromCartButton); bottomPanel.add(updateQuantityButton); bottomPanel.add(checkoutButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        removeFromCartButton.addActionListener(e -> {
            int selectedRow = cartTable.getSelectedRow();
            if (selectedRow != -1) {
                int productId = (int) cartTableModel.getValueAt(selectedRow, 0);
                removeProductFromCart(productId);
            } else {
                JOptionPane.showMessageDialog(this, "Select an item to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        updateQuantityButton.addActionListener(e -> {
            int selectedRow = cartTable.getSelectedRow();
            if (selectedRow != -1) {
                if (cartTable.isEditing()) cartTable.getCellEditor().stopCellEditing();
                try {
                    int productId = (int) cartTableModel.getValueAt(selectedRow, 0);
                    int newQuantity = Integer.parseInt(cartTableModel.getValueAt(selectedRow, 2).toString());
                    if (newQuantity <= 0) {
                         JOptionPane.showMessageDialog(this, "Quantity must be positive. To remove, use 'Remove Selected'.", "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
                         loadCart(); return;
                    }
                    updateCartQuantity(productId, newQuantity);
                } catch (NumberFormatException ex) {
                     JOptionPane.showMessageDialog(this, "Invalid quantity format in table. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                     loadCart();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select an item to update its quantity.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        checkoutButton.addActionListener(e -> handleCheckout());
        return panel;
    }

    private JPanel createOrderHistoryPanel() {
        BackgroundPanel panel = createBackgroundPanelWithContent();
        JLabel titleLabel = new JLabel("My Order History", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22)); titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.NORTH);

        orderHistoryTableModel = new DefaultTableModel(new String[]{"Order ID", "Date", "Total Amount", "Status", "Shipping Address"}, 0){
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        orderHistoryTable = new JTable(orderHistoryTableModel);
        orderHistoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderHistoryTable.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(orderHistoryTable), BorderLayout.CENTER);

        JButton viewOrderDetailsButton = new JButton("View Selected Order Details");
        viewOrderDetailsButton.setFont(new Font("Arial", Font.BOLD, 14));
        viewOrderDetailsButton.addActionListener(e -> viewCustomerOrderDetails());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false); bottomPanel.add(viewOrderDetailsButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }
    
    private void viewCustomerOrderDetails() {
        int selectedRowView = orderHistoryTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order to view its details.", "No Order Selected", JOptionPane.WARNING_MESSAGE); return;
        }
        int selectedRowModel = orderHistoryTable.convertRowIndexToModel(selectedRowView);
        int orderId = (int) orderHistoryTableModel.getValueAt(selectedRowModel, 0);

        JDialog detailsDialog = new JDialog(this, "Order Details - ID: " + orderId, true);
        detailsDialog.setSize(600, 400); detailsDialog.setLayout(new BorderLayout(5,5));
        detailsDialog.setLocationRelativeTo(this);

        DefaultTableModel itemsModel = new DefaultTableModel(
            new String[]{"Product Name", "Quantity", "Price at Purchase", "Item Total"}, 0) {
             @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable itemsTable = new JTable(itemsModel); itemsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        
        String sql = "SELECT p.name, oi.quantity, oi.price_at_purchase " +
                     "FROM order_items oi JOIN products p ON oi.product_id = p.product_id " +
                     "WHERE oi.order_id = ?";
        double orderTotalFromItems = 0;
        try (Connection conn = OnlineShopping_last_App.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) { // Changed
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                double price = rs.getDouble("price_at_purchase"); int quantity = rs.getInt("quantity");
                double itemTotal = price * quantity; orderTotalFromItems += itemTotal;
                itemsModel.addRow(new Object[]{
                    rs.getString("name"), quantity, String.format("%.2f", price), String.format("%.2f", itemTotal)
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(detailsDialog, "Error loading order items: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
        detailsDialog.add(new JScrollPane(itemsTable), BorderLayout.CENTER);
        JLabel totalLabel = new JLabel("Total: " + String.format("%.2f", orderTotalFromItems), SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setBorder(BorderFactory.createEmptyBorder(5,0,5,10));
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.addActionListener(e -> detailsDialog.dispose());
        JPanel bottomPanelDialog = new JPanel(new BorderLayout());
        bottomPanelDialog.add(totalLabel, BorderLayout.WEST);
        JPanel closeButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closeButtonPanel.add(closeButton);
        bottomPanelDialog.add(closeButtonPanel, BorderLayout.EAST);
        detailsDialog.add(bottomPanelDialog, BorderLayout.SOUTH);
        detailsDialog.setVisible(true);
    }

    private void loadProducts() {
        productTableModel.setRowCount(0);
        String sql = "SELECT product_id, name, description, price, stock_quantity, category FROM products WHERE stock_quantity > 0 ORDER BY name";
        try (Connection conn = OnlineShopping_last_App.getConnection(); // Changed
             Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                productTableModel.addRow(new Object[]{
                    rs.getInt("product_id"), rs.getString("name"), rs.getString("description"),
                    String.format("%.2f",rs.getDouble("price")), rs.getInt("stock_quantity"), rs.getString("category")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addProductToCart(int productId, int quantity) {
        String stockSql = "SELECT stock_quantity FROM products WHERE product_id = ?";
        int availableStock = 0;
        try (Connection conn = OnlineShopping_last_App.getConnection(); PreparedStatement stockPstmt = conn.prepareStatement(stockSql)) { // Changed
            stockPstmt.setInt(1, productId);
            ResultSet stockRs = stockPstmt.executeQuery();
            if (stockRs.next()) { availableStock = stockRs.getInt("stock_quantity"); }
            else { JOptionPane.showMessageDialog(this, "Product not found.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error checking stock: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE); return;
        }

        int currentCartQuantity = 0;
        String cartCheckSql = "SELECT quantity FROM cart WHERE user_id = ? AND product_id = ?";
        try (Connection conn = OnlineShopping_last_App.getConnection(); PreparedStatement cartCheckPstmt = conn.prepareStatement(cartCheckSql)) { // Changed
            cartCheckPstmt.setInt(1, OnlineShopping_last_App.currentUserId); cartCheckPstmt.setInt(2, productId);
            ResultSet cartRs = cartCheckPstmt.executeQuery();
            if (cartRs.next()) { currentCartQuantity = cartRs.getInt("quantity"); }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error checking cart quantity: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE); return;
        }

        if (currentCartQuantity + quantity > availableStock) {
            JOptionPane.showMessageDialog(this, "Cannot add " + quantity + " item(s). Total quantity (" + (currentCartQuantity + quantity) +
                                            ") would exceed available stock (" + availableStock + "). You have " + currentCartQuantity + " in cart.",
                                            "Stock Error", JOptionPane.WARNING_MESSAGE); return;
        }

        String sql = "INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
        try (Connection conn = OnlineShopping_last_App.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) { // Changed
            pstmt.setInt(1, OnlineShopping_last_App.currentUserId); pstmt.setInt(2, productId); pstmt.setInt(3, quantity);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Product added to cart!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCart();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding to cart: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCart() {
        cartTableModel.setRowCount(0);
        String sql = "SELECT c.product_id, p.name, c.quantity, p.price " +
                     "FROM cart c JOIN products p ON c.product_id = p.product_id " +
                     "WHERE c.user_id = ?";
        try (Connection conn = OnlineShopping_last_App.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) { // Changed
            pstmt.setInt(1, OnlineShopping_last_App.currentUserId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                double price = rs.getDouble("price"); int quantityVal = rs.getInt("quantity");
                double itemTotal = price * quantityVal;
                cartTableModel.addRow(new Object[]{
                    rs.getInt("product_id"), rs.getString("name"), quantityVal,
                    String.format("%.2f", price), String.format("%.2f", itemTotal)
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading cart: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeProductFromCart(int productId) {
        String sql = "DELETE FROM cart WHERE user_id = ? AND product_id = ?";
        try (Connection conn = OnlineShopping_last_App.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) { // Changed
            pstmt.setInt(1, OnlineShopping_last_App.currentUserId); pstmt.setInt(2, productId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) { JOptionPane.showMessageDialog(this, "Product removed from cart.", "Success", JOptionPane.INFORMATION_MESSAGE); }
            loadCart();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error removing from cart: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCartQuantity(int productId, int newQuantity) {
        String stockSql = "SELECT stock_quantity FROM products WHERE product_id = ?";
        try (Connection conn = OnlineShopping_last_App.getConnection(); PreparedStatement stockPstmt = conn.prepareStatement(stockSql)) { // Changed
            stockPstmt.setInt(1, productId);
            ResultSet stockRs = stockPstmt.executeQuery();
            if (stockRs.next()) {
                int stock = stockRs.getInt("stock_quantity");
                if (newQuantity > stock) {
                    JOptionPane.showMessageDialog(this, "Not enough stock available. Max " + stock + " allowed. Cart not updated.", "Stock Error", JOptionPane.WARNING_MESSAGE);
                    loadCart(); return;
                }
            } else { JOptionPane.showMessageDialog(this, "Product not found.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error checking stock: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE); return;
        }

        String sql = "UPDATE cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        try (Connection conn = OnlineShopping_last_App.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) { // Changed
            pstmt.setInt(1, newQuantity); pstmt.setInt(2, OnlineShopping_last_App.currentUserId); pstmt.setInt(3, productId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) { JOptionPane.showMessageDialog(this, "Cart quantity updated.", "Success", JOptionPane.INFORMATION_MESSAGE); }
            loadCart();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating cart quantity: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCheckout() {
        if (cartTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Your cart is empty. Add items before checking out.", "Checkout Error", JOptionPane.WARNING_MESSAGE); return;
        }

        String shippingAddress = ""; String userSavedAddress = "";
        try (Connection connAddr = OnlineShopping_last_App.getConnection(); // Changed
             PreparedStatement pstmtAddr = connAddr.prepareStatement("SELECT address FROM users WHERE user_id = ?")) {
            pstmtAddr.setInt(1, OnlineShopping_last_App.currentUserId);
            ResultSet rsAddr = pstmtAddr.executeQuery();
            if (rsAddr.next() && rsAddr.getString("address") != null && !rsAddr.getString("address").trim().isEmpty()) {
                userSavedAddress = rsAddr.getString("address").trim();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error fetching saved address: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }

        if (!userSavedAddress.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(this, "Use your saved address?\n" + userSavedAddress, "Confirm Shipping Address", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) { shippingAddress = userSavedAddress; }
            else if (choice == JOptionPane.NO_OPTION) { shippingAddress = JOptionPane.showInputDialog(this, "Enter new shipping address:", "Shipping Information", JOptionPane.PLAIN_MESSAGE); }
            else { JOptionPane.showMessageDialog(this, "Checkout cancelled.", "Checkout", JOptionPane.INFORMATION_MESSAGE); return; }
        } else { shippingAddress = JOptionPane.showInputDialog(this, "Enter your shipping address:", "Shipping Information", JOptionPane.PLAIN_MESSAGE); }

        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Shipping address is required to proceed.", "Checkout Error", JOptionPane.ERROR_MESSAGE); return;
        }
        shippingAddress = shippingAddress.trim();

        String[] paymentOptions = {"CBE", "TeleBirr", "Other Digital Wallet"};
        String paymentMethod = (String) JOptionPane.showInputDialog(this, "Select Payment Method:", "Payment Details", JOptionPane.QUESTION_MESSAGE, null, paymentOptions, paymentOptions[0]);
        if (paymentMethod == null) { JOptionPane.showMessageDialog(this, "Payment method selection cancelled. Checkout aborted.", "Checkout Cancelled", JOptionPane.INFORMATION_MESSAGE); return; }
        String tid = JOptionPane.showInputDialog(this, "Enter Transaction ID (TID) for " + paymentMethod + ":", "Payment Transaction ID", JOptionPane.PLAIN_MESSAGE);
        if (tid == null || tid.trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Transaction ID is required for payment. Checkout aborted.", "Checkout Error", JOptionPane.ERROR_MESSAGE); return; }
        tid = tid.trim();

        Connection conn = null;
        try {
            conn = OnlineShopping_last_App.getConnection(); conn.setAutoCommit(false); // Changed
            double totalAmount = 0; List<Object[]> itemsForOrder = new ArrayList<>();
            String cartSql = "SELECT c.product_id, p.name, c.quantity, p.price, p.stock_quantity " +
                             "FROM cart c JOIN products p ON c.product_id = p.product_id " +
                             "WHERE c.user_id = ? FOR UPDATE";
            try (PreparedStatement cartPstmt = conn.prepareStatement(cartSql)) {
                cartPstmt.setInt(1, OnlineShopping_last_App.currentUserId);
                ResultSet rs = cartPstmt.executeQuery();
                if (!rs.isBeforeFirst() ) { throw new SQLException("Your cart is empty or items are unavailable."); }
                while (rs.next()) {
                    int productId = rs.getInt("product_id"); int quantityInCart = rs.getInt("quantity");
                    double price = rs.getDouble("price"); int stock = rs.getInt("stock_quantity");
                    if (quantityInCart > stock) { throw new SQLException("Not enough stock for product: " + rs.getString("name") + " (Requested: " + quantityInCart + ", Available: " + stock + "). Order cancelled."); }
                    itemsForOrder.add(new Object[]{productId, quantityInCart, price});
                    totalAmount += price * quantityInCart;
                }
            }
            if (itemsForOrder.isEmpty()) { throw new SQLException("Failed to retrieve cart items for checkout."); }

            String orderSql = "INSERT INTO orders (user_id, total_amount, status, shipping_address) VALUES (?, ?, 'pending', ?)";
            int orderId = -1;
            try (PreparedStatement orderPstmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                orderPstmt.setInt(1, OnlineShopping_last_App.currentUserId); orderPstmt.setDouble(2, totalAmount);
                orderPstmt.setString(3, shippingAddress); orderPstmt.executeUpdate();
                ResultSet generatedKeys = orderPstmt.getGeneratedKeys();
                if (generatedKeys.next()) { orderId = generatedKeys.getInt(1); }
                else { throw new SQLException("Creating order failed, no ID obtained."); }
            }

            String orderItemSql = "INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";
            String updateStockSql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
            try (PreparedStatement itemPstmt = conn.prepareStatement(orderItemSql);
                 PreparedStatement stockUpdatePstmt = conn.prepareStatement(updateStockSql)) {
                for (Object[] item : itemsForOrder) {
                    int productId = (int) item[0]; int quantityVal = (int) item[1]; double price = (double) item[2];
                    itemPstmt.setInt(1, orderId); itemPstmt.setInt(2, productId);
                    itemPstmt.setInt(3, quantityVal); itemPstmt.setDouble(4, price); itemPstmt.addBatch();
                    stockUpdatePstmt.setInt(1, quantityVal); stockUpdatePstmt.setInt(2, productId); stockUpdatePstmt.addBatch();
                }
                itemPstmt.executeBatch(); stockUpdatePstmt.executeBatch();
            }

            String paymentSql = "INSERT INTO payments (order_id, payment_method, transaction_id, amount, status) VALUES (?, ?, ?, ?, 'completed')";
            try (PreparedStatement paymentPstmt = conn.prepareStatement(paymentSql)) {
                paymentPstmt.setInt(1, orderId); paymentPstmt.setString(2, paymentMethod);
                paymentPstmt.setString(3, tid); paymentPstmt.setDouble(4, totalAmount); paymentPstmt.executeUpdate();
            }

            String clearCartSql = "DELETE FROM cart WHERE user_id = ?";
            try (PreparedStatement clearCartPstmt = conn.prepareStatement(clearCartSql)) {
                clearCartPstmt.setInt(1, OnlineShopping_last_App.currentUserId); clearCartPstmt.executeUpdate();
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Order placed successfully! Your Order ID is: " + orderId, "Checkout Success", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("New Order #" + orderId + " created by " + OnlineShopping_last_App.currentUsername + ". Notifying Admin and relevant Sellers (via their dashboards).");
            loadProducts(); loadCart(); loadOrderHistory();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Checkout failed: " + ex.getMessage(), "Transaction Error", JOptionPane.ERROR_MESSAGE);
            if (conn != null) { try { conn.rollback(); System.err.println("Transaction rolled back."); } catch (SQLException eRollback) { System.err.println("Rollback failed: " + eRollback.getMessage()); } }
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException exClose) { System.err.println("Failed to reset auto-commit or close connection: " + exClose.getMessage()); } }
        }
    }

    private void loadOrderHistory() {
        orderHistoryTableModel.setRowCount(0);
        String sql = "SELECT order_id, order_date, total_amount, status, shipping_address FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        try (Connection conn = OnlineShopping_last_App.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) { // Changed
            pstmt.setInt(1, OnlineShopping_last_App.currentUserId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                orderHistoryTableModel.addRow(new Object[]{
                    rs.getInt("order_id"), sdf.format(rs.getTimestamp("order_date")),
                    String.format("%.2f", rs.getDouble("total_amount")), rs.getString("status"), rs.getString("shipping_address")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading order history: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}