package onlineshopping_last_app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton loginButton, registerButton;

    public LoginFrame() {
        setTitle("Login - Online Shopping CSd");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        String bgPath = "C:\\Users\\ABCD\\Desktop\\java\\GUI\\LIphoto_2018-11-06_04-41-42.jpg"; // UPDATE PATH IF NEEDED
        BackgroundPanel backgroundPanel = new BackgroundPanel(bgPath);
        backgroundPanel.setLayout(new GridBagLayout());
        setContentPane(backgroundPanel);

        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setOpaque(false);
        loginPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        loginPanel.setBackground(new Color(0,0,0,100)); // Semi-transparent black

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("User Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(roleLabel, gbc);

        roleComboBox = new JComboBox<>(new String[]{"Customer", "Seller", "Admin"});
        roleComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 1;
        loginPanel.add(roleComboBox, gbc);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 2;
        loginPanel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 3;
        loginPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 3;
        loginPanel.add(passwordField, gbc);

        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginButton, gbc);

        registerButton = new JButton("Register New Customer");
        registerButton.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 5;
        loginPanel.add(registerButton, gbc);

        GridBagConstraints mainGbc = new GridBagConstraints();
        backgroundPanel.add(loginPanel, mainGbc);

        roleComboBox.addActionListener(e -> prefillCredentials());
        prefillCredentials(); // Prefill based on default selection

        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> openRegistrationForm());
    }
/*
    private void prefillCredentials() {
        String selectedRole = (String) roleComboBox.getSelectedItem();
        if (selectedRole != null) {
            switch (selectedRole) {
                case "Admin":
                    usernameField.setText("  ");
                    passwordField.setText("  ");
                    break;
                case "Seller":
                    usernameField.setText("");
                    passwordField.setText("");
                    break;
                case "Customer":
                    usernameField.setText("");
                    passwordField.setText("");
                    break;
                default:
                    usernameField.setText("");
                    passwordField.setText("");
                    break;
            }
        }
    }
*/
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = ((String) roleComboBox.getSelectedItem()).toLowerCase();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and Password cannot be empty.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String query = "SELECT user_id, username, role FROM users WHERE username = ? AND password = ? AND role = ?";
        String updateLoginTimeSql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (Connection conn = OnlineShopping_last_App.getConnection(); // Changed from OnlineShoppingCSfdApp
             PreparedStatement pstmt = conn.prepareStatement(query);
             PreparedStatement updateLoginStmt = conn.prepareStatement(updateLoginTimeSql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                OnlineShopping_last_App.currentUserId = rs.getInt("user_id");
                OnlineShopping_last_App.currentUserRole = rs.getString("role");
                OnlineShopping_last_App.currentUsername = rs.getString("username");

                // Update last_login time
                updateLoginStmt.setInt(1, OnlineShopping_last_App.currentUserId);
                updateLoginStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Login Successful! Welcome " + username, "Login Success", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();

                switch (role) {
                    case "admin":
                        new AdminDashboardFrame().setVisible(true); // Changed
                        break;
                    case "seller":
                        new SellerDashboardFrame().setVisible(true); // Changed
                        break;
                    case "customer":
                        new CustomerDashboardFrame().setVisible(true); // Changed
                        break;
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username, password, or role.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error during login: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void openRegistrationForm() {
        this.dispose();
        new RegistrationForm().setVisible(true); // Changed
    }
}