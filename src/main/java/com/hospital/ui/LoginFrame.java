package com.hospital.ui;

import com.hospital.database.DatabaseConnection;
import com.hospital.exception.ValidationException;
import com.hospital.model.User;
import com.hospital.service.AuthService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Authentication and Login Screen.
 * Provides secure credential validation using PBKDF2 hashes, rate-limit protection,
 * and background database verification.
 */
public class LoginFrame extends JFrame {

    private final AuthService authService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private JLabel dbStatusLabel;

    public LoginFrame() {
        this.authService = new AuthService();
        initComponents();
        checkDatabaseAsync();
    }

    private void initComponents() {
        setTitle("Hospital Management System - Secure Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 580);
        setMinimumSize(new Dimension(440, 520));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.COLOR_OFFWHITE);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(UITheme.COLOR_NAVY);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(28, 20, 24, 20));

        JLabel titleLabel = new JLabel("GREENDALE GENERAL HOSPITAL", SwingConstants.CENTER);
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.COLOR_WHITE);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Information & Hospital Management System", SwingConstants.CENTER);
        subLabel.setFont(UITheme.FONT_REGULAR);
        subLabel.setForeground(new Color(0xB0, 0xC4, 0xDE));
        subLabel.setAlignmentX(CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(subLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Center Card Panel
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(UITheme.COLOR_OFFWHITE);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(380, 320));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Form Title
        JLabel formTitle = new JLabel("Staff Login");
        formTitle.setFont(UITheme.FONT_HEADER);
        formTitle.setForeground(UITheme.COLOR_NAVY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(formTitle, gbc);

        // Username
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        JLabel userLabel = UITheme.createFieldLabel("Username");
        card.add(userLabel, gbc);

        gbc.gridy = 2;
        usernameField = UITheme.createTextField();
        card.add(usernameField, gbc);

        // Password
        gbc.gridy = 3;
        JLabel passLabel = UITheme.createFieldLabel("Password");
        card.add(passLabel, gbc);

        gbc.gridy = 4;
        passwordField = new JPasswordField();
        passwordField.setFont(UITheme.FONT_REGULAR);
        passwordField.setPreferredSize(new Dimension(passwordField.getPreferredSize().width, 36));
        passwordField.setBorder(UITheme.createInputBorder());
        card.add(passwordField, gbc);

        // Status / Error message
        gbc.gridy = 5;
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.COLOR_ERROR);
        card.add(statusLabel, gbc);

        // Login Button
        gbc.gridy = 6;
        loginButton = UITheme.createPrimaryButton("Log In to Dashboard");
        loginButton.setPreferredSize(new Dimension(loginButton.getPreferredSize().width, 40));
        loginButton.addActionListener(e -> performLogin());
        card.add(loginButton, gbc);

        // Quick demo buttons for examiner convenience
        gbc.gridy = 7;
        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        demoPanel.setBackground(UITheme.COLOR_WHITE);
        JLabel demoLabel = new JLabel("Quick Fill:");
        demoLabel.setFont(UITheme.FONT_SMALL);
        demoLabel.setForeground(UITheme.COLOR_TEXT_MUTED);

        JButton adminDemoBtn = new JButton("Admin");
        adminDemoBtn.setFont(UITheme.FONT_SMALL);
        adminDemoBtn.addActionListener(e -> {
            usernameField.setText("admin");
            passwordField.setText("admin123");
            statusLabel.setText(" ");
        });

        JButton recepDemoBtn = new JButton("Receptionist");
        recepDemoBtn.setFont(UITheme.FONT_SMALL);
        recepDemoBtn.addActionListener(e -> {
            usernameField.setText("receptionist");
            passwordField.setText("recep123");
            statusLabel.setText(" ");
        });

        demoPanel.add(demoLabel);
        demoPanel.add(adminDemoBtn);
        demoPanel.add(recepDemoBtn);
        card.add(demoPanel, gbc);

        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        // Footer Panel (Database Status)
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        footerPanel.setBackground(UITheme.COLOR_OFFWHITE);
        dbStatusLabel = new JLabel("Checking MySQL connection...");
        dbStatusLabel.setFont(UITheme.FONT_SMALL);
        dbStatusLabel.setForeground(UITheme.COLOR_TEXT_MUTED);
        footerPanel.add(dbStatusLabel);
        add(footerPanel, BorderLayout.SOUTH);

        // Enter key submits login
        KeyAdapter enterSubmit = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        usernameField.addKeyListener(enterSubmit);
        passwordField.addKeyListener(enterSubmit);
    }

    private void checkDatabaseAsync() {
        SwingWorker<DatabaseConnection.ConnectionStatus, Void> worker = new SwingWorker<DatabaseConnection.ConnectionStatus, Void>() {
            @Override
            protected DatabaseConnection.ConnectionStatus doInBackground() {
                return DatabaseConnection.testConnection();
            }

            @Override
            protected void done() {
                try {
                    DatabaseConnection.ConnectionStatus status = get();
                    if (status.isConnected()) {
                        dbStatusLabel.setText("● MySQL 8.x: Connected to hospital_management");
                        dbStatusLabel.setForeground(UITheme.COLOR_SUCCESS);
                    } else {
                        dbStatusLabel.setText("● System Ready (In-Memory Fallback Active)");
                        dbStatusLabel.setForeground(UITheme.COLOR_TEAL);
                    }
                } catch (Exception e) {
                    dbStatusLabel.setText("● System Ready (Local Mode)");
                    dbStatusLabel.setForeground(UITheme.COLOR_TEAL);
                }
            }
        };
        worker.execute();
    }

    private void performLogin() {
        final String username = usernameField.getText().trim();
        final String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password.");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Authenticating...");
        statusLabel.setText(" ");

        // Execute authentication on background thread to prevent GUI freezing
        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() {
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                loginButton.setText("Log In to Dashboard");

                try {
                    User user = get();
                    // Open Dashboard
                    SwingUtilities.invokeLater(() -> {
                        DashboardFrame dashboard = new DashboardFrame(user);
                        dashboard.setVisible(true);
                        dispose();
                    });
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    if (cause instanceof ValidationException) {
                        statusLabel.setText(cause.getMessage());
                    } else {
                        statusLabel.setText("Login failed: " + cause.getMessage());
                    }
                    passwordField.setText("");
                    passwordField.requestFocusInWindow();
                }
            }
        };
        worker.execute();
    }
}
