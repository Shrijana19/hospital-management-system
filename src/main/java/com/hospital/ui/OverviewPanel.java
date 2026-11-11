package com.hospital.ui;

import com.hospital.model.Appointment;
import com.hospital.service.AppointmentService;
import com.hospital.service.BillingService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Hospital Dashboard Overview Panel.
 * Displays key performance indicators (KPIs) loaded asynchronously from the MySQL database:
 * - Registered Patients
 * - Active Doctors
 * - Today's Appointments
 * - Pending Bills
 * - Total Revenue Collected
 */
public class OverviewPanel extends JPanel {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final BillingService billingService;

    private JLabel patientsKpiLabel;
    private JLabel doctorsKpiLabel;
    private JLabel appointmentsKpiLabel;
    private JLabel pendingBillsKpiLabel;
    private JLabel revenueKpiLabel;

    private DefaultTableModel recentApptModel;
    private JTable recentApptTable;

    public OverviewPanel(PatientService patientService, DoctorService doctorService,
                         AppointmentService appointmentService, BillingService billingService) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.billingService = billingService;

        initComponents();
        refreshData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.COLOR_OFFWHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.COLOR_OFFWHITE);

        JLabel titleLabel = new JLabel("Hospital Dashboard Overview");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.COLOR_NAVY);

        JButton refreshBtn = UITheme.createOutlineButton(" Refresh Data");
        refreshBtn.addActionListener(e -> refreshData());

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Content: Cards on top, Recent Appointments below
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(UITheme.COLOR_OFFWHITE);

        // 5 KPI Cards Grid
        JPanel kpiGrid = new JPanel(new GridLayout(1, 5, 14, 14));
        kpiGrid.setBackground(UITheme.COLOR_OFFWHITE);
        kpiGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        patientsKpiLabel = new JLabel("...", SwingConstants.CENTER);
        doctorsKpiLabel = new JLabel("...", SwingConstants.CENTER);
        appointmentsKpiLabel = new JLabel("...", SwingConstants.CENTER);
        pendingBillsKpiLabel = new JLabel("...", SwingConstants.CENTER);
        revenueKpiLabel = new JLabel("...", SwingConstants.CENTER);

        kpiGrid.add(createKpiCard("TOTAL PATIENTS", patientsKpiLabel, UITheme.COLOR_TEAL));
        kpiGrid.add(createKpiCard("ACTIVE DOCTORS", doctorsKpiLabel, UITheme.COLOR_NAVY));
        kpiGrid.add(createKpiCard("TODAY'S APPOINTMENTS", appointmentsKpiLabel, new Color(0x3B, 0x82, 0xF6)));
        kpiGrid.add(createKpiCard("PENDING BILLS", pendingBillsKpiLabel, UITheme.COLOR_WARNING));
        kpiGrid.add(createKpiCard("TOTAL REVENUE", revenueKpiLabel, UITheme.COLOR_SUCCESS));

        centerPanel.add(kpiGrid);
        centerPanel.add(Box.createVerticalStrut(20));

        // Recent Appointments Section
        JPanel recentPanel = UITheme.createCardPanel();
        recentPanel.setLayout(new BorderLayout(8, 8));

        JLabel recentHeader = new JLabel("Recent Appointments & Consultations");
        recentHeader.setFont(UITheme.FONT_HEADER);
        recentHeader.setForeground(UITheme.COLOR_NAVY);
        recentPanel.add(recentHeader, BorderLayout.NORTH);

        String[] cols = {"Appt ID", "Patient Name", "Doctor", "Specialization", "Date", "Time", "Status"};
        recentApptModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        recentApptTable = new JTable(recentApptModel);
        UITheme.styleTable(recentApptTable);
        recentPanel.add(new JScrollPane(recentApptTable), BorderLayout.CENTER);

        centerPanel.add(recentPanel);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(4, 6));
        card.setBackground(UITheme.COLOR_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1),
                        BorderFactory.createEmptyBorder(12, 14, 12, 14)
                )
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        valueLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
        valueLabel.setForeground(UITheme.COLOR_TEXT);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void refreshData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private int patientCount;
            private int doctorCount;
            private int todayAppts;
            private int pendingBills;
            private BigDecimal revenue;
            private List<Appointment> recentList;

            @Override
            protected Void doInBackground() {
                patientCount = patientService.getTotalPatientCount();
                doctorCount = doctorService.getTotalDoctorCount();
                todayAppts = appointmentService.getTodayAppointmentCount();
                pendingBills = billingService.getPendingBillsCount();
                revenue = billingService.getTotalRevenue();
                recentList = appointmentService.getAllAppointments();
                return null;
            }

            @Override
            protected void done() {
                patientsKpiLabel.setText(String.valueOf(patientCount));
                doctorsKpiLabel.setText(String.valueOf(doctorCount));
                appointmentsKpiLabel.setText(String.valueOf(todayAppts));
                pendingBillsKpiLabel.setText(String.valueOf(pendingBills));
                revenueKpiLabel.setText("$" + String.format("%.2f", revenue != null ? revenue.doubleValue() : 0.0));

                recentApptModel.setRowCount(0);
                if (recentList != null) {
                    int count = 0;
                    for (Appointment a : recentList) {
                        if (count++ >= 15) break; // show top 15
                        recentApptModel.addRow(new Object[]{
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
            }
        };
        worker.execute();
    }
}
