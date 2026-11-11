package com.hospital.ui;

import com.hospital.exception.ValidationException;
import com.hospital.model.Doctor;
import com.hospital.model.MedicalRecord;
import com.hospital.model.Patient;
import com.hospital.service.DoctorService;
import com.hospital.service.MedicalRecordService;
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
import javax.swing.JDialog;
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
 * Medical Records & Clinical Documentation Panel.
 * Features:
 * 1. Recording diagnoses, prescriptions, treatment regimens, and physician notes.
 * 2. Full chronological medical history retrieval for any patient.
 * 3. Linkage to attending physician and consultation encounter.
 */
public class MedicalRecordPanel extends JPanel {

    private final MedicalRecordService medicalRecordService;
    private final PatientService patientService;
    private final DoctorService doctorService;

    // Form Components
    private JComboBox<PatientComboItem> patientCombo;
    private JComboBox<DoctorComboItem> doctorCombo;
    private JTextField apptIdField;
    private JTextArea diagnosisArea;
    private JTextArea medicinesArea;
    private JTextArea treatmentArea;
    private JTextArea notesArea;

    // Table Components
    private JComboBox<PatientComboItem> filterPatientCombo;
    private DefaultTableModel tableModel;
    private JTable recordTable;

    public MedicalRecordPanel(MedicalRecordService medicalRecordService,
                              PatientService patientService, DoctorService doctorService) {
        this.medicalRecordService = medicalRecordService;
        this.patientService = patientService;
        this.doctorService = doctorService;

        initComponents();
        loadDropdowns();
        loadRecordsAsync();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.COLOR_OFFWHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.COLOR_OFFWHITE);
        JLabel title = new JLabel("Electronic Medical Records (EMR) & Clinical History");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_NAVY);
        topPanel.add(title, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Left Form Card
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        formCard.setMinimumSize(new Dimension(380, 520));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;
        JLabel formHeader = new JLabel("Clinical Encounter Entry");
        formHeader.setFont(UITheme.FONT_HEADER);
        formHeader.setForeground(UITheme.COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formCard.add(formHeader, gbc);

        // Patient Selector
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Patient *"), gbc);
        patientCombo = UITheme.createComboBox();
        gbc.gridx = 1;
        formCard.add(patientCombo, gbc);
        row++;

        // Attending Doctor
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Doctor *"), gbc);
        doctorCombo = UITheme.createComboBox();
        gbc.gridx = 1;
        formCard.add(doctorCombo, gbc);
        row++;

        // Appointment ID (Optional)
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Appt Ref ID"), gbc);
        apptIdField = UITheme.createTextField();
        apptIdField.setToolTipText("Optional: ID of associated appointment");
        gbc.gridx = 1;
        formCard.add(apptIdField, gbc);
        row++;

        // Diagnosis
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Diagnosis *"), gbc);
        diagnosisArea = createTextArea(2);
        gbc.gridx = 1;
        formCard.add(new JScrollPane(diagnosisArea), gbc);
        row++;

        // Prescribed Medicines
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Prescriptions"), gbc);
        medicinesArea = createTextArea(2);
        gbc.gridx = 1;
        formCard.add(new JScrollPane(medicinesArea), gbc);
        row++;

        // Treatment Details
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Treatment Plan"), gbc);
        treatmentArea = createTextArea(2);
        gbc.gridx = 1;
        formCard.add(new JScrollPane(treatmentArea), gbc);
        row++;

        // Doctor Notes
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Doctor Notes"), gbc);
        notesArea = createTextArea(2);
        gbc.gridx = 1;
        formCard.add(new JScrollPane(notesArea), gbc);
        row++;

        // Actions
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        btnPanel.setBackground(UITheme.COLOR_WHITE);

        JButton saveBtn = UITheme.createPrimaryButton("Save Medical Record");
        saveBtn.addActionListener(e -> saveRecord());

        JButton clearBtn = UITheme.createOutlineButton("Clear");
        clearBtn.addActionListener(e -> clearForm());

        btnPanel.add(saveBtn);
        btnPanel.add(clearBtn);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formCard.add(btnPanel, gbc);

        // Right Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(8, 8));

        // Filter Bar (by Patient)
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterBar.setBackground(UITheme.COLOR_WHITE);
        filterBar.add(new JLabel("Filter by Patient:"));
        filterPatientCombo = UITheme.createComboBox();
        filterPatientCombo.setPreferredSize(new Dimension(200, 32));
        filterPatientCombo.addActionListener(e -> applyPatientFilter());
        filterBar.add(filterPatientCombo);

        JButton viewAllBtn = UITheme.createOutlineButton("View All");
        viewAllBtn.setPreferredSize(new Dimension(85, 32));
        viewAllBtn.addActionListener(e -> loadRecordsAsync());
        filterBar.add(viewAllBtn);
        tableCard.add(filterBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Record ID", "Patient", "Attending Doctor", "Diagnosis", "Date Recorded"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        recordTable = new JTable(tableModel);
        UITheme.styleTable(recordTable);
        tableCard.add(new JScrollPane(recordTable), BorderLayout.CENTER);

        // Action Bar (Bottom)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actionPanel.setBackground(UITheme.COLOR_WHITE);

        JButton historyBtn = UITheme.createSecondaryButton("View Full Patient Clinical History");
        historyBtn.addActionListener(e -> showPatientHistoryDialog());
        actionPanel.add(historyBtn);
        tableCard.add(actionPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formCard, tableCard);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.38);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);
    }

    private JTextArea createTextArea(int rows) {
        JTextArea ta = new JTextArea(rows, 18);
        ta.setFont(UITheme.FONT_REGULAR);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(UITheme.createInputBorder());
        return ta;
    }

    private void loadDropdowns() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private List<Patient> patients;
            private List<Doctor> doctors;

            @Override
            protected Void doInBackground() {
                patients = patientService.getAllPatients();
                doctors = doctorService.getAllDoctors();
                return null;
            }

            @Override
            protected void done() {
                patientCombo.removeAllItems();
                filterPatientCombo.removeAllItems();
                filterPatientCombo.addItem(new PatientComboItem(0, "All Patients"));

                for (Patient p : patients) {
                    PatientComboItem item = new PatientComboItem(p.getId(), p.getName());
                    patientCombo.addItem(item);
                    filterPatientCombo.addItem(item);
                }

                doctorCombo.removeAllItems();
                for (Doctor d : doctors) {
                    doctorCombo.addItem(new DoctorComboItem(d.getId(), d.getName(), d.getSpecialization()));
                }
            }
        };
        worker.execute();
    }

    private void saveRecord() {
        PatientComboItem patient = (PatientComboItem) patientCombo.getSelectedItem();
        DoctorComboItem doctor = (DoctorComboItem) doctorCombo.getSelectedItem();

        if (patient == null || doctor == null) {
            JOptionPane.showMessageDialog(this, "Please select both a patient and a doctor.", "Selection Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String diagnosis = diagnosisArea.getText().trim();
        String medicines = medicinesArea.getText().trim();
        String treatment = treatmentArea.getText().trim();
        String notes = notesArea.getText().trim();
        String apptStr = apptIdField.getText().trim();

        Integer apptId = null;
        if (!apptStr.isEmpty()) {
            try {
                apptId = Integer.parseInt(apptStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Appointment Ref ID must be a valid number.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        try {
            MedicalRecord record = new MedicalRecord(patient.id, doctor.id, apptId, diagnosis, medicines, treatment, notes);

            SwingWorker<MedicalRecord, Void> worker = new SwingWorker<MedicalRecord, Void>() {
                @Override
                protected MedicalRecord doInBackground() {
                    return medicalRecordService.addRecord(record);
                }

                @Override
                protected void done() {
                    try {
                        MedicalRecord saved = get();
                        JOptionPane.showMessageDialog(MedicalRecordPanel.this,
                                "Medical record saved successfully!\nRecord ID: #" + saved.getId(),
                                "Record Saved", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadRecordsAsync();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(MedicalRecordPanel.this, cause.getMessage(), "Error Saving Record", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void loadRecordsAsync() {
        SwingWorker<List<MedicalRecord>, Void> worker = new SwingWorker<List<MedicalRecord>, Void>() {
            @Override
            protected List<MedicalRecord> doInBackground() {
                return medicalRecordService.getAllRecords();
            }

            @Override
            protected void done() {
                try {
                    populateTable(get());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MedicalRecordPanel.this, "Error loading records: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void applyPatientFilter() {
        PatientComboItem item = (PatientComboItem) filterPatientCombo.getSelectedItem();
        if (item == null || item.id == 0) {
            loadRecordsAsync();
            return;
        }

        SwingWorker<List<MedicalRecord>, Void> worker = new SwingWorker<List<MedicalRecord>, Void>() {
            @Override
            protected List<MedicalRecord> doInBackground() {
                return medicalRecordService.getRecordsByPatient(item.id);
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
    }

    private void showPatientHistoryDialog() {
        PatientComboItem item = (PatientComboItem) filterPatientCombo.getSelectedItem();
        int patientId = (item != null && item.id > 0) ? item.id : 0;

        if (patientId == 0) {
            int row = recordTable.getSelectedRow();
            if (row >= 0) {
                int recId = (int) tableModel.getValueAt(row, 0);
                // find record
                for (MedicalRecord mr : medicalRecordService.getAllRecords()) {
                    if (mr.getId() == recId) {
                        patientId = mr.getPatientId();
                        break;
                    }
                }
            }
        }

        if (patientId == 0) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the filter dropdown or select a record from the table.", "Patient Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        final int targetId = patientId;
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return medicalRecordService.generatePatientHistorySummary(targetId);
            }

            @Override
            protected void done() {
                try {
                    String summary = get();
                    JDialog dialog = new JDialog((java.awt.Frame) null, "Clinical History - Patient #" + targetId, true);
                    dialog.setSize(600, 500);
                    dialog.setLocationRelativeTo(MedicalRecordPanel.this);
                    dialog.setLayout(new BorderLayout(10, 10));

                    JTextArea area = new JTextArea(summary);
                    area.setFont(UITheme.FONT_MONO);
                    area.setEditable(false);
                    area.setBackground(UITheme.COLOR_WHITE);
                    area.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
                    dialog.add(new JScrollPane(area), BorderLayout.CENTER);

                    JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
                    JButton closeBtn = UITheme.createPrimaryButton("Close");
                    closeBtn.addActionListener(e -> dialog.dispose());
                    btnBar.add(closeBtn);
                    dialog.add(btnBar, BorderLayout.SOUTH);

                    dialog.setVisible(true);
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<MedicalRecord> list) {
        tableModel.setRowCount(0);
        for (MedicalRecord r : list) {
            tableModel.addRow(new Object[]{
                    r.getId(),
                    r.getPatientName() != null ? r.getPatientName() : "Patient #" + r.getPatientId(),
                    r.getDoctorName() != null ? "Dr. " + r.getDoctorName() : "Doctor #" + r.getDoctorId(),
                    r.getDiagnosis(),
                    r.getFormattedDate()
            });
        }
    }

    private void clearForm() {
        apptIdField.setText("");
        diagnosisArea.setText("");
        medicinesArea.setText("");
        treatmentArea.setText("");
        notesArea.setText("");
    }

    private static class PatientComboItem {
        final int id;
        final String name;

        PatientComboItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return id > 0 ? "#" + id + " - " + name : name;
        }
    }

    private static class DoctorComboItem {
        final int id;
        final String name;
        final String spec;

        DoctorComboItem(int id, String name, String spec) {
            this.id = id;
            this.name = name;
            this.spec = spec;
        }

        @Override
        public String toString() {
            return "Dr. " + name + " (" + spec + ")";
        }
    }
}
