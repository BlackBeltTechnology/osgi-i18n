package hu.blackbelt.osgi.i18n.resourcebundle.base.resolvers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import hu.blackbelt.osgi.i18n.api.MessageResolver;
import hu.blackbelt.osgi.i18n.api.MessageStreamLoader;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * Aggregates multiple message resolver to be able to handle multiple language based on the language. It is using Lazy mode
 * to load language based resolvers.
 */
@Slf4j
public class LocaleBasedMessageResolver implements MessageResolver {

    public static final int CACHE_SIZE = 500;
    private final Supplier<Locale> localeSupplier;
    private final String name;
    private final MessageStreamLoader messageStreamLoader;
    protected final MessageResolver primaryMessageResolver;


    LoadingCache<Locale, MessageResolver> localeMessageResolverLoadingCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build(
            new CacheLoader<Locale, MessageResolver>() {
                @Override
                public MessageResolver load(Locale key) throws Exception {
                    return new NameBasedPropertiesFileMessageResolver(messageStreamLoader, name,
                            key.getLanguage(), key.getCountry(), key.getVariant(), primaryMessageResolver);
                }
            }
    );

    public LocaleBasedMessageResolver(Supplier<Locale> localeSupplier, String name,
                                      MessageStreamLoader msl, MessageResolver dmr) {
        this.localeSupplier = localeSupplier;
        this.name = name;
        this.messageStreamLoader = msl;
        this.primaryMessageResolver = dmr;
    }

    @Override
    @SneakyThrows(ExecutionException.class)
    public String get(String key) {
        String localized = localeMessageResolverLoadingCache.get(localeSupplier.get()).get(key);
        log.info("-> {}, {}", localeSupplier.get(), localized);
        return localized;
    }
}
