package de.netbeacon.tools.jda.internal.command.container;

import de.netbeacon.tools.jda.api.annotations.Command;
import de.netbeacon.tools.jda.api.command.manager.CommandManager;
import de.netbeacon.tools.jda.internal.command.utils.CommandHelper;
import de.netbeacon.tools.jda.internal.exception.ArgumentException;
import de.netbeacon.tools.jda.internal.exception.ParameterException;
import de.netbeacon.tools.jda.internal.exception.PermissionException;
import de.netbeacon.tools.jda.internal.exception.UnsuitableEnvironmentException;
import de.netbeacon.utils.concurrency.action.imp.SupplierExecutionAction;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CommandImp {

    private final boolean isExecutable;

    private final String name;
    private final CommandImp parent;
    private final HashMap<String, CommandImp> subCommands = new HashMap<>();

    private final Object instance;
    private final Method method;
    private final Command annotation;
    private final List<CommandArgument> commandArguments;

    public static CommandImp asSpacer(String name){
        return asSpacer(null, name);
    }

    public static CommandImp asSpacer(CommandImp parent, String name){
        return asExecutable(parent, name, null, null, null);
    }

    public static CommandImp asExecutable(CommandImp parent, String name, Object instance, Method method, Command annotation){
        return new CommandImp(parent, name, instance, method, annotation);
    }

    private CommandImp(CommandImp parent, String name, Object instance, Method method, Command annotation){
        this.parent = parent;
        this.name = name;
        this.instance = instance;
        this.method = method;
        this.annotation = annotation;
        this.commandArguments = method != null ? Arrays.stream(method.getParameters()).map(CommandArgument::new).collect(Collectors.toList()) : null;
        this.isExecutable = (instance != null && method != null && annotation != null);
    }

    public String getName() {
        return name;
    }

    public CommandImp getParent(){
        return parent;
    }

    public void addSubCommand(CommandImp... commandImps){
        for(var path : commandImps){
            subCommands.put(path.getName(), path);
        }
    }

    public CommandImp getSubCommand(String name){
        return subCommands.get(name);
    }

    public HashMap<String, CommandImp> getSubCommands() {
        return subCommands;
    }

    public Object getInstance() {
        return instance;
    }

    public Method getMethod() {
        return method;
    }

    public boolean isExecutable() {
        return isExecutable;
    }

    public Command getAnnotation() {
        return annotation;
    }

    public List<CommandArgument> getArguments() {
        return commandArguments;
    }

    public void doExecute(CommandManager commandManager, List<String> args, GenericEvent genericEvent){
        if(isExecutable){
            if(!args.isEmpty()){
                var nextCommand = getSubCommand(args.get(0));
                if(nextCommand != null){
                    args.remove(0);
                    nextCommand.doExecute(commandManager, args, genericEvent);
                    return;
                }
            }
            if(genericEvent instanceof MessageReceivedEvent messageReceivedEvent){
                doExecute(commandManager, args, messageReceivedEvent);
            }else if(genericEvent instanceof SlashCommandEvent slashCommandEvent){
                doExecute(commandManager, slashCommandEvent);
            }
        }else if(!args.isEmpty()){
            var nextCommand = getSubCommand(args.get(0));
            if(nextCommand == null){
                return;
            }
            args.remove(0);
            nextCommand.doExecute(commandManager, args, genericEvent);
        }
    }

    public void doExecute(CommandManager commandManager, List<String> args, MessageReceivedEvent event){
        try {
            // language
            var languagePack = commandManager.getLanguagePackageProvider().apply(event);
            // check location
            if(annotation.origin().equals(Command.AccessOrigin.DM) && event.isFromGuild()
                    || annotation.origin().equals(Command.AccessOrigin.GUILD) && !event.isFromGuild()){
                throw new UnsuitableEnvironmentException(UnsuitableEnvironmentException.Type.CONTEXT,
                        "Tried to access command from wrong context location");
            }
            if(event.isFromGuild() && !event.getTextChannel().isNSFW() && annotation.isNSFW()){
                throw new UnsuitableEnvironmentException(UnsuitableEnvironmentException.Type.NSFW,
                        "Tried to access an NSFW command in a non NSFW channel");
            }
            // check permissions
            if(event.isFromGuild() && !event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), annotation.botPermission())){
                throw new PermissionException(PermissionException.Type.BOT, "Bot is missing necessary permissions", new HashMap<>(){{
                    for(var perm : annotation.botPermission()){
                        put(perm, event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), perm));
                    }
                }});
            }
            if(event.isFromGuild() && !event.getMember().hasPermission(event.getTextChannel(), annotation.userPermission())){
                throw new PermissionException(PermissionException.Type.USER, "User is missing necessary permissions", new HashMap<>(){{
                    for(var perm : annotation.botPermission()){
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
                    .add("languagePackage", languagePack)
                    .add("args", args);
            Supplier<DataMap> externalDataTask = () -> {
                DataMap extDataMap = new DataMap();
                for(var supplier : commandManager.getExternalDataSupplier())
                    extDataMap = DataMap.combine(extDataMap, supplier.apply(event));
                return internalDataMap;
            };
            new SupplierExecutionAction<>(commandManager.getExecutor(), externalDataTask).queue(externalMap ->{
                var completeDataMap = DataMap.combine(internalDataMap, externalMap);
                try {
                    method.invoke(internalDataMap, CommandHelper.map(this, completeDataMap, commandManager.getParsers(), args));
                } catch (ParameterException e){
                    // bad params
                } catch (ArgumentException e){
                    // bad args
                }catch (Exception e) {
                    // unhandled
                }
            }, exception -> {
                // external data
            });
        }catch (UnsuitableEnvironmentException e){
            // bad environment
        }catch (PermissionException e){
            // bad permission for bot or user
        }catch (Exception e){
            // unhandled
        }
    }

    public void doExecute(CommandManager commandManager, SlashCommandEvent event){
        try {
            // language
            var languagePack = commandManager.getLanguagePackageProvider().apply(event);
            // check location
            if(event.isFromGuild() && !event.getTextChannel().isNSFW() && annotation.isNSFW()){
                throw new UnsuitableEnvironmentException(UnsuitableEnvironmentException.Type.NSFW,
                        "Tried to access an NSFW command in a non NSFW channel");
            }
            // check permissions
            if(event.isFromGuild() && event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), annotation.botPermission())){
                throw new PermissionException(PermissionException.Type.BOT, "Bot is missing necessary permissions", new HashMap<>(){{
                    for(var perm : annotation.botPermission()){
                        put(perm, event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), perm));
                    }
                }});
            }
            if(event.isFromGuild() && event.getMember().hasPermission(event.getTextChannel(), annotation.userPermission())){
                throw new PermissionException(PermissionException.Type.USER, "User is missing necessary permissions", new HashMap<>(){{
                    for(var perm : annotation.botPermission()){
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
                    .add("languagePackage", languagePack)
                    .add("options", event.getOptions())
                    .add("hook", event.getId())
                    .add("interaction", event.getInteraction());
            Supplier<DataMap> externalDataTask = () -> {
                DataMap extDataMap = new DataMap();
                for(var supplier : commandManager.getExternalDataSupplier())
                    extDataMap = DataMap.combine(extDataMap, supplier.apply(event));
                return internalDataMap;
            };
            new SupplierExecutionAction<>(commandManager.getExecutor(), externalDataTask).queue(externalMap ->{
                var completeDataMap = DataMap.combine(internalDataMap, externalMap);
                try {
                   method.invoke(internalDataMap, CommandHelper.map(this, completeDataMap, commandManager.getParsers(), event.getOptions()));
                } catch (ParameterException e){
                    // bad params
                } catch (ArgumentException e){
                    // bad args
                }catch (Exception e) {
                    // unhandled
                }
            }, exception -> {
                // external data
            });
        }catch (UnsuitableEnvironmentException e){
            // bad environment
        }catch (PermissionException e){
            // bad permission for bot or user
        }catch (Exception e){
            // unhandled
        }
    }

}
