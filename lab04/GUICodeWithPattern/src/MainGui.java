import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.List;

public class MainGui extends JFrame {
    private CurrencyService service;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Expense> currentViewMap = new ArrayList<>();

    // --- СОВРЕМЕННАЯ ПАЛИТРА ---
    private final Color BG_APP = new Color(15, 15, 18);        // Очень темный фон приложения
    private final Color BG_PANEL = new Color(24, 24, 28);      // Фон карточек и панелей
    private final Color BG_ELEVATED = new Color(32, 32, 38);   // Фон для выделенных элементов
    private final Color ACCENT = new Color(14, 210, 170);      // Основной акцент (Мятный) - ИСПОЛЬЗУЕТСЯ ДЛЯ ТЕКСТА ФИЛЬТРОВ
    private final Color ACCENT_HOVER = new Color(10, 180, 145);
    private final Color TEXT_MAIN = new Color(245, 245, 250);
    private final Color TEXT_MUTED = new Color(140, 140, 150);
    private final Color BORDER_COLOR = new Color(45, 45, 55);
    
    // Акценты для кнопок
    private final Color COLOR_DANGER = new Color(239, 68, 68);
    private final Color COLOR_WARNING = new Color(245, 158, 11);
    private final Color COLOR_INFO = new Color(59, 130, 246);

