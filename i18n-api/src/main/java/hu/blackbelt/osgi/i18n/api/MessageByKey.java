package hu.blackbelt.osgi.i18n.api;

import java.lang.annotation.*;

/**
 * Defines the method is used to resolve messages from resource. The method have to define two arguments, first is a String, which is the key,
 * the second one Object[], which contains parameters for key.
 */
@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageByKey {
}
