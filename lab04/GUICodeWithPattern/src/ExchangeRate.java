public class ExchangeRate {
    private final String _from;
    private final String _to;
    private final double _rate;

    public ExchangeRate(String from, String to, double rate) {
        this._from = from.toUpperCase();
        this._to = to.toUpperCase();
        this._rate = rate;
    }

    public String getFrom() { return _from; }
    public String getTo() { return _to; }
    public double getRate() { return _rate; }
}