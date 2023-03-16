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

public interface EnumI18nService {

    /**
     * Register an Enum i18N message.
     *
     * @param clazz
     */
    void register(Class<? extends Enum> clazz);

    /**
     * Unregister an Enm i18n message.
     * @param clazz
     */
    void unregister(Class<? extends Enum> clazz);

    String getMessageForEnum(Enum entry, Object... args);

}
