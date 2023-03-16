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

public final class SimpleParameterTemplater {
    private SimpleParameterTemplater() {
    }

    public static String generateAutoTemplate(String prefix, Object...args) {
        String template = prefix;
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                template = template + " {" + i + "}";
            }
        }
        return template;
    }

    public static String templateMessage(String tmpl, Object...args) {
        String template = tmpl;
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                String value = args[i] == null ? "null" : args[i].toString();
                String replacedPattern = null;
                replacedPattern = "\\{" + i + "\\}";
                if (replacedPattern != null) {
                    template = template.replaceAll(replacedPattern, value == null ? "" : value);
                }
            }
        }
        return template;
    }

}
