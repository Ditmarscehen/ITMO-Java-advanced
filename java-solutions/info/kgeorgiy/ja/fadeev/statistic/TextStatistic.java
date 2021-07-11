package info.kgeorgiy.ja.fadeev.statistic;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.text.*;
import java.util.*;
import java.util.function.Function;


public class TextStatistic {
    private static String getWord(final int begin, final int end, final Locale inputLocale, final String text) {
        final String str = text.substring(begin, end);
        if(!Character.isLetter(str.charAt(0))){
            return null;
        }
        return str.toLowerCase(inputLocale);
    }

    private static Number getNumber(Function<Locale, NumberFormat> formatF, final ParsePosition position, final Locale inputLocale, final String text) {
        final NumberFormat format = formatF.apply(inputLocale);
        return format.parse(text, position);
    }

    private static Date getDate(final ParsePosition position, final String text, List<DateFormat> dateFormats) {
        for (final DateFormat dateFormat : dateFormats) {
            final Date date = dateFormat.parse(text, position);
            if (date != null) {
                return date;
            }
        }
        return null;
    }

    private static List<Statistic<?, ?>> getStatistic(final Locale inputLocale, final String text) {
        final List<Number> numberData = new ArrayList<>();
        final List<Number> moneyData = new ArrayList<>();
        final List<Date> dateData = new ArrayList<>();
        final List<String> wordData = new ArrayList<>();
        final List<DateFormat> dateFormats = List.of(
                DateFormat.getDateInstance(DateFormat.FULL, inputLocale),
                DateFormat.getDateInstance(DateFormat.LONG, inputLocale),
                DateFormat.getDateInstance(DateFormat.MEDIUM, inputLocale),
                DateFormat.getDateInstance(DateFormat.SHORT, inputLocale)
        );
        BreakIterator breakIterator = BreakIterator.getWordInstance(inputLocale);
        breakIterator.setText(text);
        Number number;
        Number money;
        Date date;
        String word;
        for (int begin = breakIterator.first(), end = breakIterator.next(), prevPos = 0;
             end != BreakIterator.DONE;
             begin = end, end = breakIterator.next()) {
            if (begin >= prevPos) {
                final ParsePosition parsePosition = new ParsePosition(begin);
                if ((money = getNumber(NumberFormat::getCurrencyInstance, parsePosition, inputLocale, text)) != null) {
                    prevPos = parsePosition.getIndex();
                    moneyData.add(money);
                } else if ((date = getDate(parsePosition, text, dateFormats)) != null) {
                    prevPos = parsePosition.getIndex();
                    dateData.add(date);
                } else if ((number = getNumber(NumberFormat::getNumberInstance, parsePosition, inputLocale, text)) != null) {
                    numberData.add(number);
                } else if ((word = getWord(begin, end, inputLocale, text)) != null) {
                    wordData.add(word);
                }
            }
        }
        breakIterator = BreakIterator.getSentenceInstance(inputLocale);
        breakIterator.setText(text);
        final List<String> sentencesData = new ArrayList<>();
        for (int begin = breakIterator.first(), end = breakIterator.next();
             end != BreakIterator.DONE;
             begin = end, end = breakIterator.next()) {
            final String str = text.substring(begin, end).trim();
            sentencesData.add(str);
        }
        StatisticFactory statisticFactory = new StatisticFactory();
        final Statistic<String, Double> sentenceStatistic = statisticFactory.getStringsStatistic(StatisticType.SENTENCE, sentencesData, inputLocale);
        final Statistic<String, Double> wordStatistic = statisticFactory.getStringsStatistic(StatisticType.WORD, wordData, inputLocale);
        final Statistic<Number, Number> numberStatistic = statisticFactory.getNumberStatistic(StatisticType.NUMBER, numberData);
        final Statistic<Number, Number> moneyStatistic = statisticFactory.getNumberStatistic(StatisticType.MONEY, moneyData);
        final Statistic<Date, Date> dateStatistic = statisticFactory.getDateStatistics(StatisticType.DATE, dateData);
        return List.of(sentenceStatistic, wordStatistic, numberStatistic, moneyStatistic, dateStatistic);
    }

