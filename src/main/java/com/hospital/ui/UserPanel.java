package com.hospital.ui;

import com.hospital.dao.UserDao;
import com.hospital.exception.ValidationException;
import com.hospital.model.User;
import com.hospital.service.AuthService;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Staff & User Management Panel (Restricted to ADMIN role).
 * Allows creating and managing staff credentials with salted PBKDF2 hashing.
 */
public class UserPanel extends JPanel {

    private final AuthService authService;
    private final UserDao userDao;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;
    private JTextField fullNameField;

    private DefaultTableModel tableModel;
    private JTable userTable;

    public UserPanel(AuthService authService) {
        this.authService = authService;
        this.userDao = new UserDao();

        initComponents();
        loadUsersAsync();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.COLOR_OFFWHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.COLOR_OFFWHITE);
        JLabel title = new JLabel("System User & Access Control Management");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_NAVY);
        topPanel.add(title, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Form (Left)
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        formCard.setMinimumSize(new Dimension(340, 360));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;
        JLabel formHeader = new JLabel("Create User Account");
        formHeader.setFont(UITheme.FONT_HEADER);
        formHeader.setForeground(UITheme.COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formCard.add(formHeader, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Username *"), gbc);
        usernameField = UITheme.createTextField();
        gbc.gridx = 1;
        formCard.add(usernameField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Password *"), gbc);
        passwordField = new JPasswordField();
        passwordField.setFont(UITheme.FONT_REGULAR);
        passwordField.setPreferredSize(new Dimension(passwordField.getPreferredSize().width, 36));
        passwordField.setBorder(UITheme.createInputBorder());
        gbc.gridx = 1;
        formCard.add(passwordField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Full Name *"), gbc);
        fullNameField = UITheme.createTextField();
        gbc.gridx = 1;
        formCard.add(fullNameField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Role *"), gbc);
        roleCombo = UITheme.createComboBox();
        roleCombo.addItem(User.ROLE_RECEPTIONIST);
        roleCombo.addItem(User.ROLE_ADMIN);
        gbc.gridx = 1;
        formCard.add(roleCombo, gbc);
        row++;

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        btnPanel.setBackground(UITheme.COLOR_WHITE);

        JButton createBtn = UITheme.createPrimaryButton("Create Account");
        createBtn.addActionListener(e -> createUser());
        btnPanel.add(createBtn);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formCard.add(btnPanel, gbc);

        // Table (Right)
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(8, 8));

        String[] cols = {"User ID", "Username", "Role", "Full Name", "Created Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        UITheme.styleTable(userTable);
        tableCard.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actionPanel.setBackground(UITheme.COLOR_WHITE);

        JButton deleteBtn = UITheme.createDangerButton("Delete Selected Account");
        deleteBtn.addActionListener(e -> deleteSelectedUser());
        actionPanel.add(deleteBtn);

        JButton refreshBtn = UITheme.createOutlineButton("Refresh");
        refreshBtn.addActionListener(e -> loadUsersAsync());
        actionPanel.add(refreshBtn);

        tableCard.add(actionPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formCard, tableCard);
        splitPane.setDividerLocation(360);
        splitPane.setResizeWeight(0.35);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);
    }

    private void createUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String fullName = fullNameField.getText().trim();
        String role = (String) roleCombo.getSelectedItem();

        try {
            SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
                @Override
                protected User doInBackground() {
                    return authService.registerUser(username, password, role, fullName);
                }

                @Override
                protected void done() {
                    try {
                        User user = get();
                        JOptionPane.showMessageDialog(UserPanel.this,
                                "Account created successfully for " + user.getUsername() + " (" + user.getRole() + ")",
                                "Account Created", JOptionPane.INFORMATION_MESSAGE);
                        usernameField.setText("");
                        passwordField.setText("");
                        fullNameField.setText("");
                        loadUsersAsync();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(UserPanel.this, cause.getMessage(), "Error Creating Account", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an account to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);

        if ("admin".equalsIgnoreCase(username)) {
            JOptionPane.showMessageDialog(this, "Cannot delete the default root system administrator account.", "Protected Account", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete account '" + username + "' (ID #" + userId + ")?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return authService.deleteUser(userId);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(UserPanel.this, "Account removed.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadUsersAsync();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(UserPanel.this, "Failed to delete: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    public void loadUsersAsync() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() {
                return authService.getAllUsers();
            }

            @Override
            protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (User u : get()) {
                        tableModel.addRow(new Object[]{
                                u.getId(),
                                u.getUsername(),
                                u.getRole(),
                                u.getFullName(),
                                u.getCreatedAt() != null ? u.getCreatedAt().toString() : "N/A"
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UserPanel.this, "Error loading users: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
