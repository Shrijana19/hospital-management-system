package com.hospital.ui;

import com.hospital.model.User;
import com.hospital.service.AppointmentService;
import com.hospital.service.AuthService;
import com.hospital.service.BillingService;
import com.hospital.service.DoctorService;
import com.hospital.service.MedicalRecordService;
import com.hospital.service.PatientService;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Main Application Dashboard Frame.
 * Modern responsive desktop layout featuring:
 * - Persistent Navy sidebar with navigation controls and user identity.
 * - Central content workspace managed via CardLayout.
 * - Role-based menu filtering (User Management accessible to ADMIN only).
 */
public class DashboardFrame extends JFrame {

    private final User currentUser;
    private final AuthService authService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final BillingService billingService;
    private final MedicalRecordService medicalRecordService;

    private CardLayout cardLayout;
    private JPanel contentContainer;
    private JLabel currentScreenTitle;
    private List<NavButton> navButtons = new ArrayList<>();

    // Panels
    private OverviewPanel overviewPanel;
    private PatientPanel patientPanel;
    private DoctorPanel doctorPanel;
    private AppointmentPanel appointmentPanel;
    private BillingPanel billingPanel;
    private MedicalRecordPanel medicalRecordPanel;
    private DirectoryPanel directoryPanel;
    private UserPanel userPanel;

    public DashboardFrame(User user) {
        this.currentUser = user;
        this.authService = new AuthService();
        this.patientService = new PatientService();
        this.doctorService = new DoctorService();
        this.appointmentService = new AppointmentService();
        this.billingService = new BillingService();
        this.medicalRecordService = new MedicalRecordService();

        initComponents();
    }

    private void initComponents() {
        setTitle("Hospital Management System - " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.COLOR_OFFWHITE);
        setLayout(new BorderLayout());

        // Left Navigation Sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Right Workspace Container (Top Bar + Center Content)
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(UITheme.COLOR_OFFWHITE);

        // Top Status Header Bar
        JPanel topBar = createTopBar();
        workspace.add(topBar, BorderLayout.NORTH);

        // Center Card Panels
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(UITheme.COLOR_OFFWHITE);

        overviewPanel = new OverviewPanel(patientService, doctorService, appointmentService, billingService);
        patientPanel = new PatientPanel(patientService);
        doctorPanel = new DoctorPanel(doctorService);
        appointmentPanel = new AppointmentPanel(appointmentService, patientService, doctorService);
        billingPanel = new BillingPanel(billingService, patientService, appointmentService, doctorService);
        medicalRecordPanel = new MedicalRecordPanel(medicalRecordService, patientService, doctorService);
        directoryPanel = new DirectoryPanel(patientService);

        contentContainer.add(overviewPanel, "OVERVIEW");
        contentContainer.add(patientPanel, "PATIENTS");
        contentContainer.add(doctorPanel, "DOCTORS");
        contentContainer.add(appointmentPanel, "APPOINTMENTS");
        contentContainer.add(billingPanel, "BILLING");
        contentContainer.add(medicalRecordPanel, "RECORDS");
        contentContainer.add(directoryPanel, "DIRECTORY");

        if (currentUser.isAdmin()) {
            userPanel = new UserPanel(authService);
            contentContainer.add(userPanel, "USERS");
        }

        workspace.add(contentContainer, BorderLayout.CENTER);
        add(workspace, BorderLayout.CENTER);

        // Select initial view
        selectNavItem("OVERVIEW", "Dashboard Overview");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout());
        sidebar.setBackground(UITheme.COLOR_NAVY);
        sidebar.setPreferredSize(new Dimension(240, 800));

