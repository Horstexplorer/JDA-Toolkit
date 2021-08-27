package de.netbeacon.tools.jda.api.command.manager;

import de.netbeacon.tools.jda.api.annotations.Command;
import de.netbeacon.tools.jda.api.annotations.Discoverable;
import de.netbeacon.tools.jda.api.command.arg.Parser;
import de.netbeacon.tools.jda.api.language.packag.LanguagePackage;
import de.netbeacon.tools.jda.internal.command.container.CommandContainer;
import de.netbeacon.tools.jda.internal.command.container.DataMap;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import net.dv8tion.jda.api.events.GenericEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the manager for the command system
 */
public interface CommandManager {

    /**
     * Set the language manager used for within commands
     *
     * Exposed as parameter: LanguagePackage languagePackage
     * @param languagePackageProvider function to provide a language package based on the supplied event
     * @return current instance of the CommandManager, useful for chaining
     */
    CommandManager setLanguagePackageProvider(Function<? super GenericEvent, LanguagePackage> languagePackageProvider);

    /**
     * Sets the provider function for event based prefixes
     *
     * @param prefixProvider function to provide a prefix based on the data provided by the supplied event
     * @return current instance of the CommandManager, useful for chaining
     */
    CommandManager setPrefixProvider(Function<? super GenericEvent, String> prefixProvider);

    /**
     * Adds a function to supply external data to commands
     *
     * Exposed as parameter: as defined within the DataMap
     * @param externalDataSupplier function to provide external data
     * @return current instance of the CommandManager, useful for chaining
     */
    CommandManager addExternalDataSupplier(Function<? super GenericEvent, DataMap> externalDataSupplier);

    /**
     * Overload of {@link CommandManager#registerCommands(List)}
     *
     * @param commandContainers vararg of commands
     * @return current instance of the CommandManager, useful for chaining
     */
    default CommandManager registerCommands(CommandContainer... commandContainers){
        return registerCommands(Arrays.stream(commandContainers).toList());
    }

    /**
     * Registers a list of commands with this manager
     *
     * @param commandContainers list of commands
     * @return current instance of the CommandManager, useful for chaining
     */
    CommandManager registerCommands(List<CommandContainer> commandContainers);

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
     * Runs the discovery for all commands which have been marked as @Discoverable
     *
     * Will create and return a new instance of each one of them where this is possible to do so
     * @return List of command containers
     */
    static List<CommandContainer> discover(){
        try(var result = new ClassGraph().enableAllInfo().scan()){
            return result.getAllClasses().stream()
                    .filter(classInfo -> classInfo.hasAnnotation(Discoverable.class))
                    .map(ClassInfo::loadClass)
                    .map(clazz -> {
                        var declared = clazz.getDeclaredMethods();
                        var methods = Arrays.stream(declared).filter(method -> {
                            var an = method.getDeclaredAnnotation(Command.class);
                            return an != null;
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
