package info.kgeorgiy.ja.fadeev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * // :NOTE: Lowercase
 * implementation of {@link JarImpler}
 */
public class Implementor implements JarImpler {

    /**
     * produces code implementing interface by provided <var>token</var>.
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws ImplerException when
     *                         <ul><li>provided class is not an interface </li>
     *                             <li>provided class is private interface</li>
     *                             <li>can not create java-file</li>
     *                         </ul>
     */
    @Override
    public void implement(final Class<?> token, final Path root) throws ImplerException {
        checkInterface(token);
        final Path path = getFullPath(token, root, "java");
        createDirectoryIfNeeded(path);
        try (final BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
            write(bufferedWriter, getPackage(token));
            write(bufferedWriter, "class " + token.getSimpleName() + "Impl implements " + token.getCanonicalName() + " {" + LINE_SEPARATOR);
            for (final Method method : getAbstractMethods(token)) {
                write(bufferedWriter, getMethod(method));
            }
            write(bufferedWriter, "}");
        } catch (final IOException e) {
            throw new ImplerException("an I/O error occurs opening or creating file " + path.toString(), e);
        }
    }

    private void write(final BufferedWriter writer, final String data) throws IOException {
        writer.write(toEncoding(data));
    }

    /**
     * Returns full {@link Path} to generated file.
     * Path generated by adding {@link #getPath} to <var>root</var>
     *
     * @param token  type token to create path for
     * @param root   root directory
     * @param suffix suffix of generated file
     * @return full {@link Path} to generated file
     */
    private Path getFullPath(final Class<?> token, final Path root, final String suffix) {
        return root.resolve(getPath(token, suffix, FILE_SEPARATOR));
    }

    /**
     * Returns {@link String} in which presented <var>token</var> package and file to be generated.
     * <p>
     * File name generated by <var>token</var> simple name added {@code Impl.suffix}
     *
     * @param token     type token to create path for
     * @param suffix    suffix of generated file
     * @param separator file separator
     * @return {@link String} representing path
     */
    private String getPath(final Class<?> token, final String suffix, final char separator) {
        return token.getPackageName().replace('.', separator) + separator +
                token.getSimpleName() + "Impl." + suffix;
    }

    /**
     * Generates {@link String} describing <var>token</var> package
     * <p>
     * Provides empty {@link String} if package is default
     * Provides package plus <var>token</var> package otherwise
     *
     * @param token type token to get package from
     * @return {@link String} representing package
     */
    private static String getPackage(final Class<?> token) {
        final String packageName = token.getPackageName();
        return packageName.isEmpty() ? "" : "package " + token.getPackageName() + ";" + LINE_SEPARATOR.repeat(2);
    }


    /**
     * Generates {@link List} of abstract {@link Method} of given <var>token</var>
     *
     * @param token type token to get abstract methods from
     * @return {@link List} of abstract {@link Method}
     */
    private List<Method> getAbstractMethods(final Class<?> token) {
        return Arrays.stream(token.getMethods())
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .collect(Collectors.toList());
    }

    /**
     * Generates {@link String} representing generated method.
     * This string generated by adding {@link #getMethodHead} method head, {@link #getMethodTail} method tail and delimiters
     *
     * @param method method to be generated
     * @return {@link String} representing method
     */
    private String getMethod(final Method method) {
        return getMethodHead(method) + getMethodTail(method) + LINE_SEPARATOR.repeat(2);
    }

    /**
     * Generates {@link String} representing <var>method</var> head.
     * <p>
     * This string includes <var>method</var> modifiers, name, return type, arguments and delimiter
     *
     * @param method method to generate head for
     * @return {@link String} representing <var>method</var> head
     */
    private String getMethodHead(final Method method) {
        return String.format(
                "%s%s %s %s %s {",
                TAB,
                getModifiers(method),
                method.getReturnType().getCanonicalName(),
                method.getName(),
                Arrays.stream(method.getParameters())
                        .map(parameter -> parameter.getType().getCanonicalName() + " " + parameter.getName())
                        .collect(Collectors.joining(", ", "(", ")"))
        );
    }

    /**
     * Generates {@link String} of non-abstract and non-transient modifiers
     *
     * @param method method modifiers got from
     * @return {@link String} modifiers
     */
    private String getModifiers(final Method method) {
        return Modifier.toString(method.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT);
    }

    /**
     * Generates {@link String} representing <var>method</var> tail.
     * <p>
     * This string includes <var>method</var> "return", return value and delimiters
     *
     * @param method method to generate tail for
     * @return {@link String} representing method tail
     */
    private static String getMethodTail(final Method method) {
        return getReturn(method) + LINE_SEPARATOR + TAB + "}";
    }

    /**
     * Generates {@link String} representing <var>method</var> return with default values.
     * <p>
     * For void returns empty String
     * <p>
     * For boolean returns false
     * <p>
     * For {@link Class#isPrimitive()} numeric types returns 0
     * <p>
     * For other types return null
     *
     * @param method method to generate return for
     * @return {@link String} representing method return
     */
    private static String getReturn(final Method method) {
        final Class<?> methodClass = method.getReturnType();
        if (methodClass == void.class) {
            return "";
        } else {
            final String value;
            if (methodClass == boolean.class) {
                value = "false";
            } else if (methodClass.isPrimitive()) {
                value = "0";
            } else {
                value = "null";
            }
            return LINE_SEPARATOR + TAB.repeat(2) + "return " + value + ";";
        }
    }

