import java.math.BigDecimal;
import java.math.RoundingMode;

public class Money {
    private BigDecimal amount; 
    private String currency;

    public Money(BigDecimal amount, String currency) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency.toUpperCase();
    }

    // СЕТТЕРЫ — это корень зла. Они позволяют менять объект "под капотом"
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }

    @Override
    public String toString() { return amount + " " + currency; }
}