package de.netbeacon.tools.jda.api.command.manager;

import de.netbeacon.tools.jda.api.annotations.Command;
import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parsable;
import de.netbeacon.tools.jda.api.language.manager.LanguageManager;
import de.netbeacon.tools.jda.internal.command.container.CommandContainer;
import de.netbeacon.tools.jda.internal.command.container.DataMap;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import net.dv8tion.jda.api.events.GenericEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface CommandManager {

    CommandManager setLanguageProvider(LanguageManager languageManager);

    CommandManager setPrefixProvider(Function<GenericEvent, String> prefixProvider);

    CommandManager addExternalDataSupplier(Function<GenericEvent, DataMap> externalDataSupplier);

    CommandManager performSlashUpdateOnStartup(boolean state);

    default CommandManager registerCommands(CommandContainer... commandContainers){
        return registerCommands(Arrays.stream(commandContainers).toList());
    }

    CommandManager registerCommands(List<CommandContainer> commandContainers);

    default CommandManager addParsers(Parsable<?>... parsables){
        return addParsers(Arrays.stream(parsables).toList());
    }

    CommandManager addParsers(List<Parsable<?>> parsables);

    static List<CommandContainer> discover(Command.Type type){
        try(var result = new ClassGraph().enableAllInfo().scan()){
            return result.getAllClasses().stream()
                    .filter(classInfo -> classInfo.hasAnnotation(Discoverable.class))
                    .map(ClassInfo::loadClass)
                    .map(clazz -> {
                        var declared = clazz.getDeclaredMethods();
                        var methods = Arrays.stream(declared).filter(method -> {
                            var an = method.getDeclaredAnnotation(Command.class);
                            return an != null && (an.type().equals(type) || an.type().equals(Command.Type.CHAT_AND_SLASH));
                        }).collect(Collectors.toList());
                        try {
                            var instance = clazz.getDeclaredConstructor().newInstance();
                            var list = new ArrayList<CommandContainer>();
                            for(var method : methods){
                                list.add(new CommandContainer(instance, method));
                            }
                            return list;
                        } catch (Exception e) {
                            return new ArrayList<CommandContainer>();
                        }
                    }).flatMap(Collection::stream).collect(Collectors.toList());
        }
    }

}
