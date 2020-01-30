package hu.blackbelt.osgi.i18n.resourcebundle.base.resolvers;

import com.google.common.base.Strings;
import hu.blackbelt.osgi.i18n.api.MessageResolver;
import hu.blackbelt.osgi.i18n.api.MessageStreamLoader;
import hu.blackbelt.osgi.i18n.resourcebundle.utils.LocalePropertiesFileLoader;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Properties;

/**
 * Name based message file resolver from property file.
 */
public class NameBasedPropertiesFileMessageResolver implements MessageResolver {
    protected final Properties properties = new Properties();
    protected final MessageResolver primaryMessageResolver;

    public NameBasedPropertiesFileMessageResolver(MessageStreamLoader messageStreamLoader, String name, String lang, String country,
                                                  String variant, MessageResolver pmr) throws IOException, InvalidParameterException {
        LocalePropertiesFileLoader propertiesFileLoader = new LocalePropertiesFileLoader(messageStreamLoader);
        propertiesFileLoader.loadProperties(null, name, lang, country, variant, properties);
        this.primaryMessageResolver = pmr;
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
