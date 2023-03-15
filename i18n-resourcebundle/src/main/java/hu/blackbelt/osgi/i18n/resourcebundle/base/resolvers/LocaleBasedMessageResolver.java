package hu.blackbelt.osgi.i18n.resourcebundle.base.resolvers;

/*-
 * #%L
 * OSGI I18N resource bundle implementation
 * %%
 * Copyright (C) 2018 - 2023 BlackBelt Technology
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
