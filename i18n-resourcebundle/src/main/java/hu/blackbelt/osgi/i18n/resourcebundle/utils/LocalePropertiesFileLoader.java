package hu.blackbelt.osgi.i18n.resourcebundle.utils;

import com.google.common.base.Charsets;
import hu.blackbelt.osgi.i18n.api.MessageStreamLoader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

@Slf4j
public class
LocalePropertiesFileLoader {

    public static final String EMPTY = "";
    public static final String PROPERTIES_EXT = ".properties";
    public static final String UNDERSCORE = "_";
    public static final char DOT = '.';
    public static final char SLASH = '/';

    private MessageStreamLoader messageStreamLoader;

    public LocalePropertiesFileLoader(MessageStreamLoader msl) {
        this.messageStreamLoader = msl;
    }

    public void loadProperties(Class<?> itf, String lang, String country, String variant, Properties properties
    ) throws IOException {

        for (Class<?> superItf : itf.getInterfaces()) {
            loadProperties(superItf, lang, country, variant, properties);
        }
        String baseName = itf.getName().replace(DOT, SLASH);
        loadProperties(itf.getClassLoader(), baseName, lang, country, variant, properties);
    }

    public void loadProperties(ClassLoader cl, String baseName, String lang, String country, String variant, Properties properties
    ) throws IOException {
        String suffixLang = (lang == null || EMPTY.equals(lang)) ? EMPTY : (UNDERSCORE + lang);
        String suffixCountry = (country == null || EMPTY.equals(country)) ? EMPTY : (UNDERSCORE + country);
        String suffixVariant = (variant == null || EMPTY.equals(variant)) ? EMPTY : (UNDERSCORE + variant);

        MessageStreamLoader effStreamLoader = messageStreamLoader;
        if (effStreamLoader == null) {
            effStreamLoader = new ClassLoaderBasedMessageStreamLoader(cl);
        }

        InputStream in = null;
        if (in == null && !suffixVariant.equals(EMPTY)) {
            in = effStreamLoader.load(baseName + suffixLang + suffixCountry + suffixVariant + PROPERTIES_EXT);
        }
        if (in == null && !suffixCountry.equals(EMPTY)) {
            in = effStreamLoader.load(baseName + suffixLang + suffixCountry + PROPERTIES_EXT);
        }
        if (in == null && !suffixLang.equals(EMPTY)) {
            in = effStreamLoader.load(baseName + suffixLang + PROPERTIES_EXT);
        }
        if (in == null) {
            in = effStreamLoader.load(baseName + PROPERTIES_EXT);
        }

        if (in != null) {
            properties.load(new InputStreamReader(in, Charsets.UTF_8));
        }
    }


}
