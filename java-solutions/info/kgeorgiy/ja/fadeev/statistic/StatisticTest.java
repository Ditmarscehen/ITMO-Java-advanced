package info.kgeorgiy.ja.fadeev.statistic;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


@RunWith(JUnit4.class)
public class StatisticTest {
    private final ByteArrayOutputStream errOutStream = new ByteArrayOutputStream();
    private final PrintStream errPrStream = System.err;
    private static final String pathToFiles = "java-solutions/info/kgeorgiy/ja/fadeev/statistic/testFiles/";
    private final List<String> localesIn = List.of("ru-RU", "en-US", "ja-JP");
    private final List<String> localesOut = List.of("ru-RU", "en-US");


    @Before
    public void setUpStreams() {
        System.setErr(new PrintStream(errOutStream));
    }

    @After
    public void restoreStreams() {
        System.setErr(errPrStream);
    }

    @Test
    public void testWords() throws IOException {
        test("words");
    }

    @Test
    public void testSentences() throws IOException {
        test("sentences");
    }

    @Test
    public void testNumbers() throws IOException {
        test("numbers");
    }

    @Test
    public void testMoney() throws IOException {
        test("money");
    }

    @Test
    public void testDates() throws IOException {
        test("dates");
    }

    @Test
    public void testAll() throws IOException {
        test("all");
    }

    private void test(final String what) throws IOException {
        for (final String localeIn : localesIn) {
            final String inputFile = getInputFile(localeIn, what);
            for (final String localeOut : localesOut) {
                final String outputFile = pathToFiles + what + "From" + localeIn + "To" + localeOut + ".txt";
                final String expectedAnswerFile = pathToFiles + what + "ExpectedFrom" + localeIn + "To" + localeOut + ".txt";
                TextStatistic.main(new String[]{localeIn, localeOut, inputFile, outputFile});
                final String s1 = Files.readString(Path.of(outputFile));
                final String s2 = Files.readString(Path.of(expectedAnswerFile));
                Assert.assertEquals(s2, s1);
            }
        }
    }

    private String getInputFile(final String locale, final String what) {
        return pathToFiles + what + "In" + locale + ".txt";
    }


    private void testException(final String message, final String inL, final String outL, final String inF, final String outF) {
        TextStatistic.main(new String[]{inL, outL, inF, outF});
        Assert.assertTrue(errOutStream.toString().startsWith(message));
    }

    @Test
    public void testIncorrectInput() {
        testException("Four not-null arguments required", "1", "2", "3", null);
    }

    @Test
    public void testIncorrectLocales() {
        testException("Only ru and en locales supported for output", "en-US", "fr-FR", "in.txt", "out.txt");
    }

    private void testInvalidPathIn(final String locale, final String message) {
        testException(message, "en-US", locale, "i|n.txt", "out.txt");
    }

    private void testInvalidPathOut(final String locale, final String message) {
        testException(message, "en-US", locale, "in.txt", "ou?t.txt");
    }

    @Test
    public void testInvalidPathInRu() {
        testInvalidPathIn("ru-RU", "Неверный путь ввода");
    }

    @Test
    public void testInvalidPathOutRu() {
        testInvalidPathOut("ru-RU", "Неверный путь вывода");
    }

    @Test
    public void testInvalidPathInEn() {
        testInvalidPathIn("en-US", "Invalid input path");
    }

    @Test
    public void testInvalidPathOutEn() {
        testInvalidPathOut("en-US", "Invalid output path");
    }

    private void testNoInputFile(final String locale, final String message) {
        testException(message, "en-US", locale, "nofile.txt", "nof.txt");
    }

    @Test
    public void testNoInputFileRu() {
        testNoInputFile("ru-RU", "Произошла ошибка ввода-вывода при чтении из входного файла или считывании неверной или не отображаемой последовательности байтов");
    }

    @Test
    public void testNoInputFileEN() {
        testNoInputFile("en-US", "An I/O error occurred reading from the input file or a malformed or unmappable byte sequence was read");
    }

    public void testOutputException(final String locale, final String message) throws IOException {
        final String output = pathToFiles + "outReadOnly.txt";
        final File outputFile = new File(output);
        if (outputFile.createNewFile()) {
            if (outputFile.setReadOnly()) {
                final String input = getInputFile(locale, "all");
                testException(message, locale, locale, input, output);
                if (!outputFile.delete()) {
                    System.out.println("Failed to delete file" + output);
                }
            } else {
                System.out.println("Failed to set read only " + output);
            }
        } else {
            System.out.println("Failed to create file " + output);
        }
    }

    @Test
    public void testOutputExceptionRu() throws IOException {
        testOutputException("ru-RU", "Произошла ошибка ввода-вывода при чтении из выходного файла или считывании неверной или не отображаемой последовательности байтов");
    }

    @Test
    public void testOutputExceptionEn() throws IOException {
        testOutputException("en-US", "An I/O error occurred reading from the output file or a malformed or unmappable byte sequence was read");
    }
}
