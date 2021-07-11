package info.kgeorgiy.ja.fadeev.statistic;

import java.util.*;
import java.util.function.Function;

public class Statistic<T, F> {
    private final StatisticType statisticType;
    private final int numberOfElements;
    private final int numberOfUniqueElements;
    private final T minValue;
    private final T maxValue;
    private final int minLength;
    private final int maxLength;
    private final T minLengthValue;
    private final T maxLengthValue;
    private final F averageValue;

    public Statistic(StatisticType type, List<T> data, Comparator<T> comparator, Function<List<T>, F> getAverageValue) {
        this(type, data, comparator, getAverageValue, null);
    }

    public Statistic(StatisticType type, List<T> data, Comparator<T> comparator, Function<List<T>, F> getAverageValue, Function<T, Integer> getLength) {
        numberOfElements = data.size();
        statisticType = type;
        if (numberOfElements != 0) {
            Set<T> set = new HashSet<>(data);
            numberOfUniqueElements = set.size();
            minValue = data.stream().min(comparator).orElse(null);
            maxValue = data.stream().max(comparator).orElse(null);
            averageValue = getAverageValue.apply(data);
            if (getLength != null) {
                minLengthValue = data.stream().min(Comparator.comparing(getLength)).orElse(null);
                maxLengthValue = data.stream().max(Comparator.comparing(getLength)).orElse(null);
                minLength = getLength.apply(minLengthValue);
                maxLength = getLength.apply(maxLengthValue);
            } else {
                minLengthValue = maxLengthValue = null;
                minLength = maxLength = 0;
            }
        } else {
            minValue = maxValue = minLengthValue = maxLengthValue = null;
            averageValue = null;
            numberOfUniqueElements = minLength = maxLength = 0;
        }

    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public int getNumberOfUniqueElements() {
        return numberOfUniqueElements;
    }

    public T getMinValue() {
        return minValue;
    }

    public T getMaxValue() {
        return maxValue;
    }

    public StatisticType getStatisticType() {
        return statisticType;
    }

    public T getMinLengthValue() {
        return minLengthValue;
    }

    public T getMaxLengthValue() {
        return maxLengthValue;
    }

    public int getMinLength() {
        return minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public F getAverageValue() {
        return averageValue;
    }
}
