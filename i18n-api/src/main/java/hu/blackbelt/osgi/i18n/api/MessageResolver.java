package hu.blackbelt.osgi.i18n.api;

/**
 * Resolves a messsage. The locale handling is defined on upper level.
 */
public interface MessageResolver {
    String get(String key);
}
