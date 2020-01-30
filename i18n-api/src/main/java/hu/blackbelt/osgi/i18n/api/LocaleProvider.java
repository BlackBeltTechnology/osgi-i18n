package hu.blackbelt.osgi.i18n.api;

import java.util.Locale;
import java.util.Optional;

public interface LocaleProvider {

    Optional<Locale> getLocale();
}
