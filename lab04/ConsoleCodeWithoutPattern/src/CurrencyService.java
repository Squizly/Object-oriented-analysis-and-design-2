import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class CurrencyService {
    private final String dbUrl = "jdbc:sqlite:finance.db";
    private List<ExchangeRate> rates = new ArrayList<>();
    private List<Expense> expenses = new ArrayList<>();

    public CurrencyService() {
        try { Class.forName("org.sqlite.JDBC"); } catch (Exception e) {}
        loadRates();
        loadExpenses();
    }

    public void loadRates() {
        rates.clear();
        try (Connection conn = DriverManager.getConnection(dbUrl);
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM exchange_rates")) {
            while (rs.next()) {
                rates.add(new ExchangeRate(rs.getString("from_cur"), rs.getString("to_cur"), rs.getDouble("rate")));
            }
        } catch (SQLException e) { System.err.println("Ошибка курсов: " + e.getMessage()); }
    }

    public void loadExpenses() {
        expenses.clear();
        try (Connection conn = DriverManager.getConnection(dbUrl);
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM expenses")) {
            while (rs.next()) {
                BigDecimal amt = BigDecimal.valueOf(rs.getDouble("amount"));
                expenses.add(new Expense(rs.getString("category"), new Money(amt, rs.getString("currency")), rs.getString("date")));
            }
        } catch (SQLException e) { System.err.println("Ошибка трат: " + e.getMessage()); }
    }

    public void convertInPlace(Money m, String target) {
        if (m.getCurrency().equalsIgnoreCase(target)) return;
        
        double rate = 1.0;
        boolean found = false;
        for (ExchangeRate r : rates) {
            if (r.getFrom().equalsIgnoreCase(m.getCurrency()) && r.getTo().equalsIgnoreCase(target)) {
                rate = r.getRate();
                found = true;
                break;
            }
        }
        
        BigDecimal newAmount = m.getAmount().multiply(BigDecimal.valueOf(rate));
        m.setAmount(newAmount.setScale(2, java.math.RoundingMode.HALF_UP));
        m.setCurrency(target.toUpperCase());
        
        if (!found && !m.getCurrency().equals(target)) {
            System.err.println("Курс не найден для " + m.getCurrency() + " -> " + target);
        }
    }

    public void addInPlace(Money base, Money addition, String target) {
        convertInPlace(base, target);
        
        Money tempAdd = new Money(addition.getAmount(), addition.getCurrency());
        convertInPlace(tempAdd, target);
        
        // Складываем результат в первый объект
        base.setAmount(base.getAmount().add(tempAdd.getAmount()));
    }

    public List<Expense> getExpenses() { return expenses; }
}