package de.netbeacon.tools.jda.api.command.arg;

import de.netbeacon.tools.jda.api.annotations.Discoverable;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface Parsable<T> {

    Class<T> type();

    default T parse(String data){
        return parse(data.getBytes(StandardCharsets.UTF_8));
    }

    T parse(byte[] data);

    static List<Parsable<?>> discovery(){
        try(var result = new ClassGraph().enableAllInfo().scan()){
            return result.getAllClasses().stream()
                    .filter(classInfo -> classInfo.implementsInterface(Parsable.class))
                    .filter(classInfo -> classInfo.hasAnnotation(Discoverable.class))
                    .map(ClassInfo::loadClass)
                    .filter(clazz -> Arrays.stream(clazz.getDeclaredConstructors()).anyMatch(constructor -> constructor.getParameterTypes().length == 0))
                    .map(clazz -> {
                        try {
                            return (Parsable<?>) clazz.getDeclaredConstructor().newInstance();
                        }catch (Exception e){
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

}
