package de.netbeacon.tools.jda.internal.command.manager;

import de.netbeacon.tools.jda.api.command.arg.Parser;
import de.netbeacon.tools.jda.api.command.manager.CommandManager;
import de.netbeacon.tools.jda.api.event.listener.EventListenerPriority;
import de.netbeacon.tools.jda.api.language.packag.LanguagePackage;
import de.netbeacon.tools.jda.internal.command.container.CommandImp;
import de.netbeacon.tools.jda.internal.command.container.DataMap;
import de.netbeacon.tools.jda.internal.command.utils.CommandHelper;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandManagerImp extends ListenerAdapter implements CommandManager, EventListenerPriority {

    private static final Pattern ARG_PATTERN = Pattern.compile("(\"(\\X*?)\")|([^\\s]\\X*?(?=\\s|\"|$))");
    private final int priority;
    private final Function<? super GenericEvent, LanguagePackage> languagePackageProvider;
    private final Function<? super GenericEvent, String> prefixProvider;
    private final List<Function<? super GenericEvent, DataMap>> externalDataSuppliers = new ArrayList<>();
    private final Map<Class<?>, Parser<?>> parsers = new HashMap<>();
    private final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private final Map<String, CommandImp> chatCommands;
    private final Map<String, CommandImp> slashCommands;


    public CommandManagerImp(Map<String, CommandImp> chatCommands, Map<String, CommandImp> slashCommands,
                             Function<? super GenericEvent, LanguagePackage> languagePackageProvider, Function<? super GenericEvent, String> prefixProvider) {
        this.chatCommands = chatCommands;
        this.slashCommands = slashCommands;
        this.priority = 0;
        this.languagePackageProvider = languagePackageProvider;
        this.prefixProvider = prefixProvider;
    }

    public CommandManagerImp(Map<String, CommandImp> chatCommands, Map<String, CommandImp> slashCommands,
                             int priority, Function<? super GenericEvent, LanguagePackage> languagePackageProvider, Function<? super GenericEvent, String> prefixProvider) {
        this.chatCommands = chatCommands;
        this.slashCommands = slashCommands;
        this.priority = priority;
        this.languagePackageProvider = languagePackageProvider;
        this.prefixProvider = prefixProvider;
    }

    @Override
    public Function<? super GenericEvent, LanguagePackage> getLanguagePackageProvider() {
        return languagePackageProvider;
    }

    @Override
    public CommandManager addExternalDataSupplier(Function<? super GenericEvent, DataMap> externalDataSupplier) {
        this.externalDataSuppliers.add(externalDataSupplier);
        return this;
    }

    @Override
    public List<Function<? super GenericEvent, DataMap>> getExternalDataSupplier() {
        return externalDataSuppliers;
    }

    @Override
    public CommandManager addParsers(List<Parser<?>> parsers) {
        for (var parser : parsers) {
            this.parsers.put(parser.type(), parser);
        }
        return this;
    }

    @Override
    public Map<Class<?>, Parser<?>> getParsers() {
        return parsers;
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    @Override
    public int getPriority() {
        return priority;
    }


    // EVENTS

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        List<String> args = new ArrayList<>() {{
            add(event.getName());
            if (event.getSubcommandGroup() != null) {
                add(event.getSubcommandGroup());
            }
            if (event.getSubcommandName() != null) {
                add(event.getSubcommandName());
            }
        }};
        var command = chatCommands.get(args.get(0));
        if (command == null) {
            return;
        }
        args.remove(0);
        command.doExecute(this, args, event);
    }

    public List<CommandData> getSlashCommandData() {
        var commandDataList = new ArrayList<CommandData>();
        for (var entry : slashCommands.entrySet()) {
            var command = entry.getValue();
            var commandData = new CommandData(command.getName(), command.isExecutable() ? command.getAnnotation().descriptionOverride() : "")
                    .addOptions(CommandHelper.getOptions(command));
            for (var subEntry : command.getSubCommands().entrySet()) {
                var subCommand = subEntry.getValue();
                if (!subCommand.getSubCommands().isEmpty()) {
                    var subCommandGroupData = new SubcommandGroupData(subCommand.getName(), subCommand.isExecutable() ? subCommand.getAnnotation().descriptionOverride() : "");
                    for (var subSubEntry : subCommand.getSubCommands().entrySet()) {
                        var subSubCommand = subSubEntry.getValue();
                        subCommandGroupData.addSubcommands(
                                new SubcommandData(subSubCommand.getName(), subSubCommand.isExecutable() ? subSubCommand.getAnnotation().descriptionOverride() : "")
                                        .addOptions(CommandHelper.getOptions(subSubCommand))
                        );
                    }
                    commandData.addSubcommandGroups(subCommandGroupData);
                } else {
                    commandData.addSubcommands(
                            new SubcommandData(subCommand.getName(), subCommand.isExecutable() ? subCommand.getAnnotation().descriptionOverride() : "")
                                    .addOptions(CommandHelper.getOptions(subCommand))
                    );
                }
            }
            commandDataList.add(commandData);
        }
        return commandDataList;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isSystem() || event.isWebhookMessage()) {
            return;
        }
        // is command?
        var raw = event.getMessage().getContentRaw();
        var prefix = prefixProvider.apply(event);
        if (!raw.startsWith(prefix)) {
            return;
        }
        raw = raw.substring(prefix.length());
        List<String> args = new ArrayList<>();
        Matcher matcher = ARG_PATTERN.matcher(raw);
        while (matcher.find()) {
            args.add((matcher.group(2) != null) ? matcher.group(2) : matcher.group());
        }
        if (args.isEmpty()) {
            return;
        }
        var command = chatCommands.get(args.get(0));
        if (command == null) {
            return;
        }
        args.remove(0);
        command.doExecute(this, args, event);
    }

}
