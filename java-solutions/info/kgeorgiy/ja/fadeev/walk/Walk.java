package info.kgeorgiy.ja.fadeev.walk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;


// :NOTE: Форматирование кода
public class Walk {
    private enum Err {
        NULL_INPUT("expected arguments, provided null"),
        NULL_ARGUMENTS_INPUT("expected two not null arguments, provided"),
        INVALID_NUMBER_OF_ARG("expected two arguments, provided"),
        CREATE_DIRECTORY_FAILED("failed to create directory"),
        READING_FAILED("failed to read file"),
        WRITING_FAILED("failed to write in file"),
        INVALID_PATH("invalid %s path"),
        IO_EXCEPTION("an I/O error occurs opening or creating %s file");
        private final String message;

        Err(final String message) {
            this.message = message;
        }

        void printMessage(final Object str, final Exception e) {
            printMessage(str, "", e);
        }

        void printMessage(final Object str, final String s, final Exception e) {
            System.err.println("ERROR: " + String.format(message, s) + " " + str + " " + (e == null ? "" : e.getMessage()));
        }
    }


    public static void main(final String[] args) {
        if (args == null) {
            Err.NULL_INPUT.printMessage("", null);
        } else if (args.length != 2) {
            Err.INVALID_NUMBER_OF_ARG.printMessage(args.length, null);
        } else if (args[0] == null || args[1] == null) {
            Err.NULL_ARGUMENTS_INPUT.printMessage(args[0] + " " + args[1], null);
        } else {
            walk(args[0], args[1]);
        }
    }

    private static void walk(final String in, final String out) {
        final Path pathIn = createPath(in, "in");
        final Path pathOut = createPath(out, "out");
        if (pathIn == null || pathOut == null) {
            return;
        }

        if (!createDirectoryIfNeeded(pathOut)) {
            return;
        }
        try (final BufferedReader bufferedReader = Files.newBufferedReader(pathIn)) {
            try (final BufferedWriter bufferedWriter = Files.newBufferedWriter(pathOut)) {
                try {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        try {
                            final Path path = Path.of(line);
                            final long hash = calculateHash(path);
                            writeHash(hash, path.toString(), bufferedWriter);
                        } catch (final InvalidPathException e) {
                            writeHash(0, line, bufferedWriter);
                        }
                    }
                } catch (final IOException e) {
                    Err.READING_FAILED.printMessage(out, e);
                }
            } catch (final IOException e) {
                Err.IO_EXCEPTION.printMessage(out, "output", e);
            }
        } catch (final IOException e) {
            Err.IO_EXCEPTION.printMessage(in, "input", e);
        }
    }

    private static boolean createDirectoryIfNeeded(final Path path) {
        final Path parent = path.getParent();
        if (parent != null && Files.notExists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (final IOException e) {
                Err.CREATE_DIRECTORY_FAILED.printMessage(parent.toString(), e);
                return false;
            }
        }
        return true;
    }

    public static void writeHash(final long hash, final String path, final BufferedWriter bufferedWriter) {
        try {
            bufferedWriter.write(String.format("%016x %s%n", hash, path));
        } catch (final IOException e) {
            Err.WRITING_FAILED.printMessage(path, e);
        }
    }

    public static long calculateHash(final Path path) {
        try (final InputStream inputStream = Files.newInputStream(path)) {
            final byte[] b = new byte[1024];
            int c;
            long hash = 0;
            while ((c = inputStream.read(b)) >= 0) {
                for (int i = 0; i < c; i++) {
                    hash = (hash << 8) + (b[i] & 0xff);
                    final long high = hash & 0xff00_0000_0000_0000L;
                    if (high != 0) {
                        hash ^= high >> 48;
                        hash &= ~high;
                    }
                }
            }
            return hash;
        } catch (final IOException e) {
            return 0;
        }
    }

    private static Path createPath(final String name, final String description) {
        try {
            return Path.of(name);
        } catch (final InvalidPathException e) {
            Err.INVALID_PATH.printMessage(name, description, e);
            return null;
        }
    }
}