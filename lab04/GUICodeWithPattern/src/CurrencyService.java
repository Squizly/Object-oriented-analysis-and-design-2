import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CurrencyService {
    private final String dbUrl = "jdbc:sqlite:finance.db";
    private List<ExchangeRate> rates = new ArrayList<>();
    private List<Expense> expenses = new ArrayList<>();

    public CurrencyService() {
        try {
            Class.forName("org.sqlite.JDBC"); // ФОРСИРУЕМ ЗАГРУЗКУ ДРАЙВЕРА
        } catch (ClassNotFoundException e) {
            System.err.println("Драйвер не найден в classpath: " + e.getMessage());
        }
        loadRates();
        loadExpenses();
    }

    public void loadRates() {
        this.rates.clear();
        try (Connection conn = DriverManager.getConnection(dbUrl);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM exchange_rates")) {
            
            while (rs.next()) {
                this.rates.add(new ExchangeRate(
                    rs.getString("from_cur"),
                    rs.getString("to_cur"),
                    rs.getDouble("rate")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки курсов: " + e.getMessage());
        }
    }

    public void loadExpenses() {
        this.expenses.clear();
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM expenses")) {
            while (rs.next()) {
                BigDecimal amount = BigDecimal.valueOf(rs.getDouble("amount"));
                Money m = new Money(amount, rs.getString("currency"));
                this.expenses.add(new Expense(rs.getString("category"), m, rs.getString("date")));
            }
        } catch (SQLException e) { System.err.println("Ошибка загрузки трат: " + e.getMessage()); }
    }

    public void saveExpense(Expense ex) {
        String sql = "INSERT INTO expenses (category, amount, currency, date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ex.getCategory());
            pstmt.setDouble(2, ex.getMoney().getAmount().doubleValue());
            pstmt.setString(3, ex.getMoney().getCurrency());
            pstmt.setString(4, ex.getDate());
            pstmt.executeUpdate();
            
            this.expenses.add(ex);
            System.out.println("Запись сохранена в БД и кэш.");
        } catch (SQLException e) { System.err.println("Ошибка БД: " + e.getMessage()); }
    }

    public double getRate(String from, String to) {
        if (from.equalsIgnoreCase(to)) return 1.0;
        return rates.stream()
            .filter(r -> r.getFrom().equals(from.toUpperCase()) && r.getTo().equals(to.toUpperCase()))
            .map(ExchangeRate::getRate).findFirst().orElse(1.0);
    }

    public List<Expense> getExpenses() {
        return this.expenses;
    }

    public Money convert(Money m, String target) {
        double rate = getRate(m.getCurrency(), target);
        return new Money(m.getAmount().multiply(BigDecimal.valueOf(rate)), target);
    }

    public Money add(Money m1, Money m2, String target) {
        Money c1 = convert(m1, target);
        Money c2 = convert(m2, target);
        return new Money(c1.getAmount().add(c2.getAmount()), target);
    }
}