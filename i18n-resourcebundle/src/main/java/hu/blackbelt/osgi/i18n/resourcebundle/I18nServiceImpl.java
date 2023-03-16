package hu.blackbelt.osgi.i18n.resourcebundle;

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
import com.google.common.collect.Maps;
import hu.blackbelt.osgi.i18n.api.I18nService;
import hu.blackbelt.osgi.i18n.api.LocaleProvider;
import hu.blackbelt.osgi.i18n.api.MessageResolver;
import hu.blackbelt.osgi.i18n.api.MessageStreamLoader;
import hu.blackbelt.osgi.i18n.resourcebundle.base.InterfaceMessageTemplaterInvocationHandler;
import hu.blackbelt.osgi.i18n.resourcebundle.base.resolvers.ClassAndRequestContextLocaleBasedMessageResolver;
import hu.blackbelt.osgi.i18n.resourcebundle.utils.ClassLoaderBasedMessageStreamLoader;
import hu.blackbelt.osgi.i18n.resourcebundle.utils.SimpleParameterTemplater;
import hu.blackbelt.osgi.i18n.util.I18NUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

@Component(immediate = true)
@Designate(ocd = I18nServiceImpl.Config.class)
public class I18nServiceImpl implements I18nService {

    @ObjectClassDefinition
    public @interface Config {

        @AttributeDefinition(name = "Default locale")
        String defaultLocale() default "";
    }

    private BundleContext context;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    LocaleProvider localeProvider;

    private Locale defaultLocale;

    private Supplier<Locale> localeSupplier = () -> localeProvider != null && localeProvider.getLocale().isPresent() ? localeProvider.getLocale().get() : (defaultLocale != null ? defaultLocale : Locale.getDefault());

    private final Map<Class, ServiceRegistration> registrationMap = Maps.newConcurrentMap();
    private final Map<Class, MessageResolver> messageResolverMap = Maps.newConcurrentMap();

    @Activate
    void activate(BundleContext context, Config config) {
        this.context = context;
        defaultLocale = I18NUtil.getLocaleFromBCP47(config.defaultLocale());
    }

    @Deactivate
    void deactivate() {
        context = null;
    }

    @Override
    public <T> T register(Class<T> clazz) {
        MessageStreamLoader messageStreamLoader = new ClassLoaderBasedMessageStreamLoader(clazz.getClassLoader());
        ClassAndRequestContextLocaleBasedMessageResolver messageResolver =
                new ClassAndRequestContextLocaleBasedMessageResolver(localeSupplier, clazz, messageStreamLoader, null);
        InvocationHandler invocationHandler = new InterfaceMessageTemplaterInvocationHandler(clazz, messageResolver);
        T proxy =  (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, invocationHandler);
        registrationMap.put(clazz, context.registerService(clazz.getName(), proxy, new Hashtable<String, Object>()));
        messageResolverMap.put(clazz, messageResolver);
        return proxy;
    }

    @Override
    public <T> void unregister(Class<T> clazz) {
        ServiceRegistration registration = registrationMap.get(clazz);
        registrationMap.remove(clazz);
        messageResolverMap.remove(clazz);
        registration.unregister();
    }


    @Override
    public String getTemplate(String key) {
        // Iterate over registered message keys
        for (MessageResolver resolver : messageResolverMap.values()) {
            String template = resolver.get(key);
            if (!Strings.isNullOrEmpty(template)) {
                return template;
            }
        }
        return null;
        // return SimpleParameterTemplater.templateMessage(SimpleParameterTemplater.generateAutoTemplate(key, params), params);
    }

    @Override
    public String format(String key, Object...params) {
        String template = getTemplate(key);
        if (!Strings.isNullOrEmpty(template)) {
            return SimpleParameterTemplater.templateMessage(template, params);
        }
        return null;
    }
}
