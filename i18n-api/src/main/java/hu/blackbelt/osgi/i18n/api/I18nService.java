package hu.blackbelt.osgi.i18n.api;

public interface I18nService {

    /**
     * Register an i18N proxy interface to OSGi service registry. The proxy methods resolved as string to access i18n data.
     *
     * @param clazz
     */
    <T> T register(Class<T> clazz);

    /**
     * Unregister an i18N proxy interface from  OSGi service registry.
     * @param clazz
     */
    <T> void unregister(Class<T> clazz);


    /**
     * Get formatted, localized message.
     * @param key
     * @param params
     */
    String format(String key, Object... params);

    /**
     * Get messgae template.
     * @param key
     */
    String getTemplate(String key);

}
