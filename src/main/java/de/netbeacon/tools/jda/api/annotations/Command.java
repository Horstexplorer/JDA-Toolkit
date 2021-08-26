package de.netbeacon.tools.jda.api.annotations;

import net.dv8tion.jda.api.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as command
 *
 * path: contains the arguments present to access this command
 * alias: an array of possible paths used as alias for this command
 * descriptionOverride: description which should be used as fallback for when no language manager has been set up
 * botPermission: the permissions the bot needs to have to execute this command. Defaults to Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS
 * userPermission: the permissions the user needs to have to execute this command. Defaults to none
 * type: if the command should be accessed via chat, slash-command or both
 * origin: the origin of the command which can be guild, dm or global
 * isNSFW: whether this command executes nsfw content or not
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

    enum AccessOrigin {
        /**
         * Access from guild context only
         * If used as slash command this will be registered guild scoped
         */
        GUILD,

        /**
         * Access from dms only
         */
        DM,

        /**
         * Access from both dms and guild context
         * If used as slash command this will be registered global scoped
         */
        GLOBAL;
    }

    enum Type {

        /**
         * Makes this a slash command
         */
        SLASH,

        /**
         * Makes this a chat command
         */
        CHAT,

        /**
         * Tries to provide this command both as slash and chat command
         */
        CHAT_AND_SLASH;
    }

    /**
     * Contains the arguments present to access this command
     *
     * @return Path
     */
    String path();

    /**
     * An array of possible paths used as alias for this command
     *
     * @return Alias[]
     */
    String[] alias();

    /**
     * Description which should be used as fallback for when no language manager has been set up
     *
     * @return descriptionOverride
     */
    String descriptionOverride() default "";

    /**
     * Permissions the bot needs to have
     *
     * default is Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS
     * @return Permission[]
     */
    Permission[] botPermission() default {Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS};

    /**
     * Permissions the user needs to have
     *
     * @return Permission[]
     */
    Permission[] userPermission() default {};

    /**
     * What type of command this should represent
     *
     * @return Type
     */
    Type type() default Type.CHAT;

    /**
     * From which context this command might be accessible
     *
     * @return AccessOrigin
     */
    AccessOrigin origin() default AccessOrigin.GLOBAL;

    /**
     * Whether this command returns nsfw content
     *
     * default is false
     * @return boolean
     */
    boolean isNSFW() default false;
}
