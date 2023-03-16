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

import hu.blackbelt.osgi.i18n.api.LocaleProvider;
import hu.blackbelt.osgi.i18n.messages.EnumMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Locale;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class EnumI18nServiceImplTest {

    @InjectMocks
    private EnumI18nServiceImpl target;

    @Mock
    private LocaleProvider localeProviderMock;

    @Before
    public void setUp() {
        EnumI18nServiceImpl.Config config = Mockito.mock(EnumI18nServiceImpl.Config.class);
        Mockito.when(config.defaultLocale()).thenReturn("en-US");

        target.activate(config);
        target.register(EnumMessage.class);
    }

    @Test
    public void testDefaultWithDefaultLocale() {
        Mockito.when(localeProviderMock.getLocale()).thenReturn(Optional.empty());
        assertEnglish();
    }

    @Test
    public void testDefaultWithEnglish() {
        Mockito.when(localeProviderMock.getLocale()).thenReturn(Optional.of(new Locale("en", "US")));
        assertEnglish();
    }

    @Test
    public void testDefaultWithHungarian() {
        Mockito.when(localeProviderMock.getLocale()).thenReturn(Optional.of(new Locale("hu", "HU")));
        assertHungarian();
    }

    @Test
    public void testChangeLanguage() {
        Mockito.when(localeProviderMock.getLocale()).thenReturn(Optional.of(new Locale("hu", "HU")));
        assertHungarian();
        Mockito.when(localeProviderMock.getLocale()).thenReturn(Optional.of(new Locale("en", "US")));
        assertEnglish();
    }

    public void assertHungarian() {
        assertEquals("Test1 üzenet", target.getMessageForEnum(EnumMessage.TEST1));
        assertEquals("Egy üzenet TEST2(p1,2,p3)", target.getMessageForEnum(EnumMessage.TEST2, "p1", 2, "p3"));
        assertEquals("TEST3", target.getMessageForEnum(EnumMessage.TEST3));
    }

    public void assertEnglish() {
        assertEquals("Test1 message", target.getMessageForEnum(EnumMessage.TEST1));
        assertEquals("The message is TEST2(p1,2,p3)", target.getMessageForEnum(EnumMessage.TEST2, "p1", 2, "p3"));
        assertEquals("TEST3", target.getMessageForEnum(EnumMessage.TEST3));
    }
}
