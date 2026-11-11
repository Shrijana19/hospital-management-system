package com.hospital.ui;

import com.hospital.exception.ValidationException;
import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Appointment Booking & Scheduling Panel.
 * Features:
 * 1. Dynamic free-slot discovery (queries doctor availability and booked slots).
 * 2. Database and Service-level clash prevention.
 * 3. Status state machine: Scheduled -> Completed / Cancelled.
 */
public class AppointmentPanel extends JPanel {

    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final DoctorService doctorService;

    // Form Components
    private JComboBox<PatientItem> patientCombo;
    private JComboBox<DoctorItem> doctorCombo;
    private JTextField dateField;
    private JComboBox<String> timeSlotCombo;

    // Table & Filter Components
    private JComboBox<String> filterStatusCombo;
    private JTextField filterDateField;
    private DefaultTableModel tableModel;
    private JTable appointmentTable;

    public AppointmentPanel(AppointmentService appointmentService, PatientService patientService, DoctorService doctorService) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.doctorService = doctorService;

        initComponents();
        loadDropdowns();
        loadAppointmentsAsync();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.COLOR_OFFWHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.COLOR_OFFWHITE);
        JLabel title = new JLabel("Appointment Scheduling & OPD Queue");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_NAVY);
        topPanel.add(title, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Left Form Card (Booking)
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        formCard.setMinimumSize(new Dimension(360, 420));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;
        JLabel formHeader = new JLabel("Book Consultation Slot");
        formHeader.setFont(UITheme.FONT_HEADER);
        formHeader.setForeground(UITheme.COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formCard.add(formHeader, gbc);

        // Patient Selector
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Select Patient *"), gbc);
        patientCombo = UITheme.createComboBox();
        gbc.gridx = 1;
        formCard.add(patientCombo, gbc);
        row++;

        // Doctor Selector
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Select Doctor *"), gbc);
        doctorCombo = UITheme.createComboBox();
        doctorCombo.addActionListener(e -> refreshAvailableSlots());
        gbc.gridx = 1;
        formCard.add(doctorCombo, gbc);
        row++;

        // Date (yyyy-MM-dd)
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Date (YYYY-MM-DD) *"), gbc);
        dateField = UITheme.createTextField();
        dateField.setText(LocalDate.now().plusDays(1).toString()); // Defaults to tomorrow
        dateField.addActionListener(e -> refreshAvailableSlots());
        gbc.gridx = 1;
        formCard.add(dateField, gbc);
        row++;

        // Available Time Slots (Dynamically Calculated)
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Free Time Slot *"), gbc);
        timeSlotCombo = UITheme.createComboBox();
        gbc.gridx = 1;
        formCard.add(timeSlotCombo, gbc);
        row++;

        // Helper label
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel hintLabel = new JLabel("Time slots refresh automatically based on doctor and date.");
        hintLabel.setFont(UITheme.FONT_SMALL);
        hintLabel.setForeground(UITheme.COLOR_TEXT_MUTED);
        formCard.add(hintLabel, gbc);

        // Check Slots Button
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JButton checkSlotsBtn = UITheme.createOutlineButton("Check Available Slots");
        checkSlotsBtn.addActionListener(e -> refreshAvailableSlots());
        formCard.add(checkSlotsBtn, gbc);

        // Book Button
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JButton bookBtn = UITheme.createPrimaryButton("Confirm Appointment Booking");
        bookBtn.setPreferredSize(new Dimension(bookBtn.getPreferredSize().width, 38));
        bookBtn.addActionListener(e -> bookAppointment());
        formCard.add(bookBtn, gbc);

        // Right Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(8, 8));

        // Filter Controls Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterBar.setBackground(UITheme.COLOR_WHITE);

        filterBar.add(new JLabel("Status:"));
        filterStatusCombo = UITheme.createComboBox();
        filterStatusCombo.addItem("All");
        filterStatusCombo.addItem(Appointment.STATUS_SCHEDULED);
        filterStatusCombo.addItem(Appointment.STATUS_COMPLETED);
        filterStatusCombo.addItem(Appointment.STATUS_CANCELLED);
        filterStatusCombo.setPreferredSize(new Dimension(120, 32));
        filterStatusCombo.addActionListener(e -> applyFilter());
        filterBar.add(filterStatusCombo);

        filterBar.add(new JLabel("Date:"));
        filterDateField = UITheme.createTextField();
        filterDateField.setPreferredSize(new Dimension(110, 32));
        filterBar.add(filterDateField);

        JButton filterBtn = UITheme.createPrimaryButton("Filter");
        filterBtn.setPreferredSize(new Dimension(80, 32));
        filterBtn.addActionListener(e -> applyFilter());
        filterBar.add(filterBtn);

        JButton resetFilterBtn = UITheme.createOutlineButton("Reset");
        resetFilterBtn.setPreferredSize(new Dimension(75, 32));
        resetFilterBtn.addActionListener(e -> {
            filterStatusCombo.setSelectedIndex(0);
            filterDateField.setText("");
            loadAppointmentsAsync();
        });
        filterBar.add(resetFilterBtn);

        tableCard.add(filterBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Patient Name", "Doctor", "Specialization", "Date", "Time", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        appointmentTable = new JTable(tableModel);
        UITheme.styleTable(appointmentTable);
        tableCard.add(new JScrollPane(appointmentTable), BorderLayout.CENTER);

        // Table Action Buttons (Bottom of right side)
        JPanel actionsBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actionsBar.setBackground(UITheme.COLOR_WHITE);

        JButton markDoneBtn = UITheme.createSuccessButton("Mark Selected as Completed");
        markDoneBtn.addActionListener(e -> transitionSelectedStatus(Appointment.STATUS_COMPLETED));

        JButton cancelBtn = UITheme.createDangerButton("Cancel Selected Appointment");
        cancelBtn.addActionListener(e -> transitionSelectedStatus(Appointment.STATUS_CANCELLED));

        JButton refreshListBtn = UITheme.createOutlineButton("Refresh List");
        refreshListBtn.addActionListener(e -> loadAppointmentsAsync());

        actionsBar.add(markDoneBtn);
        actionsBar.add(cancelBtn);
        actionsBar.add(refreshListBtn);
        tableCard.add(actionsBar, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formCard, tableCard);
        splitPane.setDividerLocation(380);
        splitPane.setResizeWeight(0.35);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);
    }

    private void loadDropdowns() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private List<Patient> patients;
            private List<Doctor> doctors;

            @Override
            protected Void doInBackground() {
                patients = patientService.getAllPatients();
                doctors = doctorService.getAvailableDoctors();
                return null;
            }

            @Override
            protected void done() {
                patientCombo.removeAllItems();
                for (Patient p : patients) {
                    patientCombo.addItem(new PatientItem(p.getId(), p.getName(), p.getPhone()));
                }

                doctorCombo.removeAllItems();
                for (Doctor d : doctors) {
                    doctorCombo.addItem(new DoctorItem(d.getId(), d.getName(), d.getSpecialization(), d.getConsultationFee().doubleValue()));
                }

                refreshAvailableSlots();
            }
        };
        worker.execute();
    }

    public void refreshAvailableSlots() {
        DoctorItem selectedDoc = (DoctorItem) doctorCombo.getSelectedItem();
        String dateStr = dateField.getText().trim();

        if (selectedDoc == null || dateStr.isEmpty()) {
            return;
        }

        try {
            LocalDate date = LocalDate.parse(dateStr);
            SwingWorker<List<LocalTime>, Void> worker = new SwingWorker<List<LocalTime>, Void>() {
                @Override
                protected List<LocalTime> doInBackground() {
                    return appointmentService.getAvailableTimeSlots(selectedDoc.id, date);
                }

                @Override
                protected void done() {
                    try {
                        List<LocalTime> freeSlots = get();
                        timeSlotCombo.removeAllItems();
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
                        for (LocalTime slot : freeSlots) {
                            timeSlotCombo.addItem(slot.format(dtf));
                        }
                        if (freeSlots.isEmpty()) {
                            timeSlotCombo.addItem("No slots available");
                        }
                    } catch (Exception ignored) {
                    }
                }
            };
            worker.execute();
        } catch (DateTimeParseException ignored) {
            // Wait for user to finish typing valid date
        }
    }

    private void bookAppointment() {
        PatientItem patient = (PatientItem) patientCombo.getSelectedItem();
        DoctorItem doctor = (DoctorItem) doctorCombo.getSelectedItem();
        String dateStr = dateField.getText().trim();
        String slotStr = (String) timeSlotCombo.getSelectedItem();

        if (patient == null || doctor == null) {
            JOptionPane.showMessageDialog(this, "Please select both a patient and a doctor.", "Selection Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (slotStr == null || slotStr.equals("No slots available") || slotStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No available time slot selected. Please choose another date or doctor.", "Slot Unavailable", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate date = LocalDate.parse(dateStr);
            LocalTime time = LocalTime.parse(slotStr);

            SwingWorker<Appointment, Void> worker = new SwingWorker<Appointment, Void>() {
                @Override
                protected Appointment doInBackground() {
                    return appointmentService.bookAppointment(patient.id, doctor.id, date, time);
                }

                @Override
                protected void done() {
                    try {
                        Appointment booked = get();
                        JOptionPane.showMessageDialog(AppointmentPanel.this,
                                "Appointment successfully booked!\nBooking Reference: #" + booked.getId()
                                + "\nDoctor: " + doctor.name + "\nDate & Slot: " + date + " at " + time,
                                "Appointment Confirmed", JOptionPane.INFORMATION_MESSAGE);
                        loadAppointmentsAsync();
                        refreshAvailableSlots();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(AppointmentPanel.this,
                                cause.getMessage(), "Booking Clash / Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        } catch (DateTimeParseException dtpe) {
            JOptionPane.showMessageDialog(this, "Please enter date in valid YYYY-MM-DD format (e.g. 2026-11-20).", "Invalid Date", JOptionPane.WARNING_MESSAGE);
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void transitionSelectedStatus(String targetStatus) {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int apptId = (int) tableModel.getValueAt(row, 0);
        String patientName = (String) tableModel.getValueAt(row, 1);
        String currentStatus = (String) tableModel.getValueAt(row, 6);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Change status of Appointment #" + apptId + " (" + patientName + ") from '"
                + currentStatus + "' to '" + targetStatus + "'?",
                "Confirm Status Change", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return appointmentService.updateStatus(apptId, targetStatus);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(AppointmentPanel.this, "Status updated to " + targetStatus, "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadAppointmentsAsync();
                        refreshAvailableSlots();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(AppointmentPanel.this, cause.getMessage(), "Transition Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    public void loadAppointmentsAsync() {
        SwingWorker<List<Appointment>, Void> worker = new SwingWorker<List<Appointment>, Void>() {
            @Override
            protected List<Appointment> doInBackground() {
                return appointmentService.getAllAppointments();
            }

            @Override
            protected void done() {
                try {
                    populateTable(get());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AppointmentPanel.this, "Error loading appointments: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void applyFilter() {
        String status = (String) filterStatusCombo.getSelectedItem();
        String dateStr = filterDateField.getText().trim();
        LocalDate date = null;

        if (!dateStr.isEmpty()) {
            try {
                date = LocalDate.parse(dateStr);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Filter date must be in YYYY-MM-DD format.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        final LocalDate finalDate = date;
        SwingWorker<List<Appointment>, Void> worker = new SwingWorker<List<Appointment>, Void>() {
            @Override
            protected List<Appointment> doInBackground() {
                return appointmentService.filterAppointments(null, null, finalDate, status);
            }

            @Override
            protected void done() {
                try {
                    populateTable(get());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AppointmentPanel.this, "Filter query error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<Appointment> list) {
        tableModel.setRowCount(0);
        for (Appointment a : list) {
            tableModel.addRow(new Object[]{
                    a.getId(),
                    a.getPatientName(),
                    a.getDoctorName(),
                    a.getDoctorSpecialization(),
                    a.getAppointmentDate(),
                    a.getAppointmentTime(),
                    a.getStatus()
            });
        }
    }

    // Helper item classes for JComboBox
    public static class PatientItem {
        public final int id;
        public final String name;
        public final String phone;

        public PatientItem(int id, String name, String phone) {
            this.id = id;
            this.name = name;
            this.phone = phone;
        }

        @Override
        public String toString() {
            return "#" + id + " - " + name + " (" + phone + ")";
        }
    }

    public static class DoctorItem {
        public final int id;
        public final String name;
        public final String spec;
        public final double fee;

        public DoctorItem(int id, String name, String spec, double fee) {
            this.id = id;
            this.name = name;
            this.spec = spec;
            this.fee = fee;
        }

        @Override
        public String toString() {
            return "Dr. " + name + " (" + spec + ") - $" + String.format("%.2f", fee);
        }
    }
}
