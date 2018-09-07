package hu.blackbelt.osgi.i18n.api;

import java.lang.annotation.*;

/**
 * Defines the key and value for message. It can be used to verride the names on property files.
 */
@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Message {
    String value() default "";
    String key() default "";
}
