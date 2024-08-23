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


    /**
     * Unregister an Enm i18n message.
     * @param clazz
     */
    void unregister(Class<? extends Enum> clazz);

    String getMessageForEnum(Enum entry, Object... args);

}
