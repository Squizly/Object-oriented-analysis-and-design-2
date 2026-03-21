import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) {
        CurrencyService service = new CurrencyService();
        String dbUrl = "jdbc:sqlite:finance.db";

        System.out.println("\n--- 2. ПЕРВЫЕ 10 ТРАТ ИЗ БАЗЫ ДАННЫХ ---");
        printRawQuery(dbUrl, "SELECT * FROM expenses LIMIT 10");

        System.out.println("\n--- 3. ПЕРВЫЕ 5 КУРСОВ В ОБМЕННИКЕ ---");
        printRawQuery(dbUrl, "SELECT * FROM exchange_rates LIMIT 5");

        System.out.println("\n--- 4. ТОП-5 КАТЕГОРИЙ ТРАТ (ПО УБЫВАНИЮ) ---");
        List<Map.Entry<String, Money>> topCategories = getTopCategories(service);
        topCategories.forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()));

        if (!topCategories.isEmpty()) {
            String topCat = topCategories.get(0).getKey();
            System.out.println("\n--- 5. ДОБАВЛЯЕМ 1000 RUB В КАТЕГОРИЮ: " + topCat + " ---");
            service.saveExpense(new Expense(topCat, new Money(new BigDecimal("1000.00"), "RUB"), "2026-03-21"));
        }

        System.out.println("\n--- 6. ОБНОВЛЕННЫЙ ТОП-5 ---");
        getTopCategories(service).forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()));
    }

    private static void printRawQuery(String url, String sql) {
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= cols; i++) {
                    System.out.print(rs.getString(i) + " | ");
                }
                System.out.println();
            }
        } catch (SQLException e) { System.err.println("Ошибка SQL: " + e.getMessage()); }
    }

    private static List<Map.Entry<String, Money>> getTopCategories(CurrencyService service) {
        return service.getExpenses().stream()
            .collect(Collectors.groupingBy(
                Expense::getCategory,
                Collectors.reducing(
                    new Money(BigDecimal.ZERO, "RUB"),
                    ex -> service.convert(ex.getMoney(), "RUB"),
                    (m1, m2) -> service.add(m1, m2, "RUB")
                )
            ))
            .entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().getAmount().compareTo(e1.getValue().getAmount()))
            .limit(5)
            .collect(Collectors.toList());
    }
}