package com.hospital.ui;

import com.hospital.exception.ValidationException;
import com.hospital.model.Appointment;
import com.hospital.model.Bill;
import com.hospital.model.Patient;
import com.hospital.service.AppointmentService;
import com.hospital.service.BillingService;
import com.hospital.service.DoctorService;
import com.hospital.service.PatientService;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
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
 * Patient Billing & Financial Accounting Panel.
 * Features:
 * 1. Automatic total calculation (user cannot type total).
 * 2. Overloaded calculateTotal() logic.
 * 3. Atomic billing transaction (marks linked appointment Completed).
 * 4. Printable official invoice preview dialog.
 */
public class BillingPanel extends JPanel {

    private final BillingService billingService;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final DoctorService doctorService;

    // Form Components
    private JComboBox<PatientComboItem> patientCombo;
    private JComboBox<AppointmentComboItem> appointmentCombo;
    private JTextField consultationFeeField;
    private JTextField otherChargesField;
    private JTextField totalField;
    private JComboBox<String> paymentStatusCombo;

    // Table Components
    private JComboBox<String> filterStatusCombo;
    private DefaultTableModel tableModel;
    private JTable billTable;

    public BillingPanel(BillingService billingService, PatientService patientService,
                        AppointmentService appointmentService, DoctorService doctorService) {
        this.billingService = billingService;
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;

        initComponents();
        loadDropdowns();
        loadBillsAsync();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.COLOR_OFFWHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.COLOR_OFFWHITE);
        JLabel title = new JLabel("Billing & Patient Invoices");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.COLOR_NAVY);
        topPanel.add(title, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Left Form Card (Generate Bill)
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        formCard.setMinimumSize(new Dimension(360, 420));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;
        JLabel formHeader = new JLabel("Generate Patient Invoice");
        formHeader.setFont(UITheme.FONT_HEADER);
        formHeader.setForeground(UITheme.COLOR_NAVY);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formCard.add(formHeader, gbc);

        // Patient Selector
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Select Patient *"), gbc);
        patientCombo = UITheme.createComboBox();
        patientCombo.addActionListener(e -> onPatientSelected());
        gbc.gridx = 1;
        formCard.add(patientCombo, gbc);
        row++;

        // Appointment Link
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Linked Appointment"), gbc);
        appointmentCombo = UITheme.createComboBox();
        appointmentCombo.addActionListener(e -> onAppointmentSelected());
        gbc.gridx = 1;
        formCard.add(appointmentCombo, gbc);
        row++;

        // Consultation Fee
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Consultation Fee ($) *"), gbc);
        consultationFeeField = UITheme.createTextField();
        consultationFeeField.setText("0.00");
        consultationFeeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                recalcTotal();
            }
        });
        gbc.gridx = 1;
        formCard.add(consultationFeeField, gbc);
        row++;

        // Other Charges (Lab, Medicines, Tests)
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Other Charges ($)"), gbc);
        otherChargesField = UITheme.createTextField();
        otherChargesField.setText("0.00");
        otherChargesField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                recalcTotal();
            }
        });
        gbc.gridx = 1;
        formCard.add(otherChargesField, gbc);
        row++;

        // Total Amount (Calculated Automatically - Read Only)
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Total Due ($) [Auto]"), gbc);
        totalField = UITheme.createTextField();
        totalField.setEditable(false);
        totalField.setFont(UITheme.FONT_TITLE);
        totalField.setForeground(UITheme.COLOR_TEAL_DARK);
        totalField.setBackground(UITheme.COLOR_MINT);
        totalField.setText("$0.00");
        gbc.gridx = 1;
        formCard.add(totalField, gbc);
        row++;

        // Payment Status
        gbc.gridx = 0; gbc.gridy = row;
        formCard.add(UITheme.createFieldLabel("Payment Status *"), gbc);
        paymentStatusCombo = UITheme.createComboBox();
        paymentStatusCombo.addItem(Bill.STATUS_PENDING);
        paymentStatusCombo.addItem(Bill.STATUS_PAID);
        gbc.gridx = 1;
        formCard.add(paymentStatusCombo, gbc);
        row++;

        // Generate Bill Button
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JButton generateBtn = UITheme.createPrimaryButton("Generate & Issue Bill");
        generateBtn.setPreferredSize(new Dimension(generateBtn.getPreferredSize().width, 38));
        generateBtn.addActionListener(e -> generateBill());
        formCard.add(generateBtn, gbc);

        // Right Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(8, 8));

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterBar.setBackground(UITheme.COLOR_WHITE);
        filterBar.add(new JLabel("Filter Status:"));
        filterStatusCombo = UITheme.createComboBox();
        filterStatusCombo.addItem("All");
        filterStatusCombo.addItem(Bill.STATUS_PAID);
        filterStatusCombo.addItem(Bill.STATUS_PENDING);
        filterStatusCombo.setPreferredSize(new Dimension(130, 32));
        filterStatusCombo.addActionListener(e -> applyFilter());
        filterBar.add(filterStatusCombo);

        JButton refreshBtn = UITheme.createOutlineButton("Refresh");
        refreshBtn.setPreferredSize(new Dimension(80, 32));
        refreshBtn.addActionListener(e -> loadBillsAsync());
        filterBar.add(refreshBtn);
        tableCard.add(filterBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Bill ID", "Patient Name", "Appt Ref", "Fee ($)", "Other ($)", "Total ($)", "Status", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        billTable = new JTable(tableModel);
        UITheme.styleTable(billTable);
        tableCard.add(new JScrollPane(billTable), BorderLayout.CENTER);

        // Actions at bottom
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actionPanel.setBackground(UITheme.COLOR_WHITE);

        JButton markPaidBtn = UITheme.createSuccessButton("Mark Selected as Paid");
        markPaidBtn.addActionListener(e -> markSelectedAsPaid());

        JButton viewInvoiceBtn = UITheme.createPrimaryButton("View Printable Invoice");
        viewInvoiceBtn.addActionListener(e -> showPrintableInvoiceDialog());

        actionPanel.add(markPaidBtn);
        actionPanel.add(viewInvoiceBtn);
        tableCard.add(actionPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formCard, tableCard);
        splitPane.setDividerLocation(380);
        splitPane.setResizeWeight(0.35);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);
    }

    private void recalcTotal() {
        try {
            String feeStr = consultationFeeField.getText().trim();
            String otherStr = otherChargesField.getText().trim();
            BigDecimal fee = feeStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(feeStr);
            BigDecimal others = otherStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(otherStr);
            BigDecimal total = billingService.calculateTotal(fee, others);
            totalField.setText("$" + String.format("%.2f", total.doubleValue()));
        } catch (Exception e) {
            totalField.setText("Invalid input");
        }
    }

    private void loadDropdowns() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private List<Patient> patients;

            @Override
            protected Void doInBackground() {
                patients = patientService.getAllPatients();
                return null;
            }

            @Override
            protected void done() {
                patientCombo.removeAllItems();
                for (Patient p : patients) {
                    patientCombo.addItem(new PatientComboItem(p.getId(), p.getName()));
                }
                onPatientSelected();
            }
        };
        worker.execute();
    }

    private void onPatientSelected() {
        PatientComboItem item = (PatientComboItem) patientCombo.getSelectedItem();
        if (item == null) return;

        SwingWorker<List<Appointment>, Void> worker = new SwingWorker<List<Appointment>, Void>() {
            @Override
            protected List<Appointment> doInBackground() {
                return appointmentService.filterAppointments(item.id, null, null, null);
            }

            @Override
            protected void done() {
                try {
                    List<Appointment> appts = get();
                    appointmentCombo.removeAllItems();
                    appointmentCombo.addItem(new AppointmentComboItem(null, "None (Direct Billing)"));
                    for (Appointment a : appts) {
                        appointmentCombo.addItem(new AppointmentComboItem(a.getId(),
                                "Appt #" + a.getId() + " - " + a.getDoctorName() + " (" + a.getFormattedDateTime() + ")"));
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void onAppointmentSelected() {
        AppointmentComboItem item = (AppointmentComboItem) appointmentCombo.getSelectedItem();
        if (item != null && item.appointmentId != null) {
            SwingWorker<Appointment, Void> worker = new SwingWorker<Appointment, Void>() {
                @Override
                protected Appointment doInBackground() {
                    return appointmentService.getById(item.appointmentId).orElse(null);
                }

                @Override
                protected void done() {
                    try {
                        Appointment appt = get();
                        if (appt != null) {
                            doctorService.getDoctorById(appt.getDoctorId()).ifPresent(d -> {
                                consultationFeeField.setText(d.getConsultationFee().toString());
                                recalcTotal();
                            });
                        }
                    } catch (Exception ignored) {
                    }
                }
            };
            worker.execute();
        }
    }

    private void generateBill() {
        PatientComboItem patient = (PatientComboItem) patientCombo.getSelectedItem();
        AppointmentComboItem appt = (AppointmentComboItem) appointmentCombo.getSelectedItem();

        if (patient == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient.", "Missing Field", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            BigDecimal fee = new BigDecimal(consultationFeeField.getText().trim());
            BigDecimal others = new BigDecimal(otherChargesField.getText().trim());
            String status = (String) paymentStatusCombo.getSelectedItem();
            Integer apptId = (appt != null) ? appt.appointmentId : null;

            SwingWorker<Bill, Void> worker = new SwingWorker<Bill, Void>() {
                @Override
                protected Bill doInBackground() {
                    return billingService.generateBill(patient.id, apptId, fee, others, status);
                }

                @Override
                protected void done() {
                    try {
                        Bill bill = get();
                        JOptionPane.showMessageDialog(BillingPanel.this,
                                "Bill successfully generated!\nBill ID: #" + bill.getId()
                                + "\nTotal Amount: $" + String.format("%.2f", bill.getTotalAmount().doubleValue())
                                + (apptId != null ? "\nLinked appointment marked as 'Completed'." : ""),
                                "Invoice Generated", JOptionPane.INFORMATION_MESSAGE);
                        loadBillsAsync();
                        // Reset other charges
                        otherChargesField.setText("0.00");
                        recalcTotal();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(BillingPanel.this, cause.getMessage(), "Billing Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric monetary amounts.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void markSelectedAsPaid() {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a bill from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int billId = (int) tableModel.getValueAt(row, 0);
        String currentStatus = (String) tableModel.getValueAt(row, 6);

        if (Bill.STATUS_PAID.equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This bill is already recorded as Paid.", "Already Paid", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Mark Bill #" + billId + " as Paid in full?", "Confirm Payment", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return billingService.markAsPaid(billId);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(BillingPanel.this, "Bill #" + billId + " updated to Paid.", "Payment Recorded", JOptionPane.INFORMATION_MESSAGE);
                        loadBillsAsync();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(BillingPanel.this, "Failed to update status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void showPrintableInvoiceDialog() {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a bill to view its invoice.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int billId = (int) tableModel.getValueAt(row, 0);
        SwingWorker<Bill, Void> worker = new SwingWorker<Bill, Void>() {
            @Override
            protected Bill doInBackground() {
                return billingService.getById(billId).orElse(null);
            }

            @Override
            protected void done() {
                try {
                    Bill bill = get();
                    if (bill == null) return;

                    String invoiceText = billingService.generatePrintableInvoice(bill);

                    JDialog dialog = new JDialog((java.awt.Frame) null, "Invoice Preview - INV-" + String.format("%06d", bill.getId()), true);
                    dialog.setSize(520, 500);
                    dialog.setLocationRelativeTo(BillingPanel.this);
                    dialog.setLayout(new BorderLayout(10, 10));

                    JTextArea area = new JTextArea(invoiceText);
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

    public void loadBillsAsync() {
        SwingWorker<List<Bill>, Void> worker = new SwingWorker<List<Bill>, Void>() {
            @Override
            protected List<Bill> doInBackground() {
                return billingService.getAllBills();
            }

            @Override
            protected void done() {
                try {
                    populateTable(get());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BillingPanel.this, "Error loading bills: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void applyFilter() {
        String filter = (String) filterStatusCombo.getSelectedItem();
        SwingWorker<List<Bill>, Void> worker = new SwingWorker<List<Bill>, Void>() {
            @Override
            protected List<Bill> doInBackground() {
                List<Bill> all = billingService.getAllBills();
                if (filter == null || filter.equalsIgnoreCase("All")) {
                    return all;
                }
                List<Bill> filtered = new java.util.ArrayList<>();
                for (Bill b : all) {
                    if (b.getPaymentStatus().equalsIgnoreCase(filter)) {
                        filtered.add(b);
                    }
                }
                return filtered;
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

    private void populateTable(List<Bill> list) {
        tableModel.setRowCount(0);
        for (Bill b : list) {
            tableModel.addRow(new Object[]{
                    b.getId(),
                    b.getPatientName(),
                    b.getAppointmentId() != null ? "#" + b.getAppointmentId() : "-",
                    String.format("%.2f", b.getConsultationFee().doubleValue()),
                    String.format("%.2f", b.getOtherCharges().doubleValue()),
                    String.format("%.2f", b.getTotalAmount().doubleValue()),
                    b.getPaymentStatus(),
                    b.getFormattedDate()
            });
        }
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
            return "#" + id + " - " + name;
        }
    }

    private static class AppointmentComboItem {
        final Integer appointmentId;
        final String label;

        AppointmentComboItem(Integer appointmentId, String label) {
            this.appointmentId = appointmentId;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
