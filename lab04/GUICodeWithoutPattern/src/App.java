import java.util.*;

public class App {
    public static void main(String[] args) {
        CurrencyService service = new CurrencyService();
        List<Expense> list = service.getExpenses();

        System.out.println("--- ПЯТЬ ТРАТ (ИСХОДНЫЕ ДАННЫЕ) ---");
        for (int i = 0; i < 5; i++) {
            System.out.println(i + ": " + list.get(i).getCategory() + " | " + list.get(i).getMoney());
        }

        System.out.println("\n--- КОНВЕРТИРУЕМ ТРАТУ №0 В RUB ---");
        Expense firstExpense = list.get(0);
        service.convertInPlace(firstExpense.getMoney(), "RUB");
        System.out.println("Результат конвертации: " + firstExpense.getMoney());

        System.out.println("\n--- ПЯТЬ ТРАТ (ИСХОДНЫЕ ДАННЫЕ) ---");
        for (int i = 0; i < 5; i++) {
            System.out.println(i + ": " + list.get(i).getCategory() + " | " + list.get(i).getMoney());
        }
        
        System.out.println("\nДанные безвозвратно изменились. В этом и есть главная 'подстава' мутабельности.");
    }
}