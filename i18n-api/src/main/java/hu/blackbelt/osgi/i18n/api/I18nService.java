package hu.blackbelt.osgi.i18n.api;

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

public interface I18nService {

    /**
     * Register an i18N proxy interface to OSGi service registry. The proxy methods resolved as string to access i18n data.
     *
     * @param clazz
     */
    <T> T register(Class<T> clazz);

    /**
     * Unregister an i18N proxy interface from  OSGi service registry.
     * @param clazz
     */
    <T> void unregister(Class<T> clazz);


    /**
     * Get formatted, localized message.
     * @param key
     * @param params
     */
    String format(String key, Object... params);

    /**
     * Get messgae template.
     * @param key
     */
    String getTemplate(String key);

}