        // Brand Banner
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBackground(UITheme.COLOR_NAVY);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(22, 18, 22, 18));

        JLabel brandTitle = new JLabel("GREENDALE HMS");
        brandTitle.setFont(UITheme.FONT_HEADER);
        brandTitle.setForeground(UITheme.COLOR_WHITE);

        JLabel brandSub = new JLabel("Clinical & OPD Portal");
        brandSub.setFont(UITheme.FONT_SMALL);
        brandSub.setForeground(new Color(0x9E, 0xB3, 0xC6));

        brandPanel.add(brandTitle);
        brandPanel.add(Box.createVerticalStrut(4));
        brandPanel.add(brandSub);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Navigation Menu
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(UITheme.COLOR_NAVY);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addNavButton(menuPanel, "Dashboard", "OVERVIEW", "Dashboard Overview");
        addNavButton(menuPanel, "Patients", "PATIENTS", "Patient Directory & Registration");
        addNavButton(menuPanel, "Doctors", "DOCTORS", "Doctor & Specialist Directory");
        addNavButton(menuPanel, "Appointments", "APPOINTMENTS", "Appointment Queue & Slot Booking");
        addNavButton(menuPanel, "Billing & Invoices", "BILLING", "Billing Management & Invoices");
        addNavButton(menuPanel, "Medical Records", "RECORDS", "Electronic Medical Records");
        addNavButton(menuPanel, "Hospital Directory", "DIRECTORY", "Unified Directory (Polymorphism Demo)");

        if (currentUser.isAdmin()) {
            addNavButton(menuPanel, "User Management", "USERS", "System Access & Staff Accounts");
        }

        menuPanel.add(Box.createVerticalGlue());
        sidebar.add(menuPanel, BorderLayout.CENTER);

        // User Profile & Logout Box (Bottom)
        JPanel userBox = new JPanel();
        userBox.setLayout(new BoxLayout(userBox, BoxLayout.Y_AXIS));
        userBox.setBackground(new Color(0x0E, 0x22, 0x36));
        userBox.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel userNameLbl = new JLabel(currentUser.getFullName());
        userNameLbl.setFont(UITheme.FONT_BOLD);
        userNameLbl.setForeground(UITheme.COLOR_WHITE);

        JLabel userRoleLbl = new JLabel("Role: " + currentUser.getRole());
        userRoleLbl.setFont(UITheme.FONT_SMALL);
        userRoleLbl.setForeground(UITheme.COLOR_TEAL);

        JButton logoutBtn = new JButton("Log Out");
        logoutBtn.setFont(UITheme.FONT_SMALL);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(UITheme.COLOR_ERROR);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        logoutBtn.addActionListener(e -> performLogout());

        userBox.add(userNameLbl);
        userBox.add(Box.createVerticalStrut(3));
        userBox.add(userRoleLbl);
        userBox.add(Box.createVerticalStrut(10));
        userBox.add(logoutBtn);

        sidebar.add(userBox, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addNavButton(JPanel container, String text, String cardKey, String title) {
        NavButton btn = new NavButton(text, cardKey);
        btn.addActionListener(e -> selectNavItem(cardKey, title));
        navButtons.add(btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(6));
    }

    private void selectNavItem(String cardKey, String screenTitle) {
        cardLayout.show(contentContainer, cardKey);
        currentScreenTitle.setText(screenTitle);

        for (NavButton btn : navButtons) {
            btn.setActive(btn.cardKey.equals(cardKey));
        }

        // Trigger data refresh on the active panel
        if ("OVERVIEW".equals(cardKey)) {
            overviewPanel.refreshData();
        } else if ("PATIENTS".equals(cardKey)) {
            patientPanel.loadPatientsAsync();
        } else if ("DOCTORS".equals(cardKey)) {
            doctorPanel.loadDoctorsAsync();
        } else if ("APPOINTMENTS".equals(cardKey)) {
            appointmentPanel.loadAppointmentsAsync();
            appointmentPanel.refreshAvailableSlots();
        } else if ("BILLING".equals(cardKey)) {
            billingPanel.loadBillsAsync();
        } else if ("RECORDS".equals(cardKey)) {
            medicalRecordPanel.loadRecordsAsync();
        } else if ("DIRECTORY".equals(cardKey)) {
            directoryPanel.loadDirectoryAsync();
        } else if ("USERS".equals(cardKey) && userPanel != null) {
            userPanel.loadUsersAsync();
        }
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.COLOR_WHITE);
        topBar.setPreferredSize(new Dimension(topBar.getPreferredSize().width, 54));
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.COLOR_BORDER),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        currentScreenTitle = new JLabel("Dashboard Overview");
        currentScreenTitle.setFont(UITheme.FONT_HEADER);
        currentScreenTitle.setForeground(UITheme.COLOR_NAVY);

        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightInfo.setBackground(UITheme.COLOR_WHITE);

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"));
        JLabel dateLabel = new JLabel(dateStr);
        dateLabel.setFont(UITheme.FONT_REGULAR);
        dateLabel.setForeground(UITheme.COLOR_TEXT_MUTED);

        JLabel userTag = new JLabel("[" + currentUser.getRole() + "] " + currentUser.getUsername());
        userTag.setFont(UITheme.FONT_BOLD);
        userTag.setForeground(UITheme.COLOR_TEAL_DARK);

        rightInfo.add(dateLabel);
        rightInfo.add(userTag);

        topBar.add(currentScreenTitle, BorderLayout.WEST);
        topBar.add(rightInfo, BorderLayout.EAST);
        return topBar;
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out of the system?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.logout();
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
                dispose();
            });
        }
    }

    /**
     * Custom styled navigation sidebar button with active state pill indicator.
     */
    private static class NavButton extends JButton {
        final String cardKey;
        private boolean active;

        NavButton(String text, String cardKey) {
            super(text);
            this.cardKey = cardKey;
            setFont(UITheme.FONT_REGULAR);
            setForeground(new Color(0xD0, 0xDE, 0xEB));
            setBackground(UITheme.COLOR_NAVY);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        }

        void setActive(boolean active) {
            this.active = active;
            if (active) {
                setForeground(UITheme.COLOR_WHITE);
                setFont(UITheme.FONT_BOLD);
            } else {
                setForeground(new Color(0xD0, 0xDE, 0xEB));
                setFont(UITheme.FONT_REGULAR);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g2.setColor(UITheme.COLOR_TEAL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            } else if (getModel().isRollover()) {
                g2.setColor(UITheme.COLOR_NAVY_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
