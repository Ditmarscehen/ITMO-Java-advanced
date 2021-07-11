package info.kgeorgiy.ja.fadeev.statistic;

public enum StatisticType {
    SENTENCE("Sentence", "Sentences"),
    WORD("Word", "Words"),
    NUMBER("Number", "Numbers"),
    MONEY("Money", "Money"),
    DATE("Date", "Dates");

    private final String single;
    private final String plural;

    StatisticType(String single, String plural) {
        this.single = single;
        this.plural = plural;
    }
    public String getFormat(){
        return "format" + single;
    }

    public String getStat() {
        return "stat" + plural;
    }

    public String getNum() {
        return "numOf" + plural;
    }

    public String getMin() {
        return "min" + single;
    }

    public String getMax() {
        return "max" + single;
    }

    public String getMinLength() {
        return "minLength" + single;
    }

    public String getMaxLength() {
        return "maxLength" + single;
    }

    public String getAverage() {
        if (this == WORD || this == SENTENCE) {
            return "averageLength" + single;
        }
        return "average" + single;
    }

}
