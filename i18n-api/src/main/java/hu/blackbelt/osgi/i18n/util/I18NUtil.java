package hu.blackbelt.osgi.i18n.util;

/*-
 * #%L
 * OSGi I18N API
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

import java.util.Locale;

public final class I18NUtil {

    public static Locale getLocaleFromBCP47(String localeString) {
        // Locale.toString() generates strings like "en_US" and "zh_CN_#Hans".
        // Locale.toLanguageTag() generates strings like "en-US" and "zh-Hans-CN".
        // We can only parse language tags.
        if (localeString != null && localeString.indexOf('_') == -1) {
            final String[] locales = localeString.split(";");
            return Locale.forLanguageTag(locales[0]);
        }
        return null;
    }
}
