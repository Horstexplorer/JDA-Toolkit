package de.netbeacon.tools.jda.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method parameter as command argument, exposing it to the user input
 *
 * name: the name of the argument
 * descriptionOverride: description which should be used as fallback for when no language manager has been set up
 * isOptional: whether this argument is optional
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Argument {

    /**
     * Argument name as shown to the user
     *
     * @return String
     */
    String name();

    /**
     * Description which should be used as fallback for when no language manager has been set up
     *
     * @return String
     */
    String descriptionOverride() default "";

    /**
     * Whether this argument is optional
     *
     * default is false
     * @return boolean
     */
    boolean isOptional() default false;

}
