import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.*;

public class MainGui extends JFrame {
    private final String DB_URL = "jdbc:sqlite:finance.db";
    
    private final Color BG_APP = new Color(15, 15, 18);
    private final Color BG_PANEL = new Color(24, 24, 28);
    private final Color BG_ELEVATED = new Color(32, 32, 38);
    private final Color ACCENT = new Color(14, 210, 170);
    private final Color ACCENT_HOVER = new Color(10, 180, 145);
    private final Color TEXT_MAIN = new Color(245, 245, 250);
    private final Color TEXT_MUTED = new Color(140, 140, 150);
    private final Color BORDER_COLOR = new Color(45, 45, 55);
    private final Color COLOR_DANGER = new Color(239, 68, 68);
    private final Color COLOR_WARNING = new Color(245, 158, 11);
    private final Color COLOR_INFO = new Color(59, 130, 246);

    private final Font FONT_MAIN = new Font("SansSerif", Font.PLAIN, 14);
    private final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 14);
    private final Font FONT_H1 = new Font("SansSerif", Font.BOLD, 26);
    private final Font FONT_NUMBERS = new Font("Monospaced", Font.BOLD, 22);

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> currencyCombo, categoryCombo, monthCombo, yearCombo;
    private JLabel totalSumLabel, countLabel, maxExpenseLabel;

    public MainGui() {
        setupGlobalUI();
        setTitle("Finance Engine [No Patterns Edition]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 850);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_APP);

        JPanel mainPanel = new JPanel(new BorderLayout(25, 25));
        mainPanel.setBackground(BG_APP);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Обзор финансов");
        titleLabel.setFont(FONT_H1);
        titleLabel.setForeground(TEXT_MAIN);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel actionBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionBtnPanel.setOpaque(false);
        
        JButton btnAdd = new ActionButton("Добавить трату", ACCENT, BG_APP);
        JButton btnEdit = new ActionButton("Изменить", COLOR_WARNING, BG_APP);
        JButton btnDel = new ActionButton("Удалить", COLOR_DANGER, BG_APP);
        JButton btnRates = new ActionButton("Курсы валют", COLOR_INFO, BG_APP);

        btnAdd.addActionListener(e -> showExpenseDialog(false));
        btnEdit.addActionListener(e -> showExpenseDialog(true));
        btnDel.addActionListener(e -> deleteData());
        btnRates.addActionListener(e -> showRatesDialog());

        actionBtnPanel.add(btnRates); actionBtnPanel.add(btnDel); actionBtnPanel.add(btnEdit); actionBtnPanel.add(btnAdd);
        headerPanel.add(actionBtnPanel, BorderLayout.EAST);

        JPanel topWrapper = new JPanel(new BorderLayout(0, 25));
        topWrapper.setOpaque(false);
        topWrapper.add(headerPanel, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setOpaque(false);
        totalSumLabel = createCard(cardsPanel, "ОБЩИЙ ИТОГ", "---", ACCENT);
        countLabel = createCard(cardsPanel, "ОПЕРАЦИЙ", "0", COLOR_INFO);
        maxExpenseLabel = createCard(cardsPanel, "МАКСИМУМ", "---", COLOR_DANGER);
        
        JPanel filtersPanel = new RoundedPanel(15, BG_PANEL);
        filtersPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 15));
        
        currencyCombo = createStyledCombo(new String[]{"Оригинал", "RUB", "USD", "EUR", "GBP", "CNY", "KZT"});
        categoryCombo = createStyledCombo(new String[]{"Все категории"});
        monthCombo = createStyledCombo(new String[]{"Все месяцы", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"});
        monthCombo.setSelectedItem("03");
        yearCombo = createStyledCombo(new String[]{"Все годы", "2024", "2025", "2026"});
        yearCombo.setSelectedItem("2026");

        filtersPanel.add(createFilterWrapper("РЕЖИМ:", currencyCombo));
        filtersPanel.add(createFilterWrapper("КАТЕГОРИЯ:", categoryCombo));
        filtersPanel.add(createFilterWrapper("МЕСЯЦ:", monthCombo));
        filtersPanel.add(createFilterWrapper("ГОД:", yearCombo));

        JButton btnApply = new CustomButton("ПРИМЕНИТЬ", ACCENT, BG_APP);
        btnApply.setPreferredSize(new Dimension(180, 40));
        btnApply.addActionListener(e -> updateData());
        
        JPanel applyWrapper = new JPanel(new BorderLayout());
        applyWrapper.setOpaque(false);
        applyWrapper.setBorder(new EmptyBorder(18, 10, 0, 0));
        applyWrapper.add(btnApply, BorderLayout.SOUTH);
        filtersPanel.add(applyWrapper);

        topWrapper.add(cardsPanel, BorderLayout.CENTER); 
        topWrapper.add(filtersPanel, BorderLayout.SOUTH);
        mainPanel.add(topWrapper, BorderLayout.NORTH);

        String[] columns = {"КАТЕГОРИЯ", "СУММА", "ВАЛЮТА", "ДАТА", "ID"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return c == 1 ? BigDecimal.class : String.class; }
        };
        table = new JTable(tableModel);
        table.removeColumn(table.getColumnModel().getColumn(4)); 
        setupTable(table, true);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(BG_PANEL);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());

        JPanel tableWrapper = new RoundedPanel(15, BG_PANEL);
        tableWrapper.setLayout(new BorderLayout());
        tableWrapper.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(tableWrapper, BorderLayout.CENTER);

        add(mainPanel);
        updateCategories();
        updateData();
    }

    private void updateData() {
        tableModel.setRowCount(0);
        String targetCur = (String) currencyCombo.getSelectedItem();
        String targetCat = (String) categoryCombo.getSelectedItem();
        String targetMonth = (String) monthCombo.getSelectedItem();
        String targetYear = (String) yearCombo.getSelectedItem();

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal maxExp = BigDecimal.ZERO;
        int count = 0;

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Map<String, Double> rates = new HashMap<>();
            ResultSet rsRates = conn.createStatement().executeQuery("SELECT from_cur, to_cur, rate FROM exchange_rates");
            while (rsRates.next()) {
                rates.put(rsRates.getString(1) + "_" + rsRates.getString(2), rsRates.getDouble(3));
            }

            ResultSet rs = conn.createStatement().executeQuery("SELECT category, amount, currency, date, id FROM expenses");
            while (rs.next()) {
                String cat = rs.getString(1);
                double amt = rs.getDouble(2);
                String cur = rs.getString(3);
                String date = rs.getString(4);
                int id = rs.getInt(5);

                if (!targetYear.equals("Все годы") && !date.startsWith(targetYear)) continue;
                if (!targetMonth.equals("Все месяцы") && !date.contains("-" + targetMonth + "-")) continue;
                if (!targetCat.equals("Все категории") && !cat.equals(targetCat)) continue;

                BigDecimal displayAmt = new BigDecimal(amt).setScale(2, RoundingMode.HALF_UP);
                String displayCur = cur;

                if (!targetCur.equals("Оригинал") && !cur.equals(targetCur)) {
                    double rate = rates.getOrDefault(cur + "_" + targetCur, 1.0);
                    displayAmt = displayAmt.multiply(new BigDecimal(rate)).setScale(2, RoundingMode.HALF_UP);
                    displayCur = targetCur;
                }

                tableModel.addRow(new Object[]{cat.toUpperCase(), displayAmt, displayCur, date, id});
                total = total.add(displayAmt);
                if (displayAmt.compareTo(maxExp) > 0) maxExp = displayAmt;
                count++;
            }
        } catch (SQLException e) { e.printStackTrace(); }

        countLabel.setText(String.valueOf(count));
        if (!targetCur.equals("Оригинал")) {
            totalSumLabel.setText(String.format("%,.2f %s", total, targetCur));
            maxExpenseLabel.setText(String.format("%,.2f %s", maxExp, targetCur));
            table.getColumnModel().getColumn(1).setCellRenderer(new BarChartCellRenderer(maxExp));
        } else {
            totalSumLabel.setText("СМЕШАННЫЕ ДАННЫЕ"); maxExpenseLabel.setText("---");
            table.getColumnModel().getColumn(1).setCellRenderer(new BarChartCellRenderer(BigDecimal.ZERO));
        }
    }

    private void deleteData() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        int modelRow = table.convertRowIndexToModel(r);
        int id = (int) tableModel.getValueAt(modelRow, 4);
        if (JOptionPane.showConfirmDialog(this, "Удалить запись?", "Удаление", 0) == 0) {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM expenses WHERE id=?");
                ps.setInt(1, id); ps.executeUpdate();
                updateData(); updateCategories();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void showExpenseDialog(boolean isEdit) {
        int id = -1;
        String oldCat = "", oldAmt = "", oldCur = "RUB", oldDate = "2026-03-21";
        
        if (isEdit) {
            int r = table.getSelectedRow();
            if (r < 0) return;
            int modelRow = table.convertRowIndexToModel(r);
            id = (int) tableModel.getValueAt(modelRow, 4);
            oldCat = tableModel.getValueAt(modelRow, 0).toString();
            oldAmt = tableModel.getValueAt(modelRow, 1).toString();
            oldCur = tableModel.getValueAt(modelRow, 2).toString();
            oldDate = tableModel.getValueAt(modelRow, 3).toString();
        }

        JDialog d = new JDialog(this, isEdit ? "Изменить" : "Новая трата", true);
        d.setSize(420, 480); d.setLocationRelativeTo(this); d.getContentPane().setBackground(BG_PANEL); d.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(12, 20, 12, 20); g.fill = 2; g.weightx = 1.0;

        JTextField fCat = createStyledTextField(oldCat);
        JTextField fAmt = createStyledTextField(oldAmt);
        JTextField fCur = createStyledTextField(oldCur);
        JTextField fDate = createStyledTextField(oldDate);

        addDialogField(d, "Название категории:", fCat, g, 0);
        addDialogField(d, "Сумма:", fAmt, g, 1);
        addDialogField(d, "Валюта:", fCur, g, 2);
        addDialogField(d, "Дата (ГГГГ-ММ-ДД):", fDate, g, 3);

        JButton b = new CustomButton("СОХРАНИТЬ", ACCENT, BG_APP);
        final int finalId = id;
        b.addActionListener(e -> {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                PreparedStatement ps;
                if (!isEdit) {
                    ps = conn.prepareStatement("INSERT INTO expenses (category, amount, currency, date) VALUES (?,?,?,?)");
                } else {
                    ps = conn.prepareStatement("UPDATE expenses SET category=?, amount=?, currency=?, date=? WHERE id=?");
                    ps.setInt(5, finalId);
                }
                ps.setString(1, fCat.getText());
                ps.setDouble(2, Double.parseDouble(fAmt.getText().replace(",",".")));
                ps.setString(3, fCur.getText().toUpperCase());
                ps.setString(4, fDate.getText());
                ps.executeUpdate();
                updateData(); updateCategories(); d.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(d, "Ошибка данных!"); }
        });
        g.gridy = 4; g.gridwidth = 2; d.add(b, g);
        d.setVisible(true);
    }

    private void showRatesDialog() {
        JDialog d = new JDialog(this, "Курсы Валют", true);
        d.setSize(500, 450); d.setLocationRelativeTo(this); d.getContentPane().setBackground(BG_PANEL);
        d.setLayout(new BorderLayout(15, 15));
        
        String[] cols = {"Из", "В", "Курс", "ID"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 2; }
        };
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT from_cur, to_cur, rate, id FROM exchange_rates");
            while (rs.next()) m.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getDouble(3), rs.getInt(4)});
        } catch (SQLException e) { e.printStackTrace(); }
        
        JTable t = new JTable(m);
        t.getColumnModel().getColumn(3).setMinWidth(0); t.getColumnModel().getColumn(3).setMaxWidth(0);
        setupTable(t, false);
        
        JButton b = new CustomButton("СОХРАНИТЬ", ACCENT, BG_APP);
        b.addActionListener(e -> {
            if (t.isEditing()) t.getCellEditor().stopCellEditing();
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                PreparedStatement ps = conn.prepareStatement("UPDATE exchange_rates SET rate=? WHERE id=?");
                for (int i = 0; i < m.getRowCount(); i++) {
                    ps.setDouble(1, Double.parseDouble(m.getValueAt(i, 2).toString().replace(",",".")));
                    ps.setInt(2, (int)m.getValueAt(i, 3)); ps.executeUpdate();
                }
                updateData(); d.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(d, "Ошибка!"); }
        });
        d.add(new JScrollPane(t), "Center"); d.add(b, "South"); d.setVisible(true);
    }

    private void updateCategories() {
        String current = (String) categoryCombo.getSelectedItem();
        categoryCombo.removeAllItems();
        categoryCombo.addItem("Все категории");
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT DISTINCT category FROM expenses ORDER BY category");
            while (rs.next()) categoryCombo.addItem(rs.getString(1));
        } catch (SQLException e) { e.printStackTrace(); }
        if (current != null) categoryCombo.setSelectedItem(current);
    }

    private void setupTable(JTable t, boolean isMain) {
        t.setBackground(BG_PANEL); t.setForeground(TEXT_MAIN); t.setFont(FONT_MAIN);
        t.setRowHeight(isMain ? 48 : 35); t.setGridColor(BORDER_COLOR);
        t.setSelectionBackground(BG_ELEVATED); t.setSelectionForeground(ACCENT);
        t.setShowVerticalLines(false);
        t.setAutoCreateRowSorter(true); // РАБОТАЕТ СОРТИРОВКА

        JTableHeader h = t.getTableHeader();
        h.setBackground(BG_APP); h.setForeground(TEXT_MUTED); h.setFont(FONT_BOLD);
        h.setPreferredSize(new Dimension(0, isMain ? 50 : 40));

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(new EmptyBorder(0, 15, 0, 0));
                setBackground(isSelected ? BG_ELEVATED : BG_PANEL);
                setForeground(isSelected ? ACCENT : TEXT_MAIN);
                return this;
            }
        };
        for (int i = 0; i < t.getColumnCount(); i++) {
            if (t.getColumnModel().getColumn(i).getCellRenderer() == null || !(t.getColumnModel().getColumn(i).getCellRenderer() instanceof BarChartCellRenderer)) {
                t.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
            }
        }
    }

    private JComboBox<String> createStyledCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setPreferredSize(new Dimension(140, 40)); c.setFont(FONT_BOLD);
        c.setBackground(BG_APP); c.setForeground(ACCENT);
        c.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton b = new JButton("▼"); b.setBackground(BG_APP); b.setForeground(TEXT_MUTED);
                b.setBorder(null); b.setContentAreaFilled(false); return b;
            }
        });
        return c;
    }

    private JTextField createStyledTextField(String text) {
        JTextField f = new JTextField(text);
        f.setPreferredSize(new Dimension(0, 40)); f.setBackground(BG_APP);
        f.setForeground(ACCENT); f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(0, 10, 0, 10)));
        return f;
    }

    private void addDialogField(JDialog d, String l, JTextField f, GridBagConstraints g, int y) {
        g.gridy = y*2; g.gridx = 0; JLabel lbl = new JLabel(l); lbl.setForeground(TEXT_MUTED); lbl.setFont(FONT_BOLD); d.add(lbl, g);
        g.gridy = y*2+1; d.add(f, g);
    }

    private JPanel createFilterWrapper(String title, JComponent c) {
        JPanel w = new JPanel(new BorderLayout(0, 6)); w.setOpaque(false);
        JLabel l = new JLabel(title); l.setFont(new Font("SansSerif", Font.BOLD, 11)); l.setForeground(TEXT_MUTED);
        w.add(l, "North"); w.add(c, "Center"); return w;
    }

    private JLabel createCard(JPanel p, String title, String value, Color accentColor) {
        RoundedPanel c = new RoundedPanel(20, BG_PANEL); c.setLayout(new BorderLayout());
        c.setBorder(new EmptyBorder(20, 25, 20, 25));
        JLabel lt = new JLabel(title); lt.setFont(FONT_BOLD); lt.setForeground(TEXT_MUTED);
        JLabel lv = new JLabel(value); lv.setFont(FONT_NUMBERS); lv.setForeground(TEXT_MAIN);
        JPanel line = new JPanel(); line.setBackground(accentColor); line.setPreferredSize(new Dimension(4, 0));
        JPanel content = new JPanel(new GridLayout(2, 1, 0, 5)); content.setOpaque(false); content.setBorder(new EmptyBorder(0, 15, 0, 0));
        content.add(lt); content.add(lv); c.add(line, "West"); c.add(content, "Center"); p.add(c); return lv;
    }

    private void setupGlobalUI() {
        UIManager.put("OptionPane.background", BG_PANEL); UIManager.put("Panel.background", BG_PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT_MAIN);
    }

    // --- ВНУТРЕННИЕ КЛАССЫ (ТОЛЬКО ПО ОДНОМУ ЭКЗЕМПЛЯРУ) ---

    class BarChartCellRenderer extends JPanel implements TableCellRenderer {
        private BigDecimal mx; private JLabel label; private BigDecimal currentAmt; private boolean isSelected;
        public BarChartCellRenderer(BigDecimal m) {
            mx = m; setLayout(new BorderLayout()); label = new JLabel();
            label.setFont(FONT_NUMBERS.deriveFont(16f)); label.setBorder(new EmptyBorder(0, 15, 0, 0));
            add(label, "West"); setOpaque(true);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            currentAmt = (v instanceof BigDecimal) ? (BigDecimal) v : BigDecimal.ZERO; 
            label.setText(String.format("%,.2f", currentAmt));
            isSelected = s; setBackground(s ? BG_ELEVATED : BG_PANEL);
            label.setForeground(s ? ACCENT : TEXT_MAIN); return this;
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (mx != null && mx.compareTo(BigDecimal.ZERO) > 0 && currentAmt != null) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(14, 210, 170, isSelected ? 80 : 40));
                int barWidth = (int) (getWidth() * (currentAmt.doubleValue() / mx.doubleValue()));
                g2.fillRoundRect(5, 6, Math.max(barWidth - 10, 5), getHeight() - 12, 8, 8); g2.dispose();
            }
        }
    }

    class RoundedPanel extends JPanel {
        private int r; public RoundedPanel(int r, Color c) { this.r = r; setOpaque(false); setBackground(c); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground()); g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), r, r));
            g2.setColor(BORDER_COLOR); g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, r, r)); g2.dispose();
        }
    }

    class CustomButton extends JButton {
        public CustomButton(String t, Color b, Color f) {
            super(t); setFont(FONT_BOLD); setForeground(f); setBackground(b); setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(ACCENT_HOVER); }
                public void mouseExited(MouseEvent e) { setBackground(b); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            super.paintComponent(g); g2.dispose();
        }
    }

    class ActionButton extends JButton {
        private Color hov;
        public ActionButton(String t, Color h, Color bg) {
            super(t); this.hov = h; setFont(FONT_BOLD); setForeground(TEXT_MAIN); setBackground(bg);
            setContentAreaFilled(false); setBorder(new EmptyBorder(10, 15, 10, 15)); setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setForeground(hov); }
                public void mouseExited(MouseEvent e) { setForeground(TEXT_MAIN); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            if (getModel().isRollover()) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setColor(BORDER_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    class ModernScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { this.thumbColor = BORDER_COLOR; this.trackColor = BG_PANEL; }
        @Override protected JButton createDecreaseButton(int o) { return new JButton() { {setPreferredSize(new Dimension(0,0));} }; }
        @Override protected JButton createIncreaseButton(int o) { return new JButton() { {setPreferredSize(new Dimension(0,0));} }; }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle b) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isDragging ? ACCENT : thumbColor); g2.fillRoundRect(b.x + 2, b.y + 2, b.width - 4, b.height - 4, 8, 8); g2.dispose();
        }
    }

    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Драйвер SQLite не найден!");
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new MainGui().setVisible(true));
    }
}