    /**
     * Creates parent directory.
     *
     * @param path parent directory of this path generated
     * @throws ImplerException if can not create parent directory
     */
    private void createDirectoryIfNeeded(final Path path) throws ImplerException {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (final IOException e) {
                throw new ImplerException("Cannot create output path", e);
            }
        }
    }

    /**
     * Check if provided token is correct.
     * Provided token is correct if it is not private interface
     *
     * @param token type token to check
     * @throws ImplerException if provided token is not interface or provided token is private interface
     */
    private static void checkInterface(final Class<?> token) throws ImplerException {
        if (!Modifier.isInterface(token.getModifiers())) {
            throw new ImplerException("Provided type must be interface");
        }
        if (Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Provided interface must be not private");
        }
    }

    /**
     * {@link String} line separator, provided by current {@link System}.
     */
    private static final String LINE_SEPARATOR = System.lineSeparator();
    /**
     * char file separator, provided by {@link File}.
     */
    private static final char FILE_SEPARATOR = File.separatorChar;
    /**
     * {@link String} tab.
     */
    private static final String TAB = "    ";

    /**
     * Produces <var>.jar</var> file implementing interface specified by provided <var>token</var>.
     * <p>
     * class generated by {@link #implement} with same arguments
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException when
     *                         <ul><li>provided class is not interface </li>
     *                             <li>provided class is private interface</li>
     *                             <li>can not create java-file</li>
     *                             <li>can not compile generated class</li>
     *                             <li>can not create jar-file</li>
     *                         </ul>
     */
    @Override
    public void implementJar(final Class<?> token, final Path jarFile) throws ImplerException {
        Path path = jarFile.getParent() == null ? Path.of("") : jarFile.getParent();
        implement(token, path);
        compile(token, path);
        writeToJar(token, jarFile, path);
    }

    /**
     * Compile generated file.
     *
     * @param token type token to compile implemented class for
     * @param path  path to implemented class
     * @throws ImplerException if can not compile file
     */
    private void compile(final Class<?> token, final Path path) throws ImplerException {
        String sourcePath = null;
        try {
            sourcePath = Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        final JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        final String[] args = {"-cp",
                sourcePath,
                getFullPath(token, path, "java").toString()
        };
        if (javaCompiler.run(null, null, null, args) != 0) {
            throw new ImplerException("Cannot compile generated class");
        }
    }

    /**
     * Creates <var>.jar</var> file.
     * Creates <var>.jar</var> file with  manifest and compiled file
     *
     * @param token   type token to write to <var>.jar</var> file compiled implemented class for
     * @param jarFile path to jarFile
     * @param path    path to directory
     * @throws ImplerException if can not create jar file
     */
    private void writeToJar(final Class<?> token, final Path jarFile, final Path path) throws ImplerException {
        try (final JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile), createManifest())) {
            jarOutputStream.putNextEntry(new JarEntry(getPath(token, "class", '/')));
            Files.copy(getFullPath(token, path, "class"), jarOutputStream);
        } catch (final IOException e) {
            throw new ImplerException("Cannot create jar-file", e);
        }
    }

    /**
     * Creates {@link Manifest} manifest.
     * This manifest includes {@link Attributes.Name#MANIFEST_VERSION} and {@link Attributes.Name#IMPLEMENTATION_VENDOR}
     *
     * @return {@link Manifest} manifest
     */
    private Manifest createManifest() {
        final Manifest manifest = new Manifest();
        final Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Dmitry Fadeev");
        return manifest;
    }

    /**
     * Executes {@link #implement} or {@link #implementJar}.
     * <p>
     * if two arguments provided executes {@link #implement}
     * if three arguments provided and first argument is "-jar" executes {@link #implementJar}
     * All arguments must be not null in both cases
     *
     * @param args args provided
     */
    public static void main(final String[] args) {
        if (args == null) {
            System.err.println("Err: expected arguments, provided null");
        } else if (Arrays.asList(args).contains(null)) {
            System.err.println("Err: not null arguments expected");
        } else if (!(args.length == 2 || (args.length == 3 && args[0].equals("-jar")))) {
            System.err.println("two or three arguments expected, provided " + args.length);
        } else {
            final JarImpler jarImpler = new Implementor();
            try {
                if (args.length == 2) {
                    jarImpler.implement(Class.forName(args[0]), Path.of(args[1]));
                } else {
                    jarImpler.implementJar(Class.forName(args[1]), Path.of(args[2]));
                }
            } catch (final ClassNotFoundException e) {
                System.err.println("Err: class not found " + e.getMessage());
            } catch (final ImplerException e) {
                System.err.println("Err: implerException " + e.getMessage());
            } catch (final InvalidPathException e) {
                System.err.println("Err: invalid path " + e.getMessage());
            }
        }
    }

    /**
     * Encode given {@link String} to Unicode
     *
     * @param str string to be encoded
     * @return encoded {@link String}
     */
    private static String toEncoding(final String str) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (final char c : str.toCharArray()) {
            stringBuilder.append(c >= 128 ? String.format("\\u%04X", (int) c) : String.valueOf(c));
        }
        return stringBuilder.toString();
    }

}
