package hu.blackbelt.osgi.i18n.resourcebundle;

import com.google.common.collect.Maps;
import hu.blackbelt.osgi.i18n.api.EnumI18nService;
import hu.blackbelt.osgi.i18n.api.LocaleProvider;
import hu.blackbelt.osgi.i18n.api.MessageResolver;
import hu.blackbelt.osgi.i18n.api.MessageStreamLoader;
import hu.blackbelt.osgi.i18n.resourcebundle.base.resolvers.ClassAndRequestContextLocaleBasedMessageResolver;
import hu.blackbelt.osgi.i18n.resourcebundle.utils.ClassLoaderBasedMessageStreamLoader;
import hu.blackbelt.osgi.i18n.resourcebundle.utils.SimpleParameterTemplater;
import hu.blackbelt.osgi.i18n.util.I18NUtil;
import lombok.extern.slf4j.Slf4j;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

@Component(immediate = true)
@Designate(ocd = EnumI18nServiceImpl.Config.class)
@Slf4j
public class EnumI18nServiceImpl implements EnumI18nService {

    @ObjectClassDefinition
    public @interface Config {

        @AttributeDefinition(name = "Default locale")
        String defaultLocale();
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    LocaleProvider localeProvider;

    private Locale defaultLocale;

    private Supplier<Locale> localeSupplier = () -> localeProvider != null && localeProvider.getLocale().isPresent() ? localeProvider.getLocale().get() : (defaultLocale != null ? defaultLocale : Locale.getDefault());

    private final Map<Class<? extends Enum>, MessageResolver> resolvers = Maps.newConcurrentMap();

    @Activate
    void activate(Config config) {
        defaultLocale = I18NUtil.getLocaleFromBCP47(config.defaultLocale());
    }

    public void register(Class<? extends Enum> clazz) {
        MessageStreamLoader messageStreamLoader = new ClassLoaderBasedMessageStreamLoader(clazz.getClassLoader());
        ClassAndRequestContextLocaleBasedMessageResolver messageResolver =
                new ClassAndRequestContextLocaleBasedMessageResolver(localeSupplier, clazz, messageStreamLoader, null);
        resolvers.put(clazz, messageResolver);
    }

    public void unregister(Class<? extends Enum> clazz) {
        resolvers.remove(clazz);
    }

    public String getMessageForEnum(Enum entry, Object...args) {
        String template = null;
        try {
            template = resolvers.get(entry.getClass()).get(entry.name());
        } catch (Throwable throwable) {
            log.warn("Could not resolve message: " + entry.name(), throwable);
        }

        if (template == null) {
            template = entry.name();
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    template = template + " {" + i + "}";
                }
            }
        }
        return SimpleParameterTemplater.templateMessage(template, args);
    }
}