    private static void write(final List<Statistic<?, ?>> statistics, ResourceBundle bundle, final Locale inputLocale, final Locale outputLocale, final Path pathOut, final String inputFileName) throws IOException {
        try (final BufferedWriter writer = Files.newBufferedWriter(pathOut)) {
            final String statFormat = bundle.getString("statFormat");
            final String numUniqueFormat = bundle.getString("numUniqueFormat");
            final String lengthFormat = bundle.getString("lengthFormat");
            final String notAvailable = bundle.getString("notAvailable");
            final MessageFormat formatNumber = new MessageFormat(bundle.getString("formatNumber"), outputLocale);
            writer.write(bundle.getString("analyzedFile") + " " + inputFileName + "\n\n");
            writer.write(bundle.getString("summaryStat") + "\n");

            statistics.forEach(statistic -> {
                try {
                    writer.write(MessageFormat.format(statFormat, bundle.getString(statistic.getStatisticType().getNum()), statistic.getNumberOfElements()) + "\n");
                } catch (final IOException exception) {
                    exception.printStackTrace();
                }
            });
            writer.newLine();

            statistics.forEach(statistic -> {
                try {
                    StatisticType statisticType = statistic.getStatisticType();
                    Locale locale;
                    if (statisticType == StatisticType.MONEY) {
                        locale = inputLocale;
                    } else {
                        locale = outputLocale;
                    }
                    final MessageFormat format = new MessageFormat(bundle.getString(statisticType.getFormat()), locale);
                    writer.write(bundle.getString(statisticType.getStat()) + "\n");
                    writeFormat(writer, numUniqueFormat, bundle.getString(statisticType.getNum()),
                            getFormatString(formatNumber, statistic.getNumberOfElements(), 0),
                            bundle.getString("unique"),
                            getFormatString(formatNumber, statistic.getNumberOfUniqueElements(), 0));

                    writeFormat(writer, statFormat, bundle.getString(statisticType.getMin()),
                            getFormatString(format, statistic.getMinValue(), notAvailable));

                    writeFormat(writer, statFormat, bundle.getString(statisticType.getMax()),
                            getFormatString(format, statistic.getMaxValue(), notAvailable));

                    if (statisticType == StatisticType.SENTENCE || statisticType == StatisticType.WORD) {
                        writeFormat(writer, lengthFormat, bundle.getString(statisticType.getMinLength()), statistic.getMinLength(),
                                getFormatString(format, statistic.getMinLengthValue(), notAvailable));

                        writeFormat(writer, lengthFormat, bundle.getString(statisticType.getMaxLength()), statistic.getMaxLength(),
                                getFormatString(format, statistic.getMaxLengthValue(), notAvailable));

                        writeFormat(writer, statFormat, bundle.getString(statisticType.getAverage()),
                                getFormatString(formatNumber, statistic.getAverageValue(), notAvailable));
                    } else {
                        writeFormat(writer, statFormat, bundle.getString(statisticType.getAverage()),
                                getFormatString(format, statistic.getAverageValue(), notAvailable));
                    }
                    writer.newLine();

                } catch (final IOException e) {
                    printError(bundle, "IOExceptionWr", e);
                }

            });
        } catch (IOException e) {
            printError(bundle, "IOExceptionOut", e);
        }
    }

    private static void writeFormat(BufferedWriter writer, String pattern, Object... args) throws IOException {
        writer.write(MessageFormat.format(pattern, args) + "\n");
    }

    private static String getFormatString(final MessageFormat messageFormat, final Object o, final Object defValue) {
        if (o == null) {
            return defValue.toString();
        }
        return messageFormat.format(new Object[]{o});
    }

    private static boolean createDirectoryIfNeeded(final Path path, final ResourceBundle bundle) {
        final Path parent = path.getParent();
        if (parent != null && Files.notExists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (final IOException e) {
                printError(bundle, "failedCreateDirectory", e);
                return false;
            }
        }
        return true;
    }

    private static Path createPath(final String name, final ResourceBundle bundle, final String key) {
        try {
            return Path.of(name);
        } catch (final InvalidPathException e) {
            printError(bundle, key, e);
            return null;
        }
    }

    private static Locale getLocale(String languageTag) {
        return new Locale.Builder().setLanguageTag(languageTag).build();
    }

    private static void printError(ResourceBundle bundle, String key, Exception e) {
        System.err.println(bundle.getString(key) + " " + e.getMessage());
    }

    public static void main(final String[] args) {
        if (args == null || args.length != 4 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.print("Four not-null arguments required");
            return;
        }
        final Locale inputLocale = getLocale(args[0]);
        final Locale outputLocale = getLocale(args[1]);

        if (!(outputLocale.getLanguage().equals("ru") || outputLocale.getLanguage().equals("en"))) {
            System.err.println("Only ru and en locales supported for output");
            return;
        }
        final ResourceBundle bundle = ResourceBundle.getBundle("info.kgeorgiy.ja.fadeev.statistic.resources.Bundle", outputLocale);
        final Path pathIn = createPath(args[2], bundle, "invalidPathIn");
        final Path pathOut = createPath(args[3], bundle, "invalidPathOut");

        if (pathIn == null || pathOut == null) {
            return;
        }

        if (!createDirectoryIfNeeded(pathOut, bundle)) {
            return;
        }
        try {
            String text = Files.readString(pathIn);
            final List<Statistic<?, ?>> statistics = getStatistic(inputLocale, text);
            write(statistics, bundle, inputLocale, outputLocale, pathOut, args[2]);
        } catch (IOException e) {
            printError(bundle, "IOExceptionIn", e);
        }

    }

}