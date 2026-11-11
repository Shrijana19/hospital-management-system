package com.hospital.ui;

import com.hospital.exception.ValidationException;
import com.hospital.model.Doctor;
import com.hospital.service.DoctorService;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Doctor Management Panel.
 * Demonstrates:
 * 1. CRUD operations for Doctors.
 * 2. Overloaded search (by ID, Name, or Specialization).
 * 3. Availability status toggle and consultation fee enforcement.
 */
public class DoctorPanel extends JPanel {

    private final DoctorService doctorService;

    private JTextField idField;
    private JTextField nameField;
    private JTextField ageField;
    private JComboBox<String> genderCombo;
    private JTextField phoneField;
    private JTextField addressField;
    private JComboBox<String> specCombo;
    private JTextField feeField;
    private JCheckBox availableCheck;

    private JTextField searchField;
    private JCheckBox availableFilterCheck;
    private DefaultTableModel tableModel;
    private JTable doctorTable;

    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;

    public DoctorPanel(DoctorService doctorService) {
        this.doctorService = doctorService;
        initComponents();
        loadDoctorsAsync();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.COLOR_OFFWHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.COLOR_OFFWHITE);
        JLabel title = new JLabel("Doctor Management & Specialist Directory");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_NAVY);
        topPanel.add(title, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Form Card (Left)
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        formCard.setMinimumSize(new Dimension(350, 420));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;
        JLabel formHeader = new JLabel("Doctor Profile Form");
        formHeader.setFont(UITheme.FONT_HEADER);
        formHeader.setForeground(UITheme.COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formCard.add(formHeader, gbc);

        // Doctor ID (Read-only)
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Doctor ID"), gbc);
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
        formCard.add(UITheme.createFieldLabel("Age (21-100) *"), gbc);
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

        // Specialization
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Specialization *"), gbc);
        specCombo = UITheme.createComboBox();
        specCombo.setEditable(true);
        specCombo.addItem("Cardiology");
        specCombo.addItem("Dermatology");
        specCombo.addItem("Orthopedics");
        specCombo.addItem("Pediatrics");
        specCombo.addItem("General Medicine");
        specCombo.addItem("Neurology");
        specCombo.addItem("Gynecology");
        specCombo.addItem("Ophthalmology");
        specCombo.addItem("ENT / Otolaryngology");
        gbc.gridx = 1;
        formCard.add(specCombo, gbc);
        row++;

        // Consultation Fee
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Fee ($) *"), gbc);
        feeField = UITheme.createTextField();
        gbc.gridx = 1;
        formCard.add(feeField, gbc);
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
        formCard.add(UITheme.createFieldLabel("Clinic Address *"), gbc);
        addressField = UITheme.createTextField();
        gbc.gridx = 1;
        formCard.add(addressField, gbc);
        row++;

        // Availability Checkbox
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Active in OPD"), gbc);
        availableCheck = new JCheckBox("Available for consultations");
        availableCheck.setSelected(true);
        availableCheck.setFont(UITheme.FONT_REGULAR);
        availableCheck.setBackground(UITheme.COLOR_WHITE);
        gbc.gridx = 1;
        formCard.add(availableCheck, gbc);
        row++;

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        btnPanel.setBackground(UITheme.COLOR_WHITE);

        addButton = UITheme.createPrimaryButton("Save Doctor");
        addButton.addActionListener(e -> addDoctor());

        updateButton = UITheme.createSecondaryButton("Update");
        updateButton.addActionListener(e -> updateDoctor());

        deleteButton = UITheme.createDangerButton("Delete");
        deleteButton.addActionListener(e -> deleteDoctor());

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

        // Search and Filter Bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchBar.setBackground(UITheme.COLOR_WHITE);
        searchBar.add(new JLabel("Search (Name/Specialty/ID):"));
        searchField = UITheme.createTextField();
        searchField.setPreferredSize(new Dimension(180, 32));
        JButton searchBtn = UITheme.createPrimaryButton("Search");
        searchBtn.setPreferredSize(new Dimension(90, 32));
        searchBtn.addActionListener(e -> performSearch());

        availableFilterCheck = new JCheckBox("Available Only");
        availableFilterCheck.setFont(UITheme.FONT_REGULAR);
        availableFilterCheck.setBackground(UITheme.COLOR_WHITE);
        availableFilterCheck.addActionListener(e -> filterAvailableOnly());

        JButton resetBtn = UITheme.createOutlineButton("Reset");
        resetBtn.setPreferredSize(new Dimension(80, 32));
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            availableFilterCheck.setSelected(false);
            loadDoctorsAsync();
        });

        searchBar.add(searchField);
        searchBar.add(searchBtn);
        searchBar.add(availableFilterCheck);
        searchBar.add(resetBtn);
        tableCard.add(searchBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Name", "Specialization", "Fee ($)", "Available", "Phone", "Age"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        doctorTable = new JTable(tableModel);
        UITheme.styleTable(doctorTable);

        doctorTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && doctorTable.getSelectedRow() != -1) {
                populateFormFromSelectedRow();
            }
        });

        tableCard.add(new JScrollPane(doctorTable), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formCard, tableCard);
        splitPane.setDividerLocation(380);
        splitPane.setResizeWeight(0.35);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);
    }

    public void loadDoctorsAsync() {
        SwingWorker<List<Doctor>, Void> worker = new SwingWorker<List<Doctor>, Void>() {
            @Override
            protected List<Doctor> doInBackground() {
                return doctorService.getAllDoctors();
            }

            @Override
            protected void done() {
                try {
                    List<Doctor> list = get();
                    populateTable(list);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DoctorPanel.this,
                            "Error loading doctors: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void filterAvailableOnly() {
        if (availableFilterCheck.isSelected()) {
            SwingWorker<List<Doctor>, Void> worker = new SwingWorker<List<Doctor>, Void>() {
                @Override
                protected List<Doctor> doInBackground() {
                    return doctorService.getAvailableDoctors();
                }

                @Override
                protected void done() {
                    try {
                        populateTable(get());
                    } catch (Exception ignored) {
                    }
                }
            };
            worker.execute();
        } else {
            loadDoctorsAsync();
        }
    }

    private void populateTable(List<Doctor> list) {
        tableModel.setRowCount(0);
        for (Doctor d : list) {
            tableModel.addRow(new Object[]{
                    d.getId(),
                    d.getName(),
                    d.getSpecialization(),
                    String.format("%.2f", d.getConsultationFee().doubleValue()),
                    d.isAvailable() ? "Yes (Active)" : "No (On Leave)",
                    d.getPhone(),
                    d.getAge()
            });
        }
    }

    private void populateFormFromSelectedRow() {
        int row = doctorTable.getSelectedRow();
        if (row >= 0) {
            int doctorId = (int) tableModel.getValueAt(row, 0);
            SwingWorker<Doctor, Void> worker = new SwingWorker<Doctor, Void>() {
                @Override
                protected Doctor doInBackground() {
                    return doctorService.getDoctorById(doctorId).orElse(null);
                }

                @Override
                protected void done() {
                    try {
                        Doctor d = get();
                        if (d != null) {
                            idField.setText(String.valueOf(d.getId()));
                            nameField.setText(d.getName());
                            ageField.setText(String.valueOf(d.getAge()));
                            genderCombo.setSelectedItem(d.getGender());
                            specCombo.setSelectedItem(d.getSpecialization());
                            feeField.setText(d.getConsultationFee().toString());
                            phoneField.setText(d.getPhone());
                            addressField.setText(d.getAddress());
                            availableCheck.setSelected(d.isAvailable());
                        }
                    } catch (Exception ignored) {
                    }
                }
            };
            worker.execute();
        }
    }

    private void addDoctor() {
        try {
            String name = nameField.getText().trim();
            int age = Integer.parseInt(ageField.getText().trim());
            String gender = (String) genderCombo.getSelectedItem();
            String spec = (String) specCombo.getSelectedItem();
            BigDecimal fee = new BigDecimal(feeField.getText().trim());
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();
            boolean avail = availableCheck.isSelected();

            Doctor d = new Doctor(0, name, age, gender, phone, address, spec, fee, avail);

            SwingWorker<Doctor, Void> worker = new SwingWorker<Doctor, Void>() {
                @Override
                protected Doctor doInBackground() {
                    return doctorService.registerDoctor(d);
                }

                @Override
                protected void done() {
                    try {
                        Doctor saved = get();
                        JOptionPane.showMessageDialog(DoctorPanel.this,
                                "Doctor registered successfully!\nGenerated Doctor ID: " + saved.getId(),
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadDoctorsAsync();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(DoctorPanel.this, cause.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for age and fee.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateDoctor() {
        String idStr = idField.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            String name = nameField.getText().trim();
            int age = Integer.parseInt(ageField.getText().trim());
            String gender = (String) genderCombo.getSelectedItem();
            String spec = (String) specCombo.getSelectedItem();
            BigDecimal fee = new BigDecimal(feeField.getText().trim());
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();
            boolean avail = availableCheck.isSelected();

            Doctor d = new Doctor(id, name, age, gender, phone, address, spec, fee, avail);

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return doctorService.updateDoctor(d);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(DoctorPanel.this, "Doctor details updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadDoctorsAsync();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(DoctorPanel.this, cause.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
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

    private void deleteDoctor() {
        String idStr = idField.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(idStr);
        String name = nameField.getText();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Dr. " + name + " (ID #" + id + ")?\n"
                + "This action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return doctorService.deleteDoctor(id);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(DoctorPanel.this, "Doctor record removed.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadDoctorsAsync();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(DoctorPanel.this, cause.getMessage(), "Cannot Delete", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadDoctorsAsync();
            return;
        }

        SwingWorker<List<Doctor>, Void> worker = new SwingWorker<List<Doctor>, Void>() {
            @Override
            protected List<Doctor> doInBackground() {
                try {
                    int id = Integer.parseInt(query);
                    return doctorService.search(id);
                } catch (NumberFormatException e) {
                    return doctorService.search(query);
                }
            }

            @Override
            protected void done() {
                try {
                    List<Doctor> results = get();
                    populateTable(results);
                    if (results.isEmpty()) {
                        JOptionPane.showMessageDialog(DoctorPanel.this, "No doctors found matching '" + query + "'.", "Search Results", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DoctorPanel.this, "Search failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        specCombo.setSelectedIndex(0);
        feeField.setText("");
        phoneField.setText("");
        addressField.setText("");
        availableCheck.setSelected(true);
        doctorTable.clearSelection();
    }
}
