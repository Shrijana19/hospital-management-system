package com.hospital;

import com.hospital.database.DatabaseConnection;
import com.hospital.ui.LoginFrame;
import com.hospital.ui.UITheme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application entry point for the Hospital Management System.
 * 1. Configures clean system look and feel.
 * 2. Runs startup database connectivity diagnostic.
 * 3. Launches the Login interface on the Swing Event Dispatch Thread (EDT).
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // Check if Web mode was requested
        for (String arg : args) {
            if ("--web".equalsIgnoreCase(arg) || "-web".equalsIgnoreCase(arg) || "web".equalsIgnoreCase(arg)) {
                LOGGER.info("Starting in Web Application mode on http://localhost:8080/...");
                com.hospital.web.WebServer.main(new String[]{"8080"});
                return;
            }
        }

        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not set system look and feel", e);
        }

        // Launch on EDT
        SwingUtilities.invokeLater(() -> {
            LOGGER.info("Starting Hospital Management System...");

            // Perform pre-flight connection check
            DatabaseConnection.ConnectionStatus status = DatabaseConnection.testConnection();

            if (!status.isConnected()) {
                LOGGER.warning("Database connection check on startup: " + status.getMessage());
            } else {
                LOGGER.info("Startup check: MySQL connected successfully.");
            }

            // Launch login frame
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }

    /**
     * Friendly startup modal shown if MySQL is not running or misconfigured.
     */
    private static void showStartupDbWarning(String diagnosticMessage) {
        JDialog dialog = new JDialog((java.awt.Frame) null, "Database Connectivity Notice", true);
        dialog.setSize(560, 360);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout(12, 12));
        dialog.getContentPane().setBackground(UITheme.COLOR_OFFWHITE);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        header.setBackground(UITheme.COLOR_NAVY);
        JLabel title = new JLabel("MySQL Database Notice");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.COLOR_WHITE);
        header.add(title);
        dialog.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setBackground(UITheme.COLOR_OFFWHITE);
        body.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel info = new JLabel("<html><b>The application could not establish a connection to MySQL.</b><br>"
                + "You can still inspect the user interface, but database operations will require MySQL running.</html>");
        info.setFont(UITheme.FONT_REGULAR);
        info.setForeground(UITheme.COLOR_TEXT);
        body.add(info, BorderLayout.NORTH);

        JTextArea errorArea = new JTextArea(diagnosticMessage);
        errorArea.setFont(UITheme.FONT_MONO);
        errorArea.setEditable(false);
        errorArea.setLineWrap(true);
        errorArea.setWrapStyleWord(true);
        errorArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        body.add(new JScrollPane(errorArea), BorderLayout.CENTER);

        dialog.add(body, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(UITheme.COLOR_OFFWHITE);

        JButton retryBtn = UITheme.createPrimaryButton("Retry Connection");
        retryBtn.addActionListener(e -> {
            DatabaseConnection.loadConfiguration();
            DatabaseConnection.ConnectionStatus retryStatus = DatabaseConnection.testConnection();
            if (retryStatus.isConnected()) {
                dialog.dispose();
            } else {
                errorArea.setText(retryStatus.getMessage());
            }
        });

        JButton continueBtn = UITheme.createOutlineButton("Continue Anyway");
        continueBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(retryBtn);
        btnPanel.add(continueBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
