package hu.blackbelt.osgi.i18n.api;

import java.util.Locale;
import java.util.function.Supplier;

public interface EnumI18nService {

    /**
     * Register an Enum i18N message.
     *
     * @param clazz
     */
    void register(Class<? extends Enum> clazz);

    /**
     * Register an Enum i18N message.
     *
     * @param clazz
     * @param localeSupplier
     */
    void register(Class<? extends Enum> clazz, Supplier<Locale> localeSupplier);

    void register(String className,  ClassLoader classLoader) throws ClassNotFoundException;

    void register(String className, Supplier<Locale> localeSupplier, ClassLoader classLoader) throws ClassNotFoundException;
        /**
         * Unregister an Enm i18n message.
         * @param clazz
         */
    void unregister(Class<? extends Enum> clazz);

    String getMessageForEnum(Enum entry, Object... args);

}
