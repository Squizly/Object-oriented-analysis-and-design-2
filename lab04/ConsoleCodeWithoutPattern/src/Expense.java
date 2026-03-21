public class Expense {
    private String category;
    private Money money;
    private String date;

    public Expense(String category, Money money, String date) {
        this.category = category;
        this.money = money;
        this.date = date;
    }
    public Money getMoney() { return money; }
    public String getCategory() { return category; }
    public String getDate() { return date; }
}