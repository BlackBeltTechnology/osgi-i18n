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

import com.google.common.base.Strings;
import hu.blackbelt.osgi.i18n.api.MessageResolver;
import hu.blackbelt.osgi.i18n.api.MessageStreamLoader;
import hu.blackbelt.osgi.i18n.resourcebundle.utils.LocalePropertiesFileLoader;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Properties;

/**
 * Resolves interface based messages. When the primary message resolver is presented it is used to resolve message in the first, and when it is
 * unsuccessfull fallback to interface type based messages.
 */
public class ClassBasedPropertiesFileMessageResolver implements MessageResolver {

    private final Properties properties = new Properties();
    private final MessageResolver primaryMessageResolver;

    /**
     * Contructs the resolver.
     *
     * @param messageStreamLoader The stream loader instance to load property file
     * @param itf Interface type map messages to
     * @param lang Language
     * @param country Country
     * @param variant Variant
     * @param dmr Default message Resolver
     * @throws IOException
     * @throws InvalidParameterException
     */
    public ClassBasedPropertiesFileMessageResolver(MessageStreamLoader messageStreamLoader, Class<?> itf, String lang, String country,
                                                   String variant, MessageResolver dmr) throws IOException, InvalidParameterException {
        LocalePropertiesFileLoader propertiesFileLoader = new LocalePropertiesFileLoader(messageStreamLoader);
        propertiesFileLoader.loadProperties(itf, lang, country, variant, properties);
        this.primaryMessageResolver = dmr;
    }

    @Override
    public String get(String key) {
        String ret = null;
        if (primaryMessageResolver != null) {
            ret = primaryMessageResolver.get(key);
        }
        if (Strings.isNullOrEmpty(ret)) {
            ret = (String) properties.get(key);
        }
        return ret;
    }
}
