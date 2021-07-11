package info.kgeorgiy.ja.fadeev.statistic;

import java.math.BigInteger;
import java.text.Collator;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticFactory {
    public Statistic<String, Double> getStringsStatistic(StatisticType type, List<String> data, Locale locale) {
        return new Statistic<>(type, data,
                (s1, s2) -> Collator.getInstance(locale).compare(s1, s2),
                d -> d.stream()
                        .mapToDouble(String::length)
                        .reduce(0, Double::sum) / d.size(),
                String::length
        );
    }

    public Statistic<Number, Number> getNumberStatistic(StatisticType type, List<Number> data) {
        return new Statistic<>(type, data,
                Comparator.comparing(Number::doubleValue),
                d -> d.stream()
                        .mapToDouble(Number::doubleValue)
                        .reduce(0, Double::sum) / d.size()
        );
    }

    public Statistic<Date, Date> getDateStatistics(StatisticType type, List<Date> data) {
        return new Statistic<>(type, data,
                Comparator.comparing(Date::getTime),
                d -> new Date(d.stream().map(date ->
                        BigInteger.valueOf(date.getTime()))
                        .reduce(BigInteger.ZERO, BigInteger::add)
                        .divide(BigInteger.valueOf(d.size())).longValue())
        );
    }
}
