package com.hospital.ui;

import com.hospital.exception.ValidationException;
import com.hospital.model.Patient;
import com.hospital.service.PatientService;
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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Patient Management Panel.
 * Demonstrates:
 * 1. CRUD Operations on Patients.
 * 2. Overloaded search (by ID or partial Name).
 * 3. Prevention of duplicate phone numbers and accidental deletions.
 */
public class PatientPanel extends JPanel {

    private final PatientService patientService;

    private JTextField idField;
    private JTextField nameField;
    private JTextField ageField;
    private JComboBox<String> genderCombo;
    private JTextField phoneField;
    private JTextField addressField;
    private JTextArea historyArea;

    private JTextField searchField;
    private DefaultTableModel tableModel;
    private JTable patientTable;

    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;

    public PatientPanel(PatientService patientService) {
        this.patientService = patientService;
        initComponents();
        loadPatientsAsync();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.COLOR_OFFWHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.COLOR_OFFWHITE);
        JLabel title = new JLabel("Patient Registration & Directory");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_NAVY);
        topPanel.add(title, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Split Pane: Left Form (35%), Right Table (65%)
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        formCard.setMinimumSize(new Dimension(340, 400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;
        JLabel formHeader = new JLabel("Patient Details Form");
        formHeader.setFont(UITheme.FONT_HEADER);
        formHeader.setForeground(UITheme.COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formCard.add(formHeader, gbc);

        // Patient ID (Read-only)
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Patient ID"), gbc);
        idField = UITheme.createTextField();
        idField.setEditable(false);
        idField.setBackground(new java.awt.Color(0xF0, 0xF0, 0xF0));
        gbc.gridx = 1;
        formCard.add(idField, gbc);
        row++;

        // Name
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Full Name *"), gbc);
        nameField = UITheme.createTextField();
        gbc.gridx = 1;
        formCard.add(nameField, gbc);
        row++;

        // Age
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Age *"), gbc);
        ageField = UITheme.createTextField();
        gbc.gridx = 1;
        formCard.add(ageField, gbc);
        row++;

        // Gender
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Gender *"), gbc);
        genderCombo = UITheme.createComboBox();
        genderCombo.addItem("Male");
        genderCombo.addItem("Female");
        genderCombo.addItem("Other");
        gbc.gridx = 1;
        formCard.add(genderCombo, gbc);
        row++;

        // Phone
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Phone (10 digits) *"), gbc);
        phoneField = UITheme.createTextField();
        gbc.gridx = 1;
        formCard.add(phoneField, gbc);
        row++;

        // Address
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Address *"), gbc);
        addressField = UITheme.createTextField();
        gbc.gridx = 1;
        formCard.add(addressField, gbc);
        row++;

        // Medical History
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Medical History"), gbc);
        historyArea = new JTextArea(3, 18);
        historyArea.setFont(UITheme.FONT_REGULAR);
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        historyArea.setBorder(UITheme.createInputBorder());
        JScrollPane histScroll = new JScrollPane(historyArea);
        histScroll.setPreferredSize(new Dimension(180, 70));
        gbc.gridx = 1;
        formCard.add(histScroll, gbc);
        row++;

        // Action Buttons Grid
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        btnPanel.setBackground(UITheme.COLOR_WHITE);

        addButton = UITheme.createPrimaryButton("Save Patient");
        addButton.addActionListener(e -> addPatient());

        updateButton = UITheme.createSecondaryButton("Update");
        updateButton.addActionListener(e -> updatePatient());

        deleteButton = UITheme.createDangerButton("Delete");
        deleteButton.addActionListener(e -> deletePatient());

        clearButton = UITheme.createOutlineButton("Clear");
        clearButton.addActionListener(e -> clearForm());

        btnPanel.add(addButton);
        btnPanel.add(updateButton);
        btnPanel.add(deleteButton);
        btnPanel.add(clearButton);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formCard.add(btnPanel, gbc);

        // Right Table Panel
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(8, 8));

        // Search Bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchBar.setBackground(UITheme.COLOR_WHITE);
        searchBar.add(new JLabel("Search (ID or Name):"));
        searchField = UITheme.createTextField();
        searchField.setPreferredSize(new Dimension(200, 32));
        JButton searchBtn = UITheme.createPrimaryButton("Search");
        searchBtn.setPreferredSize(new Dimension(90, 32));
        searchBtn.addActionListener(e -> performSearch());

        JButton resetBtn = UITheme.createOutlineButton("Reset");
        resetBtn.setPreferredSize(new Dimension(80, 32));
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            loadPatientsAsync();
        });

        searchBar.add(searchField);
        searchBar.add(searchBtn);
        searchBar.add(resetBtn);
        tableCard.add(searchBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Name", "Age", "Gender", "Phone", "Address", "Registered Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        patientTable = new JTable(tableModel);
        UITheme.styleTable(patientTable);

        // Selection listener to populate form
        patientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && patientTable.getSelectedRow() != -1) {
                populateFormFromSelectedRow();
            }
        });

        tableCard.add(new JScrollPane(patientTable), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formCard, tableCard);
        splitPane.setDividerLocation(380);
        splitPane.setResizeWeight(0.35);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);
    }

    public void loadPatientsAsync() {
        SwingWorker<List<Patient>, Void> worker = new SwingWorker<List<Patient>, Void>() {
            @Override
            protected List<Patient> doInBackground() {
                return patientService.getAllPatients();
            }

            @Override
            protected void done() {
                try {
                    List<Patient> list = get();
                    populateTable(list);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PatientPanel.this,
                            "Error loading patient list: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<Patient> list) {
        tableModel.setRowCount(0);
        for (Patient p : list) {
            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getAge(),
                    p.getGender(),
                    p.getPhone(),
                    p.getAddress(),
                    p.getRegisteredOn()
            });
        }
    }

    private void populateFormFromSelectedRow() {
        int row = patientTable.getSelectedRow();
        if (row >= 0) {
            int patientId = (int) tableModel.getValueAt(row, 0);
            SwingWorker<Patient, Void> worker = new SwingWorker<Patient, Void>() {
                @Override
                protected Patient doInBackground() {
                    return patientService.getPatientById(patientId).orElse(null);
                }

                @Override
                protected void done() {
                    try {
                        Patient p = get();
                        if (p != null) {
                            idField.setText(String.valueOf(p.getId()));
                            nameField.setText(p.getName());
                            ageField.setText(String.valueOf(p.getAge()));
                            genderCombo.setSelectedItem(p.getGender());
                            phoneField.setText(p.getPhone());
                            addressField.setText(p.getAddress());
                            historyArea.setText(p.getMedicalHistory());
                        }
                    } catch (Exception ignored) {
                    }
                }
            };
            worker.execute();
        }
    }

    private void addPatient() {
        try {
            String name = nameField.getText().trim();
            int age = Integer.parseInt(ageField.getText().trim());
            String gender = (String) genderCombo.getSelectedItem();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();
            String history = historyArea.getText().trim();

            Patient p = new Patient(name, age, gender, phone, address, history);

            SwingWorker<Patient, Void> worker = new SwingWorker<Patient, Void>() {
                @Override
                protected Patient doInBackground() {
                    return patientService.registerPatient(p);
                }

                @Override
                protected void done() {
                    try {
                        Patient saved = get();
                        JOptionPane.showMessageDialog(PatientPanel.this,
                                "Patient registered successfully!\nGenerated Patient ID: " + saved.getId(),
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadPatientsAsync();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(PatientPanel.this,
                                cause.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric age.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updatePatient() {
        String idStr = idField.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            String name = nameField.getText().trim();
            int age = Integer.parseInt(ageField.getText().trim());
            String gender = (String) genderCombo.getSelectedItem();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();
            String history = historyArea.getText().trim();

            Patient p = new Patient(id, name, age, gender, phone, address, history, null);

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return patientService.updatePatient(p);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(PatientPanel.this, "Patient details updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadPatientsAsync();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(PatientPanel.this, cause.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric fields.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deletePatient() {
        String idStr = idField.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a patient to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(idStr);
        String name = nameField.getText();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete patient #" + id + " (" + name + ")?\n"
                + "This action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return patientService.deletePatient(id);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(PatientPanel.this, "Patient record removed.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadPatientsAsync();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(PatientPanel.this, cause.getMessage(), "Cannot Delete", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadPatientsAsync();
            return;
        }

        SwingWorker<List<Patient>, Void> worker = new SwingWorker<List<Patient>, Void>() {
            @Override
            protected List<Patient> doInBackground() {
                // Check if query is an integer ID (calls overloaded search(int id))
                try {
                    int id = Integer.parseInt(query);
                    return patientService.search(id);
                } catch (NumberFormatException e) {
                    // Calls overloaded search(String name)
                    return patientService.search(query);
                }
            }

            @Override
            protected void done() {
                try {
                    List<Patient> results = get();
                    populateTable(results);
                    if (results.isEmpty()) {
                        JOptionPane.showMessageDialog(PatientPanel.this, "No patients found matching '" + query + "'.", "Search Results", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PatientPanel.this, "Search failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        ageField.setText("");
        genderCombo.setSelectedIndex(0);
        phoneField.setText("");
        addressField.setText("");
        historyArea.setText("");
        patientTable.clearSelection();
    }
}
