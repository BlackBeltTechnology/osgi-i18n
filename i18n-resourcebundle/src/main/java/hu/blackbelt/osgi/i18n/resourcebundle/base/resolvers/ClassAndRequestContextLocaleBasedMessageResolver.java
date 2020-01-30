package hu.blackbelt.osgi.i18n.resourcebundle.base.resolvers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import hu.blackbelt.osgi.i18n.api.MessageResolver;
import hu.blackbelt.osgi.i18n.api.MessageStreamLoader;
import lombok.SneakyThrows;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * Aggregates multiple message resolver to be able to handle multiple language based on the context language. It is using Lazy mode
 * to load language based resolvers.
 */
public class ClassAndRequestContextLocaleBasedMessageResolver implements MessageResolver {

    public static final int CACHE_SIZE = 500;
    private final Supplier<Locale> localeSupplier;
    private final Class<?> itf;
    private final MessageStreamLoader messageStreamLoader;
    private final MessageResolver primaryMessageResolver;


    LoadingCache<Locale, MessageResolver> localeMessageResolverLoadingCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build(
            new CacheLoader<Locale, MessageResolver>() {
                @Override
                public MessageResolver load(Locale key) throws Exception {
                    return new ClassBasedPropertiesFileMessageResolver(messageStreamLoader, itf,
                            key.getLanguage(), key.getCountry(), key.getVariant(), primaryMessageResolver);
                }
            }
    );


    public ClassAndRequestContextLocaleBasedMessageResolver(Supplier<Locale> localeSupplier, Class<?> itf,
                                                            MessageStreamLoader msl, MessageResolver dmr) {
        this.localeSupplier = localeSupplier;
        this.itf = itf;
        this.messageStreamLoader = msl;
        this.primaryMessageResolver = dmr;
    }

    @Override
    @SneakyThrows(ExecutionException.class)
    public String get(String key) {
        String localized = localeMessageResolverLoadingCache.get(localeSupplier.get()).get(key);
        return localized;
    }
}
