package hu.blackbelt.osgi.i18n.api;

public interface EnumI18nService {

    /**
     * Register an Enum i18N message.
     *
     * @param clazz
     */
    void register(Class<? extends Enum> clazz);

    /**
     * Unregister an Enm i18n message.
     * @param clazz
     */
    void unregister(Class<? extends Enum> clazz);

    String getMessageForEnum(Enum entry, Object... args);

}
