package com.hospital.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Design system and styling utility for the Hospital Management System.
 * Enforces the required color palette and typography across all screens:
 * - Navy: #16324F
 * - Teal: #168C8C
 * - Mint: #EAF6F1
 * - Off-white: #F7F8F4
 * - Text: #183044
 * - Success: #218653
 * - Warning: #A96600
 * - Error: #B42332
 */
public final class UITheme {

    // Palette Colors
    public static final Color COLOR_NAVY = new Color(0x16, 0x32, 0x4F);
    public static final Color COLOR_NAVY_LIGHT = new Color(0x23, 0x47, 0x6D);
    public static final Color COLOR_TEAL = new Color(0x16, 0x8C, 0x8C);
    public static final Color COLOR_TEAL_DARK = new Color(0x10, 0x6B, 0x6B);
    public static final Color COLOR_MINT = new Color(0xEA, 0xF6, 0xF1);
    public static final Color COLOR_OFFWHITE = new Color(0xF7, 0xF8, 0xF4);
    public static final Color COLOR_TEXT = new Color(0x18, 0x30, 0x44);
    public static final Color COLOR_TEXT_MUTED = new Color(0x60, 0x72, 0x82);
    public static final Color COLOR_BORDER = new Color(0xCF, 0xD8, 0xDC);
    public static final Color COLOR_WHITE = Color.WHITE;

    public static final Color COLOR_SUCCESS = new Color(0x21, 0x86, 0x53);
    public static final Color COLOR_WARNING = new Color(0xA9, 0x66, 0x00);
    public static final Color COLOR_ERROR = new Color(0xB4, 0x23, 0x32);

    // Typography
    private static final String FONT_FAMILY = "Segoe UI";
    public static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font(FONT_FAMILY, Font.BOLD, 17);
    public static final Font FONT_SUBHEADER = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 13);

    private UITheme() {
        // Utility class
    }

    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, COLOR_TEAL, COLOR_WHITE, COLOR_TEAL_DARK);
    }

    public static JButton createSecondaryButton(String text) {
        return createStyledButton(text, COLOR_NAVY, COLOR_WHITE, COLOR_NAVY_LIGHT);
    }

    public static JButton createDangerButton(String text) {
        return createStyledButton(text, COLOR_ERROR, COLOR_WHITE, new Color(0x8E, 0x1A, 0x26));
    }

    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, COLOR_SUCCESS, COLOR_WHITE, new Color(0x17, 0x63, 0x3D));
    }

    public static JButton createOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(COLOR_NAVY);
        btn.setBackground(COLOR_WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_NAVY, 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        return btn;
    }

    private static JButton createStyledButton(String text, Color bg, Color fg, Color hoverBg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(hoverBg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(hoverBg);
                } else {
                    g2.setColor(getBackground());
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        return btn;
    }

    public static JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_REGULAR);
        tf.setForeground(COLOR_TEXT);
        tf.setBackground(COLOR_WHITE);
        tf.setCaretColor(COLOR_TEXT);
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 36));
        tf.setBorder(createInputBorder());
        return tf;
    }

    public static <E> JComboBox<E> createComboBox() {
        JComboBox<E> cb = new JComboBox<>();
        cb.setFont(FONT_REGULAR);
        cb.setForeground(COLOR_TEXT);
        cb.setBackground(COLOR_WHITE);
        cb.setPreferredSize(new Dimension(cb.getPreferredSize().width, 36));
        cb.setBorder(createInputBorder());
        return cb;
    }

    public static Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        );
    }

    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(COLOR_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));
        return card;
    }

    public static JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BOLD);
        label.setForeground(COLOR_TEXT);
        return label;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setForeground(COLOR_TEXT);
        table.setRowHeight(34);
        table.setGridColor(new Color(0xEB, 0xF0, 0xF3));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(COLOR_MINT);
        table.setSelectionForeground(COLOR_TEXT);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_SUBHEADER);
        header.setBackground(COLOR_NAVY);
        header.setForeground(COLOR_WHITE);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
        header.setOpaque(true);
        header.setReorderingAllowed(false);

        // Custom header renderer ensures high-contrast navy background and bold white headings
        // under all Look-and-Feels including Windows Native L&F
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(COLOR_NAVY);
                label.setForeground(COLOR_WHITE);
                label.setFont(FONT_SUBHEADER);
                label.setHorizontalAlignment(column == 0 ? JLabel.CENTER : JLabel.LEFT);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(0x13, 0x2A, 0x3B)),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
                return label;
            }
        };
        header.setDefaultRenderer(headerRenderer);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        // Default center alignment for integer ID columns
        if (table.getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        }
    }

    public static JScrollPane createStyledScrollPane(Component comp) {
        JScrollPane sp = new JScrollPane(comp);
        sp.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        sp.getViewport().setBackground(COLOR_WHITE);
        return sp;
    }
}