    // Универсальные красивые шрифты без привязки к ОС
    private final Font FONT_MAIN = new Font("SansSerif", Font.PLAIN, 14);
    private final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 14);
    private final Font FONT_H1 = new Font("SansSerif", Font.BOLD, 26);
    private final Font FONT_NUMBERS = new Font("Monospaced", Font.BOLD, 22);

    private final String DB_URL = "jdbc:sqlite:finance.db";

    private JComboBox<String> currencyCombo, categoryCombo, monthCombo, yearCombo;
    private JLabel totalSumLabel, countLabel, maxExpenseLabel;

    public MainGui() {
        setupGlobalUI(); 
        service = new CurrencyService();

        setTitle("Finance Data Engine Pro :: [Value Object & CRUD]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 850);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_APP);

        JPanel mainPanel = new JPanel(new BorderLayout(25, 25));
        mainPanel.setBackground(BG_APP);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // --- 1. ШАПКА ---
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

        btnAdd.addActionListener(e -> showExpenseDialog(null));
        btnEdit.addActionListener(e -> editSelectedExpense());
        btnDel.addActionListener(e -> deleteSelectedExpense());
        btnRates.addActionListener(e -> showRatesDialog());

        actionBtnPanel.add(btnRates);
        actionBtnPanel.add(btnDel);
        actionBtnPanel.add(btnEdit);
        actionBtnPanel.add(btnAdd);
        headerPanel.add(actionBtnPanel, BorderLayout.EAST);

        // --- 2. ВЕРХНИЙ БЛОК (КАРТОЧКИ И ФИЛЬТРЫ) ---
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
        categoryCombo = createStyledCombo(new String[]{});
        updateCategoryCombo();
        monthCombo = createStyledCombo(new String[]{"Все", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"});
        monthCombo.setSelectedItem("03");
        yearCombo = createStyledCombo(new String[]{"Все", "2024", "2025", "2026"});
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

        JPanel middleWrapper = new JPanel(new BorderLayout(0, 20));
        middleWrapper.setOpaque(false);
        middleWrapper.add(cardsPanel, BorderLayout.NORTH);
        middleWrapper.add(filtersPanel, BorderLayout.CENTER);
        
        topWrapper.add(middleWrapper, BorderLayout.CENTER);
        mainPanel.add(topWrapper, BorderLayout.NORTH);

        // --- 3. ТАБЛИЦА С ДАННЫМИ ---
        String[] columns = {"КАТЕГОРИЯ", "СУММА", "ВАЛЮТА", "ДАТА"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return c == 1 ? BigDecimal.class : String.class; }
        };
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        setupTable(table, true);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(BG_PANEL);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI()); 
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());

        JPanel tableWrapper = new RoundedPanel(15, BG_PANEL);
        tableWrapper.setLayout(new BorderLayout());
        tableWrapper.setBorder(new EmptyBorder(1,1,1,1));
        tableWrapper.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(tableWrapper, BorderLayout.CENTER);
        add(mainPanel);
        updateData();
    }

    private void updateData() {
        if (categoryCombo == null || tableModel == null) return;
        tableModel.setRowCount(0);
        currentViewMap.clear();
        String targetCur = (String) currencyCombo.getSelectedItem();
        String targetCat = (String) categoryCombo.getSelectedItem();
        if (targetCat == null) targetCat = "Все";
        String targetMonth = (String) monthCombo.getSelectedItem();
        String targetYear = (String) yearCombo.getSelectedItem();
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal maxExp = BigDecimal.ZERO;

        for (Expense ex : service.getExpenses()) {
            if (!targetYear.equals("Все") && !ex.getDate().startsWith(targetYear)) continue;
            if (!targetMonth.equals("Все") && !ex.getDate().contains("-" + targetMonth + "-")) continue;
            if (!targetCat.equals("Все") && !ex.getCategory().equals(targetCat)) continue;
            Money m;
            if (!targetCur.equals("Оригинал")) {
                m = service.convert(ex.getMoney(), targetCur);
                total = total.add(m.getAmount());
                if (m.getAmount().compareTo(maxExp) > 0) maxExp = m.getAmount();
            } else { m = ex.getMoney(); }
            tableModel.addRow(new Object[]{ ex.getCategory().toUpperCase(), m.getAmount(), m.getCurrency(), ex.getDate() });
            currentViewMap.add(ex);
        }
        countLabel.setText(String.valueOf(currentViewMap.size()));
        if (!targetCur.equals("Оригинал")) {
            totalSumLabel.setText(String.format("%,.2f %s", total, targetCur));
            maxExpenseLabel.setText(String.format("%,.2f %s", maxExp, targetCur));
            table.getColumnModel().getColumn(1).setCellRenderer(new BarChartCellRenderer(maxExp));
        } else {
            totalSumLabel.setText("СМЕШАННЫЕ ДАННЫЕ"); maxExpenseLabel.setText("---");
            table.getColumnModel().getColumn(1).setCellRenderer(new BarChartCellRenderer(BigDecimal.ZERO));
        }
    }

    private void editSelectedExpense() {
        int r = table.getSelectedRow(); if (r < 0) return;
        showExpenseDialog(currentViewMap.get(table.convertRowIndexToModel(r)));
    }

    private void deleteSelectedExpense() {
        int r = table.getSelectedRow(); if (r < 0) return;
        Expense ex = currentViewMap.get(table.convertRowIndexToModel(r));
        if (JOptionPane.showConfirmDialog(this, "Удалить: " + ex.getCategory() + "?", "Подтверждение", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                String sql = "DELETE FROM expenses WHERE rowid IN (SELECT rowid FROM expenses WHERE category=? AND amount=? AND currency=? AND date=? LIMIT 1)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, ex.getCategory()); ps.setDouble(2, ex.getMoney().getAmount().doubleValue());
                ps.setString(3, ex.getMoney().getCurrency()); ps.setString(4, ex.getDate());
                ps.executeUpdate();
                service.loadExpenses(); updateCategoryCombo(); updateData();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void showExpenseDialog(Expense old) {
        JDialog d = new JDialog(this, old == null ? "Новая трата" : "Изменить трату", true);
        d.setSize(420, 480);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(BG_PANEL);
        d.setLayout(new GridBagLayout());
        
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(12, 20, 12, 20);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;

        JTextField fCat = createStyledTextField(old != null ? old.getCategory() : "");
        JTextField fAmt = createStyledTextField(old != null ? old.getMoney().getAmount().toString() : "");
        JTextField fCur = createStyledTextField(old != null ? old.getMoney().getCurrency() : "RUB");
        JTextField fDate = createStyledTextField(old != null ? old.getDate() : "2026-03-21");

        addDialogField(d, "Название категории:", fCat, g, 0);
        addDialogField(d, "Сумма операции:", fAmt, g, 1);
        addDialogField(d, "Валюта (RUB, USD...):", fCur, g, 2);
        addDialogField(d, "Дата (ГГГГ-ММ-ДД):", fDate, g, 3);

        JButton b = new CustomButton("СОХРАНИТЬ", ACCENT, BG_APP);
        b.setPreferredSize(new Dimension(200, 45));
        g.gridy = 4; g.gridwidth = 2; g.insets = new Insets(30, 20, 20, 20);
        d.add(b, g);

        b.addActionListener(e -> {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                if (old == null) {
                    PreparedStatement ps = conn.prepareStatement("INSERT INTO expenses (category, amount, currency, date) VALUES (?, ?, ?, ?)");
                    ps.setString(1, fCat.getText()); ps.setDouble(2, Double.parseDouble(fAmt.getText().replace(",",".")));
                    ps.setString(3, fCur.getText().toUpperCase()); ps.setString(4, fDate.getText());
                    ps.executeUpdate();
                } else {
                    String sql = "UPDATE expenses SET category=?, amount=?, currency=?, date=? WHERE rowid IN (SELECT rowid FROM expenses WHERE category=? AND amount=? AND currency=? AND date=? LIMIT 1)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, fCat.getText()); ps.setDouble(2, Double.parseDouble(fAmt.getText().replace(",",".")));
                    ps.setString(3, fCur.getText().toUpperCase()); ps.setString(4, fDate.getText());
                    ps.setString(5, old.getCategory()); ps.setDouble(6, old.getMoney().getAmount().doubleValue());
                    ps.setString(7, old.getMoney().getCurrency()); ps.setString(8, old.getDate());
                    ps.executeUpdate();
                }
                service.loadExpenses(); updateCategoryCombo(); updateData(); d.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(d, "Ошибка данных: " + ex.getMessage()); }
        });
        d.setVisible(true);
    }

    private void showRatesDialog() {
        JDialog d = new JDialog(this, "Курсы Валют", true);
        d.setSize(500, 450);
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(BG_PANEL);
        d.setLayout(new BorderLayout(15, 15));
        
        String[] displayCols = {"Из валюты", "В валюту", "Курс", "ID"};
        DefaultTableModel m = new DefaultTableModel(displayCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 2; }
        };
        try (Connection conn = DriverManager.getConnection(DB_URL);
             ResultSet rs = conn.createStatement().executeQuery("SELECT id, from_cur, to_cur, rate FROM exchange_rates")) {
            while (rs.next()) m.addRow(new Object[]{rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getInt(1)});
        } catch (SQLException e) { e.printStackTrace(); }
        
        JTable t = new JTable(m);
        t.getColumnModel().getColumn(3).setMinWidth(0); t.getColumnModel().getColumn(3).setMaxWidth(0);
        setupTable(t, false);

        JScrollPane scroll = new JScrollPane(t);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(15, 15, 0, 15));
        wrapper.setOpaque(false);
        wrapper.add(scroll, BorderLayout.CENTER);

        JButton b = new CustomButton("СОХРАНИТЬ ИЗМЕНЕНИЯ", ACCENT, BG_APP);
        b.setPreferredSize(new Dimension(0, 45));
        JPanel btnWrapper = new JPanel(new BorderLayout());
        btnWrapper.setOpaque(false);
        btnWrapper.setBorder(new EmptyBorder(10, 15, 15, 15));
        btnWrapper.add(b, BorderLayout.CENTER);

        b.addActionListener(e -> {
            if (t.isEditing()) t.getCellEditor().stopCellEditing();
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                PreparedStatement ps = conn.prepareStatement("UPDATE exchange_rates SET rate=? WHERE id=?");
                for (int i = 0; i < m.getRowCount(); i++) {
                    ps.setDouble(1, Double.parseDouble(m.getValueAt(i, 2).toString().replace(",",".")));
                    ps.setInt(2, (Integer) m.getValueAt(i, 3)); ps.executeUpdate();
                }
                service.loadRates(); updateData(); d.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(d, "Ошибка курсов! Проверьте формат чисел."); }
        });
        
        d.add(wrapper, BorderLayout.CENTER);
        d.add(btnWrapper, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void setupTable(JTable t, boolean isMain) {
        t.setBackground(BG_PANEL);
        t.setForeground(TEXT_MAIN);
        t.setFont(FONT_MAIN);
        t.setRowHeight(isMain ? 48 : 35);
        t.setGridColor(BORDER_COLOR);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setSelectionBackground(BG_ELEVATED);
        t.setSelectionForeground(ACCENT);
        t.setIntercellSpacing(new Dimension(0, 0));
        
        JTableHeader h = t.getTableHeader();
        h.setBackground(BG_APP);
        h.setForeground(TEXT_MUTED);
        h.setFont(FONT_BOLD);
        h.setPreferredSize(new Dimension(0, isMain ? 50 : 40));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        
        // Кастомный рендерер заголовка (жесткие цвета)
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(BG_APP);
                setForeground(TEXT_MUTED);
                setBorder(new EmptyBorder(0, 15, 0, 0));
                return this;
            }
        };
        for (int i = 0; i < t.getColumnModel().getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Рендерер для обычных ячеек (жесткие цвета)
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(new EmptyBorder(0, 15, 0, 0));
                if (isSelected) {
                    setBackground(BG_ELEVATED);
                    setForeground(ACCENT);
                } else {
                    setBackground(BG_PANEL);
                    setForeground(TEXT_MAIN);
                }
                return this;
            }
        };

        if (isMain) {
            t.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
            t.getColumnModel().getColumn(2).setCellRenderer(cellRenderer);
            t.getColumnModel().getColumn(3).setCellRenderer(cellRenderer);
        } else {
            for (int i = 0; i < t.getColumnModel().getColumnCount(); i++) {
                t.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
            }
        }
    }

    private void updateCategoryCombo() {
        if (categoryCombo == null) return;
        String cur = (String) categoryCombo.getSelectedItem();
        categoryCombo.removeAllItems(); categoryCombo.addItem("Все");
        service.getExpenses().stream().map(Expense::getCategory).distinct().sorted().forEach(categoryCombo::addItem);
        if (cur != null) categoryCombo.setSelectedItem(cur);
    }

    private void addDialogField(JDialog d, String l, JTextField f, GridBagConstraints g, int y) {
        g.gridy = y; g.gridx = 0; g.gridwidth = 2; 
        JLabel lbl = new JLabel(l); 
        lbl.setForeground(TEXT_MUTED);
        lbl.setFont(FONT_BOLD);
        d.add(lbl, g);
        
        g.gridy = y + 1; 
        g.insets = new Insets(0, 20, 15, 20);
        d.add(f, g);
        g.insets = new Insets(5, 20, 5, 20); 
    }

    private JPanel createFilterWrapper(String title, JComponent c) {
        JPanel w = new JPanel(new BorderLayout(0, 6));
        w.setOpaque(false);
        JLabel l = new JLabel(title);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(TEXT_MUTED);
        w.add(l, BorderLayout.NORTH);
        w.add(c, BorderLayout.CENTER);
        return w;
    }

    private JLabel createCard(JPanel p, String title, String value, Color accentColor) {
        RoundedPanel c = new RoundedPanel(20, BG_PANEL);
        c.setLayout(new BorderLayout());
        c.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        JLabel lt = new JLabel(title);
        lt.setFont(FONT_BOLD);
        lt.setForeground(TEXT_MUTED);
        
        JLabel lv = new JLabel(value);
        lv.setFont(FONT_NUMBERS);
        lv.setForeground(TEXT_MAIN);
        
        JPanel line = new JPanel();
        line.setBackground(accentColor);
        line.setPreferredSize(new Dimension(4, 0));
        
        JPanel content = new JPanel(new GridLayout(2, 1, 0, 5));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 15, 0, 0));
        content.add(lt);
        content.add(lv);
        
        c.add(line, BorderLayout.WEST);
        c.add(content, BorderLayout.CENTER);
        p.add(c);
        return lv;
    }

    // --- ПОЛНОСТЬЮ ПЕРЕПИСАННЫЙ КОМБОБОКС (РЕЖИМ, КАТЕГОРИЯ И ТД) ---
    private JComboBox<String> createStyledCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setPreferredSize(new Dimension(140, 40));
        c.setFont(FONT_BOLD);
        c.setBackground(BG_APP);
        c.setForeground(ACCENT); // Делаем текст фильтров Мятным для 100% видимости
        c.setOpaque(true);

        // Убиваем влияние Windows/OS темы
        c.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("▼");
                btn.setFont(new Font("SansSerif", Font.PLAIN, 10));
                btn.setBackground(BG_APP);
                btn.setForeground(TEXT_MUTED);
                btn.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                btn.setFocusPainted(false);
                btn.setContentAreaFilled(false);
                btn.setOpaque(true);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return btn;
            }
            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(BG_APP);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });

        // Кастомный рендерер для выпадающего списка
        c.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setBorder(new EmptyBorder(8, 10, 8, 10));
                if (isSelected && index >= 0) { // Если навели мышкой
                    l.setBackground(ACCENT);
                    l.setForeground(BG_APP);
                } else { // Обычное состояние
                    l.setBackground(BG_PANEL);
                    l.setForeground(ACCENT); 
                }
                return l;
            }
        });
        return c;
    }

    private JTextField createStyledTextField(String text) {
        JTextField f = new JTextField(text);
        f.setPreferredSize(new Dimension(0, 40));
        f.setBackground(BG_APP);
        f.setForeground(ACCENT); // Вводимый текст тоже мятный
        f.setCaretColor(ACCENT);
        f.setFont(FONT_MAIN);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(0, 10, 0, 10)
        ));
        return f;
    }

    private void setupGlobalUI() {
        UIManager.put("OptionPane.background", BG_PANEL);
        UIManager.put("Panel.background", BG_PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT_MAIN);
    }

    // --- КАСТОМНЫЕ КОМПОНЕНТЫ ---

    class BarChartCellRenderer extends JPanel implements TableCellRenderer {
        private BigDecimal mx;
        private JLabel label;
        private BigDecimal currentAmt;
        private boolean isSelected;

        public BarChartCellRenderer(BigDecimal m) {
            mx = m;
            setLayout(new BorderLayout());
            label = new JLabel();
            label.setFont(FONT_NUMBERS.deriveFont(16f));
            label.setBorder(new EmptyBorder(0, 15, 0, 0));
            add(label, BorderLayout.WEST);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            currentAmt = (BigDecimal) v;
            label.setText(String.format("%,.2f", currentAmt));
            isSelected = s;
            
            if (s) {
                setBackground(BG_ELEVATED);
                label.setForeground(ACCENT);
            } else {
                setBackground(BG_PANEL);
                label.setForeground(TEXT_MAIN);
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); 
            if (mx != null && mx.compareTo(BigDecimal.ZERO) > 0 && currentAmt != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(14, 210, 170, isSelected ? 80 : 40)); 
                int barWidth = (int) (getWidth() * (currentAmt.doubleValue() / mx.doubleValue()));
                if (barWidth < 5) barWidth = 5;
                g2.fillRoundRect(5, 6, barWidth - 10, getHeight() - 12, 8, 8);
                g2.dispose();
            }
        }
    }

    class RoundedPanel extends JPanel {
        private int r;
        public RoundedPanel(int r, Color c) { 
            this.r = r; 
            setOpaque(false); 
            setBackground(c); 
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground()); 
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), r, r));
            g2.setColor(BORDER_COLOR); 
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, r, r));
            g2.dispose();
        }
    }

    class CustomButton extends JButton {
        private Color bgColor, fgColor;
        public CustomButton(String t, Color b, Color f) {
            super(t); 
            this.bgColor = b; 
            this.fgColor = f;
            setFont(FONT_BOLD); 
            setForeground(f); 
            setBackground(b);
            setFocusPainted(false); 
            setBorderPainted(false); 
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(ACCENT_HOVER); }
                public void mouseExited(MouseEvent e) { setBackground(bgColor); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    class ActionButton extends JButton {
        private Color hoverColor;
        public ActionButton(String t, Color hover, Color bg) {
            super(t); 
            this.hoverColor = hover;
            setFont(FONT_BOLD);
            setForeground(TEXT_MAIN); 
            setBackground(bg); 
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(10, 15, 10, 15));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setForeground(hoverColor); }
                public void mouseExited(MouseEvent e) { setForeground(TEXT_MAIN); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isRollover()) {
                g2.setColor(BORDER_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
            super.paintComponent(g);
            g2.dispose();
        }
    }

    class ModernScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            this.thumbColor = BORDER_COLOR;
            this.trackColor = BG_PANEL;
        }
        @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        private JButton createZeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isDragging ? ACCENT : thumbColor);
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
            g2.dispose();
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGui().setVisible(true));
    }
}