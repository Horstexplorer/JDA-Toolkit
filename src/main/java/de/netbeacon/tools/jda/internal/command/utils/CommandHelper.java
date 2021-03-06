package de.netbeacon.tools.jda.internal.command.utils;

import de.netbeacon.tools.jda.api.command.arg.Parser;
import de.netbeacon.tools.jda.internal.command.container.CommandArgument;
import de.netbeacon.tools.jda.internal.command.container.CommandImp;
import de.netbeacon.tools.jda.internal.command.container.DataMap;
import de.netbeacon.tools.jda.internal.exception.ArgumentException;
import de.netbeacon.tools.jda.internal.exception.ParameterException;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandHelper {

    private CommandHelper() {
    }

    public static Object[] map(CommandImp commandImp, DataMap additionalDataMap, Map<Class<?>, Parser<?>> parsers, List<?> args) {
        var argumentContainers = commandImp.getArguments();
        var mappedArgs = new Object[argumentContainers.size()];
        var argPos = 0;
        var parPos = new HashMap<Class<?>, Integer>();
        for (int i = 0; i < argumentContainers.size(); i++) {
            var argContainer = argumentContainers.get(i);
            if (argContainer.isExposedArgument()) {
                // arg from input
                var cPos = argPos++;
                var annotation = argContainer.getAnnotation();
                if (cPos >= args.size()) {
                    if (annotation.isOptional()) {
                        mappedArgs[i] = null;
                        continue;
                    } else {
                        throw new ArgumentException(ArgumentException.Type.NOT_SUPPLIED, "Argument " + argContainer.getDisplayName() + " has not been provided");
                    }
                }
                var arg = args.get(cPos);
                if (arg instanceof OptionMapping option) {
                    if (Boolean.class.equals(argContainer.getAClass())) {
                        mappedArgs[i] = option.getAsBoolean();
                    } else if (Long.class.equals(argContainer.getAClass())) {
                        mappedArgs[i] = option.getAsLong();
                    } else if (GuildChannel.class.equals(argContainer.getAClass())) {
                        mappedArgs[i] = option.getAsGuildChannel();
                    } else if (MessageChannel.class.equals(argContainer.getAClass())) {
                        mappedArgs[i] = option.getAsMessageChannel();
                    } else if (Member.class.equals(argContainer.getAClass())) {
                        mappedArgs[i] = option.getAsMember();
                    } else if (User.class.equals(argContainer.getAClass())) {
                        mappedArgs[i] = option.getAsUser();
                    } else if (Role.class.equals(argContainer.getAClass())) {
                        mappedArgs[i] = option.getAsRole();
                    } else if (IMentionable.class.equals(argContainer.getAClass())) {
                        mappedArgs[i] = option.getAsMentionable();
                    } else {
                        if (!parsers.containsKey(argContainer.getAClass())) {
                            throw new ArgumentException(ArgumentException.Type.NO_AVAILABLE_PARSER, "No parser available to parse type " + argContainer.getAClass().getName() + " for argument " + argContainer.getDisplayName());
                        }
                        var parser = parsers.get(argContainer.getAClass());
                        try {
                            var result = parser.parse(option.getAsString());
                            if (result == null && !annotation.isOptional()) {
                                throw new RuntimeException("Parsing result is null but argument is not optional");
                            }
                            mappedArgs[i] = result;
                        } catch (Exception e) {
                            throw new ArgumentException(ArgumentException.Type.PARSING_FAILED, "Failed to parse type " + argContainer.getAClass().getName() + " for argument " + argContainer.getDisplayName(), e);
                        }
                    }
                } else {
                    if (!parsers.containsKey(argContainer.getAClass())) {
                        throw new ArgumentException(ArgumentException.Type.NO_AVAILABLE_PARSER, "No parser available to parse type " + argContainer.getAClass().getName() + " for argument " + argContainer.getDisplayName());
                    }
                    var parser = parsers.get(argContainer.getAClass());
                    try {
                        var result = parser.parse((String) arg);
                        if (result == null && !annotation.isOptional()) {
                            throw new RuntimeException("Parsing result is null but argument is not optional");
                        }
                        mappedArgs[i] = result;
                    } catch (Exception e) {
                        throw new ArgumentException(ArgumentException.Type.PARSING_FAILED, "Failed to parse type " + argContainer.getAClass().getName() + " for argument " + argContainer.getDisplayName(), e);
                    }
                }
            } else {
                // param from data
                var paramClass = argContainer.getAClass();
                var paramName = argContainer.getParameter().getName();
                // data by param
                if (additionalDataMap.containsKey(paramName)) {
                    Object data = additionalDataMap.get(paramName);
                    if (data != null && !paramClass.isAssignableFrom(data.getClass())) {
                        throw new ParameterException("Type of parameter " + paramName + "%" + paramClass.getName() + " does not match with the found object of type " + data.getClass());
                    }
                    mappedArgs[i] = data;
                } else {
                    var dataL = additionalDataMap.get(paramClass);
                    if (!parPos.containsKey(paramClass)) {
                        parPos.put(paramClass, 0);
                    }
                    var cParPos = parPos.get(paramClass);
                    if (cParPos >= dataL.size()) {
                        throw new ParameterException("Data for Parameter " + paramName + "%" + paramClass.getName() + " not provided.");
                    }
                    var data = dataL.get(cParPos);
                    parPos.put(paramClass, cParPos + 1);
                    mappedArgs[i] = data;
                }
            }
        }
        return mappedArgs;
    }

    public static List<OptionData> getOptions(CommandImp commandImp) {
        return commandImp.getArguments().stream()
                .filter(CommandArgument::isExposedArgument)
                .map(arg -> {
                    var isOptional = arg.getAnnotation().isOptional();
                    var clazz = arg.getAClass();
                    if (Boolean.class.isAssignableFrom(clazz)) {
                        return new OptionData(OptionType.INTEGER, arg.getDisplayName(), arg.getAnnotation().descriptionOverride()).setRequired(isOptional);
                    } else if (Integer.class.isAssignableFrom(clazz)) {
                        return new OptionData(OptionType.INTEGER, arg.getDisplayName(), arg.getAnnotation().descriptionOverride()).setRequired(isOptional);
                    } else if (User.class.isAssignableFrom(clazz) || net.dv8tion.jda.api.entities.Member.class.isAssignableFrom(clazz)) {
                        return new OptionData(OptionType.USER, arg.getDisplayName(), arg.getAnnotation().descriptionOverride()).setRequired(isOptional);
                    } else if (GuildChannel.class.isAssignableFrom(clazz) || MessageChannel.class.isAssignableFrom(clazz)) {
                        return new OptionData(OptionType.CHANNEL, arg.getDisplayName(), arg.getAnnotation().descriptionOverride()).setRequired(isOptional);
                    } else if (Role.class.isAssignableFrom(clazz)) {
                        return new OptionData(OptionType.ROLE, arg.getDisplayName(), arg.getAnnotation().descriptionOverride()).setRequired(isOptional);
                    } else {
                        return new OptionData(OptionType.STRING, arg.getDisplayName(), arg.getAnnotation().descriptionOverride()).setRequired(isOptional);
                    }
                }).collect(Collectors.toList());
    }

}
