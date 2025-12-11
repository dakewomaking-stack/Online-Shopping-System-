package onlineshopping_last_app;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OnlineShopping_last_App {

    // --- Shared User Session Information ---
    public static int currentUserId = -1;
    public static String currentUserRole = "";
    public static String currentUsername = "";

    // --- Database Connection Details & Method ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/online_shopping_CSd";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1672"; // Change if your password is different

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "MySQL JDBC Driver not found!", "Driver Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed to connect to database: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // For developer console
            System.exit(1);
        }
        return connection;
    }

    // --- Main Method to Start the Application ---
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Nimbus L&F not found, using default.");
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(); // Changed from OnlineShoppingCSfdApp.LoginFrame
            loginFrame.setVisible(true);
        });
    }
}