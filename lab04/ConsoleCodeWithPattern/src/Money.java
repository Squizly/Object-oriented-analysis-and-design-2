import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Money {
    private final BigDecimal _amount;
    private final String _currency;

    public Money(BigDecimal amount, String currency) {
        if (amount == null) 
            throw new IllegalArgumentException("Сумма не может быть null");

        if (currency == null || currency.trim().length() != 3) {
            throw new IllegalArgumentException("Код валюты должен быть из 3 символов (ISO)");
        }
        
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Сумма не может быть отрицательной");
        }

        this._amount = amount.setScale(2, RoundingMode.HALF_UP);
        this._currency = currency.trim().toUpperCase();
    }

    public BigDecimal getAmount() { return _amount; }
    public String getCurrency() { return _currency; }

    @Override
    public boolean equals(Object o) {
        if (this == o) 
            return true;

        if (o == null || getClass() != o.getClass()) 
            return false;

        Money money = (Money) o;
        return _amount.compareTo(money._amount) == 0 && Objects.equals(_currency, money._currency);
    }

    @Override
    public String toString() {
        return _amount + " " + _currency;
    }
}