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

import hu.blackbelt.osgi.i18n.api.MessageStreamLoader;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.Bundle;

import java.io.IOException;
import java.io.InputStream;

/**
 * Loads message as stream from OSGi bundle.
 */
@Slf4j
public class BundleMessageStreamLoader implements MessageStreamLoader {
    private Bundle bundle;

    public BundleMessageStreamLoader(Bundle bnd) {
        this.bundle = bnd;
    }

    @Override
    public InputStream load(String name) {
        InputStream in = null;
        try {
            in = bundle.getEntry(name).openStream();
        } catch (IOException exception) {
            log.info(String.format("Message %s not found in bundle", name, bundle.toString()));
        }
        return in;
    }
}
