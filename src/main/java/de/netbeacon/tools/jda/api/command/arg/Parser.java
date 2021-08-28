package de.netbeacon.tools.jda.api.command.arg;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.internal.exception.ParserException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Declares a class as parser for its used within arg parsing of commands
 *
 * @param <T> which will be parsed
 */
public interface Parser<T> {

    /**
     * Runs the discovery for all Parsers which have been marked as @Discoverable
     * <p>
     * Will create and return a new instance of each one of them where this is possible to do so
     *
     * @return List of found parsers
     */
    static List<Parser<?>> discovery() {
        try (var result = new ClassGraph().enableAllInfo().scan()) {
            return result.getAllClasses().stream()
                    .filter(classInfo -> classInfo.implementsInterface(Parser.class))
                    .filter(classInfo -> classInfo.hasAnnotation(Discoverable.class))
                    .map(ClassInfo::loadClass)
                    .filter(clazz -> Arrays.stream(clazz.getDeclaredConstructors()).anyMatch(constructor -> constructor.getParameterTypes().length == 0))
                    .map(clazz -> {
                        try {
                            return (Parser<?>) clazz.getDeclaredConstructor().newInstance();
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Class which the parser will process
     *
     * @return Class
     */
    Class<T> type();

    /**
     * Parsing the data from a String
     *
     * @param data as String
     * @return result
     */
    default T parse(String data) throws ParserException {
        return parse(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Parsing the data from a byte[]
     *
     * @param data as byte[]
     * @return result
     */
    T parse(byte[] data) throws ParserException;

}
