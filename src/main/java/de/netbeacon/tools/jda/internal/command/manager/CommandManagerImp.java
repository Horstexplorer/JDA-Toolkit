package de.netbeacon.tools.jda.internal.command.manager;

import de.netbeacon.tools.jda.api.annotations.Command;
import de.netbeacon.tools.jda.api.command.arg.Parsable;
import de.netbeacon.tools.jda.api.command.manager.CommandManager;
import de.netbeacon.tools.jda.api.event.listener.EventListenerPriority;
import de.netbeacon.tools.jda.api.language.manager.LanguageManager;
import de.netbeacon.tools.jda.internal.command.container.CommandContainer;
import de.netbeacon.tools.jda.internal.command.container.DataMap;
import de.netbeacon.tools.jda.internal.command.utils.ArgUtil;
import de.netbeacon.tools.jda.internal.exception.*;
import de.netbeacon.utils.concurrency.action.imp.SupplierExecutionAction;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandManagerImp extends ListenerAdapter implements CommandManager, EventListenerPriority {

    private final int priority;
    private LanguageManager languageManager;
    private Function<GenericEvent, String> prefixProvider;
    private boolean slashUpdate = false;
    private final List<Function<GenericEvent, DataMap>> externalDataSuppliers = new ArrayList<>();
    private final Map<Class<?>, Parsable<?>> parsers = new HashMap<>();
    private final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    private final List<CommandContainer> commands = new ArrayList<>();
    private final HashMap<String, CommandContainer> commandQuickAccess = new HashMap<>();

    private static final Pattern SPACE_PATTERN = Pattern.compile("\s+");
    private static final Pattern ARG_PATTERN = Pattern.compile("(\"(\\X*?)\")|([^\\s]\\X*?(?=\\s|\"|$))");


    public CommandManagerImp(){
        this.priority = 0;
    }

    public CommandManagerImp(int priority){
        this.priority = priority;
    }

    @Override
    public CommandManager setLanguageProvider(LanguageManager languageManager) {
        this.languageManager = languageManager;
        return this;
    }

    @Override
    public CommandManager setPrefixProvider(Function<GenericEvent, String> prefixProvider) {
        this.prefixProvider = prefixProvider;
        return this;
    }

    @Override
    public CommandManager addExternalDataSupplier(Function<GenericEvent, DataMap> externalDataSupplier) {
        this.externalDataSuppliers.add(externalDataSupplier);
        return this;
    }

    @Override
    public CommandManager performSlashUpdateOnStartup(boolean state) {
        this.slashUpdate = state;
        return this;
    }

    @Override
    public CommandManager registerCommands(List<CommandContainer> commandContainers) {
        commands.addAll(commandContainers);
        for(var container : commandContainers){
            var cmd = container.getAnnotation();
            commandQuickAccess.put(cmd.path(), container);
            for(var alias : cmd.alias()){
                commandQuickAccess.put(alias, container);
            }
        }
        return this;
    }

    @Override
    public CommandManager addParsers(List<Parsable<?>> parsables) {
        for(var parser : parsables){
            this.parsers.put(parser.type(), parser);
        }
        return this;
    }

    @Override
    public int getPriority() {
        return priority;
    }


    // EVENTS


    @Override
    public void onReady(@NotNull ReadyEvent event) {

    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {

    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        try {
            List<String> commandPath = new ArrayList<>();
            commandPath.add(event.getName());
            if(event.getSubcommandGroup() != null){
                commandPath.add(event.getSubcommandGroup());
            }
            if(event.getSubcommandName() != null){
                commandPath.add(event.getSubcommandName());
            }
            // find command
            Object cPos = commandQuickAccess;
            for(var arg : commandPath){
                if(cPos instanceof Map){
                    if(((HashMap<?, ?>) cPos).containsKey(arg)){
                        cPos = ((HashMap<?, ?>) cPos).get(arg);
                    }else{
                        cPos = null;
                        break;
                    }
                }else if(cPos instanceof CommandContainer){
                    break;
                }
            }
            if(cPos == null){
                return;
            }
            var commandContainer = (CommandContainer) cPos;
            var commandAnnotation = commandContainer.getAnnotation();
            if(commandAnnotation.type().equals(Command.Type.CHAT)
                    || commandAnnotation.origin().equals(Command.AccessOrigin.DM) && event.isFromGuild()
                    || commandAnnotation.origin().equals(Command.AccessOrigin.GUILD) && !event.isFromGuild()){
                return;
            }
            // check permissions
            if(event.isFromGuild() && !event.getTextChannel().isNSFW() && commandAnnotation.isNSFW()){
                throw new UnsuitableEnvironmentException(UnsuitableEnvironmentException.Type.NSFW,
                        "Tried to access an NSFW command in a non NSFW channel");
            }
            if(event.isFromGuild() && event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), commandAnnotation.botPermission())){
                throw new PermissionException(PermissionException.Type.BOT, "Bot is missing necessary permissions", new HashMap<>(){{
                    for(var perm : commandAnnotation.botPermission()){
                        put(perm, event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), perm));
                    }
                }});
            }
            if(event.isFromGuild() && event.getMember().hasPermission(event.getTextChannel(), commandAnnotation.userPermission())){
                throw new PermissionException(PermissionException.Type.USER, "User is missing necessary permissions", new HashMap<>(){{
                    for(var perm : commandAnnotation.botPermission()){
                        put(perm, event.getMember().hasPermission(event.getTextChannel(), perm));
                    }
                }});
            }
            // get external data
            DataMap internalDataMap = new DataMap()
                    .add("event", event)
                    .add("member", event.getMember())
                    .add("user", event.getUser())
                    .add("message", event.getMember())
                    .add("channel", event.getChannel())
                    .add("textChannel", event.getTextChannel())
                    .add("privateChannel", event.getPrivateChannel())
                    .add("jda", event.getJDA())
                    .add("languageManager", languageManager)
                    .add("options", event.getOptions())
                    .add("hook", event.getId())
                    .add("interaction", event.getInteraction());
            Supplier<DataMap> externalDataTask = () -> {
                DataMap extDataMap = new DataMap();
                for(var supplier : externalDataSuppliers)
                    extDataMap = DataMap.combine(extDataMap, supplier.apply(event));
                return internalDataMap;
            };
            new SupplierExecutionAction<>(executor, externalDataTask).queue(externalMap ->{
                var completeDataMap = DataMap.combine(internalDataMap, externalMap);
                try {
                    commandContainer.getMethod().invoke(commandContainer.getInstance(), ArgUtil.map(commandContainer, completeDataMap, parsers, event.getOptions()));
                } catch (ParameterException e){
                    // bad params
                } catch (ArgumentException e){
                    // bad args
                }catch (Exception e) {
                    // unhandled
                }
            }, exception -> {

            });
            // parse
            // run
        }catch (UnsuitableEnvironmentException e){
            // channel not suitable
        }catch (PermissionException e){
            // missing bot or user perms
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        try {
            if(event.getAuthor().isBot() || event.getAuthor().isSystem() || event.isWebhookMessage()){
                return;
            }
            // is command?
            var raw = event.getMessage().getContentRaw();
            var prefix = prefixProvider.apply(event);
            if(!raw.startsWith(prefix)){
                return;
            }
            raw = raw.substring(prefix.length());
            // find command
            String longestMatch = "";
            for(String path : commandQuickAccess.keySet()){
                if(raw.startsWith(path) && path.length() > longestMatch.length()){
                    longestMatch = path;
                }
            }
            CommandContainer commandContainer = commandQuickAccess.get(longestMatch);
            if(commandContainer == null){
                return;
            }
            raw = raw.substring(longestMatch.length());
            // parse args
            List<String> args = new ArrayList<>();
            Matcher matcher = ARG_PATTERN.matcher(raw);
            while(matcher.find()){
                args.add((matcher.group(2) != null) ? matcher.group(2) : matcher.group());
            }


            var commandAnnotation = commandContainer.getAnnotation();
            if(commandAnnotation.type().equals(Command.Type.SLASH)
                    || commandAnnotation.origin().equals(Command.AccessOrigin.DM) && event.isFromGuild()
                    || commandAnnotation.origin().equals(Command.AccessOrigin.GUILD) && !event.isFromGuild()){
                return;
            }
            // check permissions
            if(event.isFromGuild() && !event.getTextChannel().isNSFW() && commandAnnotation.isNSFW()){
                throw new UnsuitableEnvironmentException(UnsuitableEnvironmentException.Type.NSFW,
                        "Tried to access an NSFW command in a non NSFW channel");
            }
            if(event.isFromGuild() && !event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), commandAnnotation.botPermission())){
                throw new PermissionException(PermissionException.Type.BOT, "Bot is missing necessary permissions", new HashMap<>(){{
                    for(var perm : commandAnnotation.botPermission()){
                        put(perm, event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), perm));
                    }
                }});
            }
            if(event.isFromGuild() && !event.getMember().hasPermission(event.getTextChannel(), commandAnnotation.userPermission())){
                throw new PermissionException(PermissionException.Type.USER, "User is missing necessary permissions", new HashMap<>(){{
                    for(var perm : commandAnnotation.botPermission()){
                        put(perm, event.getMember().hasPermission(event.getTextChannel(), perm));
                    }
                }});
            }
            // get external data
            DataMap internalDataMap = new DataMap()
                    .add("event", event)
                    .add("member", event.getMember())
                    .add("author", event.getAuthor())
                    .add("message", event.getMember())
                    .add("channel", event.getChannel())
                    .add("textChannel", event.getTextChannel())
                    .add("privateChannel", event.isFromGuild() ? null : event.getPrivateChannel())
                    .add("jda", event.getJDA())
                    .add("languageManager", languageManager);
            Supplier<DataMap> externalDataTask = () -> {
                DataMap extDataMap = new DataMap();
                for(var supplier : externalDataSuppliers)
                    extDataMap = DataMap.combine(extDataMap, supplier.apply(event));
                return internalDataMap;
            };
            new SupplierExecutionAction<>(executor, externalDataTask).queue(externalMap ->{
                var completeDataMap = DataMap.combine(internalDataMap, externalMap);
                try {
                    commandContainer.getMethod().invoke(commandContainer.getInstance(), ArgUtil.map(commandContainer, completeDataMap, parsers, args));
                } catch (ParameterException e){
                    e.printStackTrace();
                    // bad params
                } catch (ArgumentException e){
                    e.printStackTrace();
                    // bad args
                }catch (Exception e) {
                    e.printStackTrace();
                    // unhandled
                }
            }, exception -> {
                exception.printStackTrace();
            });
            // parse
            // run
        }catch (UnsuitableEnvironmentException e){
            // channel not suitable
        }catch (PermissionException e){
            System.out.println(e.getPermissionCheck());
            // missing bot or user perms
        }
    }



}
