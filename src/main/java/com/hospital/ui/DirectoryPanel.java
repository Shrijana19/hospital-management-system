package com.hospital.ui;

import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.model.Person;
import com.hospital.service.PatientService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Hospital Personnel & Patient Unified Directory.
 * DIRECT OOPS DEMONSTRATION: Runtime Polymorphism & Dynamic Method Dispatch.
 * Iterates over a heterogeneous collection of Person references (List<Person>)
 * and invokes abstract methods getSummary() and displayInfo().
 */
public class DirectoryPanel extends JPanel {

    private final PatientService patientService;
    private DefaultTableModel tableModel;
    private JTable directoryTable;
    private List<Person> currentDirectory;

    public DirectoryPanel(PatientService patientService) {
        this.patientService = patientService;
        initComponents();
        loadDirectoryAsync();
    }

    private void initComponents() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.COLOR_OFFWHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top Educational Card (Explains Polymorphism for Viva)
        JPanel bannerCard = UITheme.createCardPanel();
        bannerCard.setLayout(new BorderLayout(8, 6));

        JLabel title = new JLabel("Unified Directory - Runtime Polymorphism Demonstration");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_NAVY);

        JLabel expl = new JLabel(
                "<html><b>OOPS Concept in Action:</b> This screen retrieves both <code>Patient</code> and <code>Doctor</code> instances "
                + "as abstract <code>Person</code> references in a single <code>List&lt;Person&gt;</code>. "
                + "Calling <code>person.getSummary()</code> and <code>person.displayInfo()</code> invokes dynamic method dispatch at runtime.</html>");
        expl.setFont(UITheme.FONT_REGULAR);
        expl.setForeground(UITheme.COLOR_TEXT_MUTED);

        bannerCard.add(title, BorderLayout.NORTH);
        bannerCard.add(expl, BorderLayout.CENTER);
        add(bannerCard, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(8, 8));

        String[] cols = {"Ref ID", "Entity Subclass", "Name", "Contact Phone", "Polymorphic getSummary() Output"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        directoryTable = new JTable(tableModel);
        UITheme.styleTable(directoryTable);
        directoryTable.getColumnModel().getColumn(4).setPreferredWidth(320);

        tableCard.add(new JScrollPane(directoryTable), BorderLayout.CENTER);

        // Bottom Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actionPanel.setBackground(UITheme.COLOR_WHITE);

        JButton inspectBtn = UITheme.createPrimaryButton("Inspect Selected Profile (Invokes displayInfo())");
        inspectBtn.addActionListener(e -> inspectSelectedPerson());

        JButton refreshBtn = UITheme.createOutlineButton("Refresh Directory");
        refreshBtn.addActionListener(e -> loadDirectoryAsync());

        actionPanel.add(inspectBtn);
        actionPanel.add(refreshBtn);
        tableCard.add(actionPanel, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);
    }

    public void loadDirectoryAsync() {
        SwingWorker<List<Person>, Void> worker = new SwingWorker<List<Person>, Void>() {
            @Override
            protected List<Person> doInBackground() {
                return patientService.getHospitalDirectory();
            }

            @Override
            protected void done() {
                try {
                    currentDirectory = get();
                    tableModel.setRowCount(0);
                    for (Person p : currentDirectory) {
                        String type = (p instanceof Doctor) ? "Doctor (Clinician)" : "Patient (Admitted/OPD)";
                        // Polymorphic method call: p.getSummary()
                        tableModel.addRow(new Object[]{
                                p.getId(),
                                type,
                                p.getName(),
                                p.getPhone(),
                                p.getSummary()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DirectoryPanel.this,
                            "Error loading directory: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void inspectSelectedPerson() {
        int row = directoryTable.getSelectedRow();
        if (row < 0 || currentDirectory == null || row >= currentDirectory.size()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a person from the directory table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Person p = currentDirectory.get(row);
        // Direct Polymorphism: p.displayInfo() dispatches dynamically to either Patient.displayInfo() or Doctor.displayInfo()
        String profileCardText = p.displayInfo();

        JDialog dialog = new JDialog((java.awt.Frame) null, "Polymorphism Inspection: " + p.getClass().getSimpleName(), true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JTextArea area = new JTextArea(profileCardText);
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
    }
}
