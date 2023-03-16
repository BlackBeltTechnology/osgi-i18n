package hu.blackbelt.osgi.i18n.resourcebundle.utils;

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

import com.google.common.base.CharMatcher;
import hu.blackbelt.osgi.i18n.resourcebundle.base.InterfaceMessageTemplaterInvocationHandler;
import hu.blackbelt.osgi.i18n.resourcebundle.base.resolvers.ClassBasedPropertiesFileMessageResolver;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Locale;
import java.util.ResourceBundle;

public final class I18N {

    public static final String EMPTY = "";

    public static final String INVALID_LOCALE_FORMAT = "Invalid locale format: ";
    public static final String UNDERSCORE = "_";
    public static final char UNDERSCORE_CHAR = '_';
    public static final String HASH = "#";
    public static final int LOC_END_INDEX = 3;
    public static final int LOC_MAX_LEN = 5;
    public static final int LOC_BEGIN_INDEX = 4;

    private I18N() {
    }

    public static <T> T create(Class<T> itf) throws IOException {
        return create(itf, (Locale) null, (ClassLoader) null);
    }

    public static Locale createLocaleFromLang(String lang) {
        return createLocaleFromLang(lang, null);
    }

    public static Locale createLocaleFromLang(String lang, Locale loc) {
        Locale ret = toLocale(lang);
        if (ret == null) {
            ret = Locale.getDefault();
        }
        return ret;
    }

    public static <T> T create(Class<T> itf, Locale locale, ClassLoader classLoader) throws IOException {
        return create(itf, locale, classLoader, (ResourceBundle) null);
    }


    public static <T> T create(Class<T> itf, Locale locale, ClassLoader classLoader, ResourceBundle bundle) throws IOException {
        String locStr = null;
        if (locale != null) {
            locStr = locale.getLanguage();
            if (locale.getCountry() != null && !EMPTY.equals(locale.getCountry())) {
                locStr += UNDERSCORE + locale.getCountry();
            }
            if (locale.getVariant() != null && !EMPTY.equals(locale.getVariant())) {
                locStr += UNDERSCORE + locale.getVariant();
            }
        }
        return create(itf, locStr, classLoader, bundle);
    }

    public static <T> T create(Class<T> itf, String lang, ClassLoader classLoader, ResourceBundle bundle) throws IOException {
        return create(itf, lang, classLoader, null, bundle);
    }

    public static <T> T create(Class<T> itf, String lang, ClassLoader classLoader, String prefix, ResourceBundle bundle) throws IOException {
        Locale locale = createLocaleFromLang(lang);
        final String key = ((prefix == null) ? EMPTY : prefix + HASH) + itf.getName() + ((lang == null) ? EMPTY : (UNDERSCORE + lang));
        return createProxyLocalBasedProxy(itf, locale.getLanguage(), locale.getCountry(), locale.getVariant(), classLoader, bundle);
    }

    private static <T> T createProxyLocalBasedProxy(Class<T> itf, String lang, String country, String variant, ClassLoader classLoader,
                                                    ResourceBundle bundle)
            throws IOException {
        InvocationHandler ih;
        ih = new InterfaceMessageTemplaterInvocationHandler(itf,
                new ClassBasedPropertiesFileMessageResolver(
                        new ClassLoaderBasedMessageStreamLoader(itf.getClassLoader()),
                        itf, lang, country, variant, null));
        return (T) Proxy.newProxyInstance(itf.getClassLoader(), new Class[]{itf}, ih);
    }

    public static Locale toLocale(final String str) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            // JDK 8 introduced an empty locale where all fields are blank
            return new Locale(EMPTY, EMPTY);
        }
        if (str.contains(HASH)) {
            // Cannot handle Java 7 script & extensions
            throw new IllegalArgumentException(INVALID_LOCALE_FORMAT + str);
        }
        final int len = str.length();
        if (len < 2) {
            throw new IllegalArgumentException(INVALID_LOCALE_FORMAT + str);
        }
        final char ch0 = str.charAt(0);
        if (ch0 == UNDERSCORE_CHAR) {
            if (len < LOC_END_INDEX) {
                throw new IllegalArgumentException(INVALID_LOCALE_FORMAT + str);
            }
            final char ch1 = str.charAt(1);
            final char ch2 = str.charAt(2);
            if (!Character.isUpperCase(ch1) || !Character.isUpperCase(ch2)) {
                throw new IllegalArgumentException(INVALID_LOCALE_FORMAT + str);
            }
            if (len == LOC_END_INDEX) {
                return new Locale(EMPTY, str.substring(1, LOC_END_INDEX));
            }
            if (len < LOC_MAX_LEN) {
                throw new IllegalArgumentException(INVALID_LOCALE_FORMAT + str);
            }
            if (str.charAt(LOC_END_INDEX) != UNDERSCORE_CHAR) {
                throw new IllegalArgumentException(INVALID_LOCALE_FORMAT + str);
            }
            return new Locale(EMPTY, str.substring(1, LOC_END_INDEX), str.substring(LOC_BEGIN_INDEX));
        }

        final String[] split = str.split(UNDERSCORE, -1);
        final int occurrences = split.length - 1;
        switch (occurrences) {
            case 0:
                if (CharMatcher.javaLowerCase().matchesAllOf(str) && (len == 2 || len == LOC_END_INDEX)) {
                    return new Locale(str);
                }
                throw new IllegalArgumentException(INVALID_LOCALE_FORMAT + str);

            case 1:
                if (CharMatcher.javaLowerCase().matchesAllOf(split[0])
                        && (split[0].length() == 2 || split[0].length() == LOC_END_INDEX)
                        && split[1].length() == 2 && CharMatcher.javaUpperCase().matchesAllOf(split[1])) {
                    return new Locale(split[0], split[1]);
                }
                throw new IllegalArgumentException(INVALID_LOCALE_FORMAT + str);

            case 2:
                if (CharMatcher.javaLowerCase().matchesAllOf(split[0])
                        && (split[0].length() == 2 || split[0].length() == LOC_END_INDEX)
                        && (split[1].length() == 0 || split[1].length() == 2 && CharMatcher.javaUpperCase().matchesAllOf(split[1]))
                        && split[2].length() > 0) {
                    return new Locale(split[0], split[1], split[2]);
                }
            //$FALL-THROUGH$
            default:
                throw new IllegalArgumentException(INVALID_LOCALE_FORMAT + str);
        }
    }
}
