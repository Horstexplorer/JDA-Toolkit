package de.netbeacon.tools.jda.api.command.manager;

import de.netbeacon.tools.jda.api.annotations.Command;
import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parser;
import de.netbeacon.tools.jda.api.language.packag.LanguagePackage;
import de.netbeacon.tools.jda.internal.command.container.CommandImp;
import de.netbeacon.tools.jda.internal.command.container.DataMap;
import de.netbeacon.tools.jda.internal.tuples.Pair;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import net.dv8tion.jda.api.events.GenericEvent;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the manager for the command system
 */
public interface CommandManager {

    Function<? super GenericEvent, LanguagePackage> getLanguagePackageProvider();

    /**
     * Adds a function to supply external data to commands
     *
     * Exposed as parameter: as defined within the DataMap
     * @param externalDataSupplier function to provide external data
     * @return current instance of the CommandManager, useful for chaining
     */
    CommandManager addExternalDataSupplier(Function<? super GenericEvent, DataMap> externalDataSupplier);

    /**
     * Returns a list of all data suppliers
     *
     * @return data suppliers
     */
    List<Function<? super GenericEvent, DataMap>> getExternalDataSupplier();

    /**
     * Overload of {@link CommandManager#addParsers(List)}
     *
     * @param parsers List of parsers
     * @return current instance of the CommandManager, useful for chaining
     */
    default CommandManager addParsers(Parser<?>... parsers){
        return addParsers(Arrays.stream(parsers).toList());
    }

    /**
     * Adds a list of parsers used for argument parsing
     *
     * @param parsers List of parsers
     * @return current instance of the CommandManager, useful for chaining
     */
    CommandManager addParsers(List<Parser<?>> parsers);

    /**
     * Returns a map of all parsers
     *
     * @return map of parsers
     */
    Map<Class<?>, Parser<?>> getParsers();

    /**
     * Returns the executor which should be used to fetch the external data
     *
     * @return executor
     */
    Executor getExecutor();

    /**
     * Runs the discovery for all commands which have been marked as @Discoverable
     *
     * Will create and return a new instance of each one of them where this is possible to do so
     * @return Command map used in the command manager
     */
    static Map<String, ? super CommandImp> discover(Command.Type type){
        try(var result = new ClassGraph().enableAllInfo().scan()){
            var pairs = result.getAllClasses().stream()
                    .filter(classInfo -> classInfo.hasAnnotation(Discoverable.class))
                    .map(ClassInfo::loadClass)
                    .map(clazz -> {
                        var declared = clazz.getDeclaredMethods();
                        var methods = Arrays.stream(declared).filter(method -> {
                            var annotation = method.getDeclaredAnnotation(Command.class);
                            return annotation != null && (annotation.type().equals(type) || annotation.type().equals(Command.Type.CHAT_AND_SLASH) || type.equals(Command.Type.CHAT_AND_SLASH));
                        }).collect(Collectors.toList());
                        try {
                            var instance = clazz.getDeclaredConstructor().newInstance();
                            return new Pair<>(instance, methods);
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            var commandMap = new HashMap<String, CommandImp>();
            for(var pair : pairs){
                var instance = pair.getValue1();
                for(var method : pair.getValue2()){
                    var annotation = method.getDeclaredAnnotation(Command.class);
                    String[] paths = new String[annotation.alias().length + 1];
                    paths[0] = annotation.path();
                    System.arraycopy(annotation.alias(), 0, paths, 1, annotation.alias().length);
                    for(String path : paths){
                        if(path.isBlank()){
                            continue;
                        }
                        var pathArgs = path.split("\s+");
                        Object posBuffer = commandMap;
                        for(int i = 0; i < pathArgs.length - 1; i++){
                            var pathArg = pathArgs[i];
                            if(posBuffer instanceof Map posBufferMap){
                                if(!posBufferMap.containsKey(pathArg)){
                                    posBufferMap.put(pathArg, CommandImp.asSpacer(pathArg));
                                }
                                posBuffer = posBufferMap.get(pathArg);
                            }else if(posBuffer instanceof CommandImp posBufferCI){
                                if(posBufferCI.getSubCommand(path) == null){
                                    posBufferCI.addSubCommand(CommandImp.asSpacer(posBufferCI, path));
                                }
                                posBuffer = posBufferCI.getSubCommand(path);
                            }
                        }
                        if(posBuffer instanceof CommandImp commandImp){
                            commandImp.addSubCommand(CommandImp.asExecutable(commandImp, pathArgs[paths.length - 1], instance, method, annotation));
                        }else if(posBuffer instanceof Map map){
                            map.put(pathArgs[paths.length - 1], CommandImp.asExecutable(null, pathArgs[paths.length - 1], instance, method, annotation));
                        }
                    }
                }
            }
            return commandMap;
        }
    }

}
