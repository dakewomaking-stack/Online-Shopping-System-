package onlineshopping_last_app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
// import javax.swing.table.TableRowSorter; // Already imported via OnlineShopping_last_App, but good practice
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class AdminDashboardFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private JButton logoutButton;
    private final String BACKGROUND_PATH = "C:\\Users\\ABCD\\Desktop\\java\\GUI\\Aphoto_2024-05-06_11-10-20.jpg";

    private JTable productAdminTable;
    private DefaultTableModel productAdminTableModel;
    private JTable customerAdminTable;
    private DefaultTableModel customerAdminTableModel;
    private JTable sellerAdminTable;
    private DefaultTableModel sellerAdminTableModel;
    private JTable adminOrderTable;
    private DefaultTableModel adminOrderTableModel;
    private JComboBox<String> adminOrderStatusComboBox;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public AdminDashboardFrame() {
        setTitle("Admin Dashboard - Online Shopping CSd");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel statsPanel = createBackgroundPanelWithContent();
        statsPanel.setLayout(new GridBagLayout());
        JLabel welcomeLabel = new JLabel("<html><div style='text-align: center;'><h1>Welcome Admin!</h1><p>Full control over Products, Users, Sellers, and Orders.</div></html>", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        statsPanel.add(welcomeLabel, new GridBagConstraints());
        tabbedPane.addTab("Dashboard", statsPanel);

        JPanel productManagementPanel = createProductManagementPanel();
        tabbedPane.addTab("Manage Products", productManagementPanel);

        JPanel userManagementPanel = createUserManagementPanel();
        tabbedPane.addTab("Manage Customers", userManagementPanel);

        JPanel sellerManagementPanel = createSellerManagementPanel();
        tabbedPane.addTab("Manage Sellers", sellerManagementPanel);

        JPanel orderManagementPanel = createAdminOrderManagementPanel();
        tabbedPane.addTab("Manage Orders", orderManagementPanel);

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
        topPanel.setBackground(new Color(50, 50, 50));
        JLabel loggedInAsLabel = new JLabel("Logged in as: " + OnlineShopping_last_App.currentUsername + " (Admin)  ");
        loggedInAsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        loggedInAsLabel.setForeground(Color.WHITE);
        topPanel.add(loggedInAsLabel);
        topPanel.add(logoutButton);

        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        loadAdminProducts();
        loadAdminCustomers();
        loadAdminSellers();
        loadAdminOrders();
    }

    private BackgroundPanel createBackgroundPanelWithContent() {
        BackgroundPanel panel = new BackgroundPanel(BACKGROUND_PATH);
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        return panel;
    }

    private JPanel createProductManagementPanel() {
        BackgroundPanel panel = createBackgroundPanelWithContent();
        JLabel titleLabel = new JLabel("Product Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.NORTH);

        productAdminTableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Description", "Price", "Stock", "Category", "Image URL"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        productAdminTable = new JTable(productAdminTableModel);
        productAdminTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productAdminTable.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(productAdminTable), BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controlsPanel.setOpaque(false);
        JButton addProductButton = new JButton("Add Product");
        JButton editProductButton = new JButton("Edit Selected Product");
        JButton deleteProductButton = new JButton("Delete Selected Product");
        JButton refreshProductsButton = new JButton("Refresh Products");

        styleAdminButton(addProductButton);
        styleAdminButton(editProductButton);
        styleAdminButton(deleteProductButton);
        styleAdminButton(refreshProductsButton);

        controlsPanel.add(addProductButton);
        controlsPanel.add(editProductButton);
        controlsPanel.add(deleteProductButton);
        controlsPanel.add(refreshProductsButton);
        panel.add(controlsPanel, BorderLayout.SOUTH);

        addProductButton.addActionListener(e -> openAddProductDialog());
        editProductButton.addActionListener(e -> openEditProductDialog());
        deleteProductButton.addActionListener(e -> deleteAdminProduct());
        refreshProductsButton.addActionListener(e -> loadAdminProducts());
        return panel;
    }

    private void styleAdminButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
    }

    private void loadAdminProducts() {
        productAdminTableModel.setRowCount(0);
        String sql = "SELECT product_id, name, description, price, stock_quantity, category, image_url FROM products ORDER BY name";
        try (Connection conn = OnlineShopping_last_App.getConnection(); // Changed
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                productAdminTableModel.addRow(new Object[]{
                    rs.getInt("product_id"), rs.getString("name"), rs.getString("description"),
                    String.format("%.2f", rs.getDouble("price")), rs.getInt("stock_quantity"),
                    rs.getString("category"), rs.getString("image_url")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAddProductDialog() {
        JDialog addDialog = new JDialog(this, "Add New Product", true);
        addDialog.setSize(500, 400);
        addDialog.setLocationRelativeTo(this);
        addDialog.setLayout(new BorderLayout(10,10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20);
        JTextArea descArea = new JTextArea(3, 20);
        JTextField priceField = new JTextField(10);
        JTextField stockField = new JTextField(10);
        JTextField categoryField = new JTextField(15);
        JTextField imageUrlField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth=2; formPanel.add(nameField, gbc); gbc.gridwidth=1;
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth=2; formPanel.add(new JScrollPane(descArea), gbc); gbc.gridwidth=1;
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; formPanel.add(priceField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; formPanel.add(stockField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; formPanel.add(categoryField, gbc);
        gbc.gridx = 0; gbc.gridy = 5; formPanel.add(new JLabel("Image URL (Optional):"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth=2; formPanel.add(imageUrlField, gbc);

        addDialog.add(formPanel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save Product");
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim(); String desc = descArea.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                int stock = Integer.parseInt(stockField.getText().trim());
                String category = categoryField.getText().trim(); String imageUrl = imageUrlField.getText().trim();

                if (name.isEmpty() || desc.isEmpty() || category.isEmpty() || price <= 0 || stock < 0) {
                    JOptionPane.showMessageDialog(addDialog, "Name, Description, Category, Price (>0), and Stock (>=0) are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String sql = "INSERT INTO products (name, description, price, stock_quantity, category, image_url) VALUES (?, ?, ?, ?, ?, ?)";
                try (Connection conn = OnlineShopping_last_App.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) { // Changed
                    pstmt.setString(1, name); pstmt.setString(2, desc); pstmt.setDouble(3, price);
                    pstmt.setInt(4, stock); pstmt.setString(5, category);
                    pstmt.setString(6, imageUrl.isEmpty() ? null : imageUrl);
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(addDialog, "Product added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAdminProducts(); addDialog.dispose();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(addDialog, "DB Error adding product: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(addDialog, "Invalid number format for Price or Stock.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> addDialog.dispose());
        buttonPanel.add(cancelButton);
        addDialog.add(buttonPanel, BorderLayout.SOUTH);
        addDialog.setVisible(true);
    }

    private void openEditProductDialog() {
        int selectedRow = productAdminTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit.", "No Product Selected", JOptionPane.WARNING_MESSAGE); return;
        }
        int modelRow = productAdminTable.convertRowIndexToModel(selectedRow);
        int productId = (int) productAdminTableModel.getValueAt(modelRow, 0);

        JDialog editDialog = new JDialog(this, "Edit Product - ID: " + productId, true);
        editDialog.setSize(500, 400); editDialog.setLocationRelativeTo(this);
        editDialog.setLayout(new BorderLayout(10,10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5); gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20); JTextArea descArea = new JTextArea(3, 20);
        JTextField priceField = new JTextField(10); JTextField stockField = new JTextField(10);
        JTextField categoryField = new JTextField(15); JTextField imageUrlField = new JTextField(20);

        try (Connection conn = OnlineShopping_last_App.getConnection(); // Changed
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM products WHERE product_id = ?")) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nameField.setText(rs.getString("name")); descArea.setText(rs.getString("description"));
                priceField.setText(String.format("%.2f", rs.getDouble("price")));
                stockField.setText(String.valueOf(rs.getInt("stock_quantity")));
                categoryField.setText(rs.getString("category"));
                imageUrlField.setText(rs.getString("image_url") == null ? "" : rs.getString("image_url"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error fetching product details: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            editDialog.dispose(); return;
        }

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth=2; formPanel.add(nameField, gbc); gbc.gridwidth=1;
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth=2; formPanel.add(new JScrollPane(descArea), gbc); gbc.gridwidth=1;
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; formPanel.add(priceField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; formPanel.add(stockField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; formPanel.add(categoryField, gbc);
        gbc.gridx = 0; gbc.gridy = 5; formPanel.add(new JLabel("Image URL (Optional):"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth=2; formPanel.add(imageUrlField, gbc);
        editDialog.add(formPanel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim(); String desc = descArea.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                int stock = Integer.parseInt(stockField.getText().trim());
                String category = categoryField.getText().trim(); String imageUrl = imageUrlField.getText().trim();

                if (name.isEmpty() || desc.isEmpty() || category.isEmpty() || price <= 0 || stock < 0) {
                   JOptionPane.showMessageDialog(editDialog, "Name, Description, Category, Price (>0), and Stock (>=0) are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                   return;
               }
                String sql = "UPDATE products SET name=?, description=?, price=?, stock_quantity=?, category=?, image_url=? WHERE product_id=?";
                try (Connection conn = OnlineShopping_last_App.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) { // Changed
                    pstmt.setString(1, name); pstmt.setString(2, desc); pstmt.setDouble(3, price);
                    pstmt.setInt(4, stock); pstmt.setString(5, category);
                    pstmt.setString(6, imageUrl.isEmpty() ? null : imageUrl); pstmt.setInt(7, productId);
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(editDialog, "Product updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAdminProducts(); editDialog.dispose();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(editDialog, "DB Error updating product: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(editDialog, "Invalid number format for Price or Stock.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> editDialog.dispose());
        buttonPanel.add(cancelButton);
        editDialog.add(buttonPanel, BorderLayout.SOUTH);
        editDialog.setVisible(true);
    }

    private void deleteAdminProduct() {
        int selectedRow = productAdminTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.", "No Product Selected", JOptionPane.WARNING_MESSAGE); return;
        }
        int modelRow = productAdminTable.convertRowIndexToModel(selectedRow);
        int productId = (int) productAdminTableModel.getValueAt(modelRow, 0);
        String productName = (String) productAdminTableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete product '" + productName + "' (ID: " + productId + ")?\nThis may affect existing orders or seller assignments if not handled carefully.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String unassignSellerSql = "UPDATE users SET managed_product_id = NULL WHERE managed_product_id = ?";
            String deleteFromCartSql = "DELETE FROM cart WHERE product_id = ?";
            String deleteProductSql = "DELETE FROM products WHERE product_id = ?";
            Connection conn = null;
            try {
                conn = OnlineShopping_last_App.getConnection(); conn.setAutoCommit(false); // Changed
                try (PreparedStatement pstmtUnassign = conn.prepareStatement(unassignSellerSql)) { pstmtUnassign.setInt(1, productId); pstmtUnassign.executeUpdate(); }
                try (PreparedStatement pstmtCart = conn.prepareStatement(deleteFromCartSql)) { pstmtCart.setInt(1, productId); pstmtCart.executeUpdate(); }
                try (PreparedStatement pstmtDelete = conn.prepareStatement(deleteProductSql)) {
                    pstmtDelete.setInt(1, productId);
                    int affectedRows = pstmtDelete.executeUpdate();
                    if (affectedRows > 0) {
                        conn.commit();
                        JOptionPane.showMessageDialog(this, "Product '" + productName + "' deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadAdminProducts(); loadAdminSellers();
                    } else {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this, "Product could not be deleted.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                try { if (conn != null) conn.rollback(); } catch (SQLException eRollback) { eRollback.printStackTrace(); }
                JOptionPane.showMessageDialog(this, "DB Error deleting product: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException eClose) { eClose.printStackTrace(); }
            }
        }
    }

    private JPanel createUserManagementPanel() {
        BackgroundPanel panel = createBackgroundPanelWithContent();
        JLabel titleLabel = new JLabel("Customer Account Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22)); titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.NORTH);

        customerAdminTableModel = new DefaultTableModel(
            new String[]{"User ID", "Username", "Full Name", "Email", "Phone", "Address", "Registered", "Last Login"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        customerAdminTable = new JTable(customerAdminTableModel);
        customerAdminTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerAdminTable.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(customerAdminTable), BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controlsPanel.setOpaque(false);
        JButton deleteCustomerButton = new JButton("Delete Selected Customer");
        JButton refreshCustomersButton = new JButton("Refresh Customers");
        styleAdminButton(deleteCustomerButton); styleAdminButton(refreshCustomersButton);
        controlsPanel.add(deleteCustomerButton); controlsPanel.add(refreshCustomersButton);
        panel.add(controlsPanel, BorderLayout.SOUTH);

        deleteCustomerButton.addActionListener(e -> deleteAdminCustomer());
        refreshCustomersButton.addActionListener(e -> loadAdminCustomers());
        return panel;
    }

    private void loadAdminCustomers() {
        customerAdminTableModel.setRowCount(0);
        String sql = "SELECT user_id, username, full_name, email, phone_number, address, registration_date, last_login " +
                     "FROM users WHERE role = 'customer' ORDER BY username";
        try (Connection conn = OnlineShopping_last_App.getConnection(); // Changed
             Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Timestamp regDate = rs.getTimestamp("registration_date");
                Timestamp lastLogin = rs.getTimestamp("last_login");
                customerAdminTableModel.addRow(new Object[]{
                    rs.getInt("user_id"), rs.getString("username"), rs.getString("full_name"),
                    rs.getString("email"), rs.getString("phone_number"), rs.getString("address"),
                    regDate != null ? sdf.format(regDate) : "N/A",
                    lastLogin != null ? sdf.format(lastLogin) : "N/A"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading customers: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAdminCustomer() {
        int selectedRow = customerAdminTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete.", "No Customer Selected", JOptionPane.WARNING_MESSAGE); return;
        }
        int modelRow = customerAdminTable.convertRowIndexToModel(selectedRow);
        int userId = (int) customerAdminTableModel.getValueAt(modelRow, 0);
        String username = (String) customerAdminTableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete customer '" + username + "' (ID: " + userId + ")?\nThis will delete all their orders, cart items, etc. This action is IRREVERSIBLE.",
            "Confirm Delete Customer", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM users WHERE user_id = ? AND role = 'customer'";
            try (Connection conn = OnlineShopping_last_App.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) { // Changed
                pstmt.setInt(1, userId);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Customer '" + username + "' deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAdminCustomers(); loadAdminOrders();
                } else {
                    JOptionPane.showMessageDialog(this, "Customer could not be deleted or was not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "DB Error deleting customer: " + ex.getMessage() + "\nEnsure related data (orders, cart) is handled (e.g., ON DELETE CASCADE).", "DB Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private JPanel createSellerManagementPanel() {
        BackgroundPanel panel = createBackgroundPanelWithContent();
        JLabel titleLabel = new JLabel("Seller Account Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22)); titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.NORTH);

        sellerAdminTableModel = new DefaultTableModel(
            new String[]{"User ID", "Username", "Full Name", "Email", "Managed Product ID"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        sellerAdminTable = new JTable(sellerAdminTableModel);
        sellerAdminTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sellerAdminTable.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(sellerAdminTable), BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controlsPanel.setOpaque(false);
        JButton addSellerButton = new JButton("Add New Seller");
        JButton deleteSellerButton = new JButton("Delete Selected Seller");
        JButton assignProductButton = new JButton("Assign/Update Product for Seller");
        JButton refreshSellersButton = new JButton("Refresh Sellers");
        styleAdminButton(addSellerButton); styleAdminButton(deleteSellerButton);
        styleAdminButton(assignProductButton); styleAdminButton(refreshSellersButton);
        controlsPanel.add(addSellerButton); controlsPanel.add(deleteSellerButton);
        controlsPanel.add(assignProductButton); controlsPanel.add(refreshSellersButton);
        panel.add(controlsPanel, BorderLayout.SOUTH);

        addSellerButton.addActionListener(e -> openAddSellerDialog());
        deleteSellerButton.addActionListener(e -> deleteAdminSeller());
        assignProductButton.addActionListener(e -> openAssignProductToSellerDialog());
        refreshSellersButton.addActionListener(e -> loadAdminSellers());
        return panel;
    }

    private void loadAdminSellers() {
        sellerAdminTableModel.setRowCount(0);
        String sql = "SELECT user_id, username, full_name, email, managed_product_id FROM users WHERE role = 'seller' ORDER BY username";
        try (Connection conn = OnlineShopping_last_App.getConnection(); // Changed
             Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int managedProdId = rs.getInt("managed_product_id");
                sellerAdminTableModel.addRow(new Object[]{
                    rs.getInt("user_id"), rs.getString("username"), rs.getString("full_name"),
                    rs.getString("email"), rs.wasNull() ? "N/A" : managedProdId
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading sellers: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAddSellerDialog() {
        JDialog addDialog = new JDialog(this, "Add New Seller", true);
        addDialog.setSize(450, 350); addDialog.setLocationRelativeTo(this);
        addDialog.setLayout(new BorderLayout(10,10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5); gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField usernameField = new JTextField(20); JPasswordField passwordField = new JPasswordField(20);
        JTextField fullNameField = new JTextField(20); JTextField emailField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; formPanel.add(usernameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; formPanel.add(passwordField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; formPanel.add(fullNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; formPanel.add(emailField, gbc);
        addDialog.add(formPanel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save Seller");
        saveButton.addActionListener(e -> {
            String username = usernameField.getText().trim(); String password = new String(passwordField.getPassword());
            String fullName = fullNameField.getText().trim(); String email = emailField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(addDialog, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE); return;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                 JOptionPane.showMessageDialog(addDialog, "Invalid email format.", "Validation Error", JOptionPane.ERROR_MESSAGE); return;
            }
            String checkUserSql = "SELECT user_id FROM users WHERE username = ? OR email = ?";
            try (Connection conn = OnlineShopping_last_App.getConnection(); PreparedStatement checkStmt = conn.prepareStatement(checkUserSql)) { // Changed
                checkStmt.setString(1, username); checkStmt.setString(2, email);
                ResultSet rsCheck = checkStmt.executeQuery();
                if (rsCheck.next()) {
                    JOptionPane.showMessageDialog(addDialog, "Username or Email already exists.", "Registration Error", JOptionPane.ERROR_MESSAGE); return;
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(addDialog, "Database error checking user: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE); return;
            }
            String sql = "INSERT INTO users (username, password, role, full_name, email) VALUES (?, ?, 'seller', ?, ?)";
            try (Connection conn = OnlineShopping_last_App.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) { // Changed
                pstmt.setString(1, username); pstmt.setString(2, password);
                pstmt.setString(3, fullName); pstmt.setString(4, email);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(addDialog, "Seller added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAdminSellers(); addDialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(addDialog, "DB Error adding seller: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        JButton cancelButton = new JButton("Cancel"); cancelButton.addActionListener(e -> addDialog.dispose());
        buttonPanel.add(cancelButton);
        addDialog.add(buttonPanel, BorderLayout.SOUTH);
        addDialog.setVisible(true);
    }

    private void deleteAdminSeller() {
        int selectedRow = sellerAdminTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a seller to delete.", "No Seller Selected", JOptionPane.WARNING_MESSAGE); return;
        }
        int modelRow = sellerAdminTable.convertRowIndexToModel(selectedRow);
        int userId = (int) sellerAdminTableModel.getValueAt(modelRow, 0);
        String username = (String) sellerAdminTableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete seller '" + username + "' (ID: " + userId + ")?\nThis action is IRREVERSIBLE.",
            "Confirm Delete Seller", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM users WHERE user_id = ? AND role = 'seller'";
            try (Connection conn = OnlineShopping_last_App.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) { // Changed
                pstmt.setInt(1, userId);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Seller '" + username + "' deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAdminSellers();
                } else {
                    JOptionPane.showMessageDialog(this, "Seller could not be deleted or was not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "DB Error deleting seller: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openAssignProductToSellerDialog() {
        int selectedRow = sellerAdminTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a seller.", "No Seller Selected", JOptionPane.WARNING_MESSAGE); return;
        }
        int modelRow = sellerAdminTable.convertRowIndexToModel(selectedRow);
        int sellerId = (int) sellerAdminTableModel.getValueAt(modelRow, 0);
        String sellerUsername = (String) sellerAdminTableModel.getValueAt(modelRow, 1);
        Object currentManagedProdIdObj = sellerAdminTableModel.getValueAt(modelRow, 4);
        Integer currentManagedProdId = (currentManagedProdIdObj instanceof Integer) ? (Integer) currentManagedProdIdObj : null;

        JDialog assignDialog = new JDialog(this, "Assign Product to Seller: " + sellerUsername, true);
        assignDialog.setSize(450, 250); assignDialog.setLocationRelativeTo(this);
        assignDialog.setLayout(new BorderLayout(10,10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5); gbc.fill = GridBagConstraints.HORIZONTAL;

        Vector<ProductItem> availableProducts = new Vector<>();
        availableProducts.add(new ProductItem(-1, "Unassign Product (None)"));
        String productSql = "SELECT p.product_id, p.name FROM products p " +
                            "LEFT JOIN users u ON p.product_id = u.managed_product_id AND u.role = 'seller' " +
                            "WHERE u.user_id IS NULL OR u.user_id = ? " +
                            "ORDER BY p.name";
        try (Connection conn = OnlineShopping_last_App.getConnection(); PreparedStatement pstmt = conn.prepareStatement(productSql)) { // Changed
            pstmt.setInt(1, sellerId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                availableProducts.add(new ProductItem(rs.getInt("product_id"), rs.getInt("product_id") + ": " + rs.getString("name")));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading available products: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            assignDialog.dispose(); return;
        }

        JComboBox<ProductItem> productComboBox = new JComboBox<>(availableProducts);
        if (currentManagedProdId != null) {
            for (int i = 0; i < availableProducts.size(); i++) {
                if (availableProducts.get(i).getId() == currentManagedProdId) { productComboBox.setSelectedIndex(i); break; }
            }
        } else { productComboBox.setSelectedIndex(0); }

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Select Product:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; formPanel.add(productComboBox, gbc);
        assignDialog.add(formPanel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Update Assignment");
        saveButton.addActionListener(e -> {
            ProductItem selectedProduct = (ProductItem) productComboBox.getSelectedItem();
            Integer newProductId = (selectedProduct != null && selectedProduct.getId() != -1) ? selectedProduct.getId() : null;

            if (newProductId != null) {
                String checkSql = "SELECT username FROM users WHERE managed_product_id = ? AND user_id != ? AND role = 'seller'";
                try (Connection conn = OnlineShopping_last_App.getConnection(); PreparedStatement pstmtCheck = conn.prepareStatement(checkSql)) { // Changed
                    pstmtCheck.setInt(1, newProductId); pstmtCheck.setInt(2, sellerId);
                    ResultSet rsCheck = pstmtCheck.executeQuery();
                    if (rsCheck.next()) {
                        JOptionPane.showMessageDialog(assignDialog,
                            "Product ID " + newProductId + " is already managed by seller: " + rsCheck.getString("username") + ".\nPlease choose another product or unassign it first.",
                            "Product Already Managed", JOptionPane.ERROR_MESSAGE); return;
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(assignDialog, "DB Error checking product assignment: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE); return;
                }
            }
            String sql = "UPDATE users SET managed_product_id = ? WHERE user_id = ? AND role = 'seller'";
            try (Connection conn = OnlineShopping_last_App.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) { // Changed
                if (newProductId != null) { pstmt.setInt(1, newProductId); } else { pstmt.setNull(1, Types.INTEGER); }
                pstmt.setInt(2, sellerId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(assignDialog, "Seller's product assignment updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAdminSellers(); assignDialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(assignDialog, "DB Error updating assignment: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        JButton cancelButton = new JButton("Cancel"); cancelButton.addActionListener(e -> assignDialog.dispose());
        buttonPanel.add(cancelButton);
        assignDialog.add(buttonPanel, BorderLayout.SOUTH);
        assignDialog.setVisible(true);
    }

    private JPanel createAdminOrderManagementPanel() {
        BackgroundPanel panel = createBackgroundPanelWithContent();
        JLabel titleLabel = new JLabel("Manage All Orders", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22)); titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.NORTH);

        adminOrderTableModel = new DefaultTableModel(
            new String[]{"Order ID", "Customer", "Date", "Total", "Status", "Shipping Address"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        adminOrderTable = new JTable(adminOrderTableModel);
        adminOrderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        adminOrderTable.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(adminOrderTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controlsPanel.setOpaque(false);
        JButton refreshButton = new JButton("Refresh Orders");
        JButton viewDetailsButton = new JButton("View Order Details");
        JLabel statusLabel = new JLabel("Set Status:"); statusLabel.setForeground(Color.WHITE);
        adminOrderStatusComboBox = new JComboBox<>(new String[]{
            "pending", "processing", "ready_for_pickup",
            "shipped_by_seller", "shipped", "delivered", "cancelled"
        });
        adminOrderStatusComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        JButton updateStatusButton = new JButton("Update Status");
        styleAdminButton(refreshButton); styleAdminButton(viewDetailsButton); styleAdminButton(updateStatusButton);
        controlsPanel.add(refreshButton); controlsPanel.add(viewDetailsButton);
        controlsPanel.add(statusLabel); controlsPanel.add(adminOrderStatusComboBox);
        controlsPanel.add(updateStatusButton);
        panel.add(controlsPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> loadAdminOrders());
        viewDetailsButton.addActionListener(e -> viewAdminOrderDetails());
        updateStatusButton.addActionListener(e -> updateAdminOrderStatus());
        return panel;
    }

    private void loadAdminOrders() {
        adminOrderTableModel.setRowCount(0);
        String sql = "SELECT o.order_id, u.username AS customer_username, o.order_date, o.total_amount, o.status, o.shipping_address " +
                     "FROM orders o JOIN users u ON o.user_id = u.user_id ORDER BY o.order_date DESC";
        try (Connection conn = OnlineShopping_last_App.getConnection(); // Changed
             Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                adminOrderTableModel.addRow(new Object[]{
                    rs.getInt("order_id"), rs.getString("customer_username"),
                    sdf.format(rs.getTimestamp("order_date")), String.format("%.2f", rs.getDouble("total_amount")),
                    rs.getString("status"), rs.getString("shipping_address")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewAdminOrderDetails() {
        int selectedRow = adminOrderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order to view details.", "No Order Selected", JOptionPane.WARNING_MESSAGE); return;
        }
        int modelRow = adminOrderTable.convertRowIndexToModel(selectedRow);
        int orderId = (int) adminOrderTableModel.getValueAt(modelRow, 0);

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

    private void updateAdminOrderStatus() {
        int selectedRow = adminOrderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order to update.", "No Order Selected", JOptionPane.WARNING_MESSAGE); return;
        }
        int modelRow = adminOrderTable.convertRowIndexToModel(selectedRow);
        int orderId = (int) adminOrderTableModel.getValueAt(modelRow, 0);
        String newStatus = (String) adminOrderStatusComboBox.getSelectedItem();
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection conn = OnlineShopping_last_App.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) { // Changed
            pstmt.setString(1, newStatus); pstmt.setInt(2, orderId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Order status updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAdminOrders();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update order status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating order status: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}