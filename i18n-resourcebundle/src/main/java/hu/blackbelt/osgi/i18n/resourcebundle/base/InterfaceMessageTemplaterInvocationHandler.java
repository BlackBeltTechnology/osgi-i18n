package hu.blackbelt.osgi.i18n.resourcebundle.base;

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
import hu.blackbelt.osgi.i18n.api.Message;
import hu.blackbelt.osgi.i18n.api.MessageByKey;
import hu.blackbelt.osgi.i18n.api.MessageResolver;
import hu.blackbelt.osgi.i18n.resourcebundle.utils.SimpleParameterTemplater;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class InterfaceMessageTemplaterInvocationHandler implements InvocationHandler {

    private MessageResolver messageResolver;
    private Class<?> messageInterface;

    public InterfaceMessageTemplaterInvocationHandler(Class<?> itf, MessageResolver mr)  {
        this.messageResolver = mr;
        this.messageInterface = itf;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!String.class.equals(method.getReturnType())) {
            return "Invalid return type of the method " + method.toString();
        }
        return buildMessage(method.getName(), method, args);
    }

    private String buildMessage(String propertyName, Method method, Object[] argsPar)  throws Throwable {
        String propertyNameToResolve = propertyName;
        Object[] templateParameters = argsPar;
        if (method.isAnnotationPresent(MessageByKey.class)) {
            if (argsPar.length < 2) {
                return "The @MessageResolver annotated class have to defined to arguments, Stirng and Object[] - " + method.toString();
            }
            if (!(argsPar[0] instanceof String)) {
                return "Key parameter have to be String " + method.toString();
            }
            if (!(argsPar[1] instanceof Object[])) {
                return "Template parameter have to be Object[] " + method.toString();
            }

            propertyNameToResolve = (String) argsPar[0];
            templateParameters = (Object[]) argsPar[1];
        }

        String template = messageResolver.get(propertyNameToResolve);
        if (Strings.isNullOrEmpty(template)) {
            Message m = method.getAnnotation(Message.class);
            if (m != null) {
                template = m.value();
                String key = m.key();
                if (!Strings.isNullOrEmpty(key)) {
                    template = messageResolver.get(key);
                }
            }
        }
        if (Strings.isNullOrEmpty(template)) {
            template = SimpleParameterTemplater.generateAutoTemplate(method.getName(), templateParameters);
        }
        return SimpleParameterTemplater.templateMessage(template, templateParameters);
    }

    public static boolean isA(Class<?> c1, Class<?> c2) {
        return c2.isAssignableFrom(c1);
    }

    <T extends Annotation> T getAnnotation(Annotation[] as, Class<T> annotation) {
        for (Annotation a : as) {
            if (isA(a.getClass(), annotation)) {
                return (T) a;
            }
        }
        return null;
    }

    <T extends Annotation> List<T> getAnnotations(Annotation[] as, Class<T> a) {
        List<T> foundAnnotations = new ArrayList<T>();
        for (Annotation current : as) {
            if (current.getClass().equals(a.getClass())) {
                foundAnnotations.add((T) current);
            }
        }
        return foundAnnotations;
    }

}
