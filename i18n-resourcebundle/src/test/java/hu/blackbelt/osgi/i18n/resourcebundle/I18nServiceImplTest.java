package hu.blackbelt.osgi.i18n.resourcebundle;

import hu.blackbelt.osgi.i18n.api.LocaleProvider;
import hu.blackbelt.osgi.i18n.messages.FooMessages;
import hu.blackbelt.osgi.utils.test.MockOsgi;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Dictionary;
import java.util.Locale;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class I18nServiceImplTest {

    @InjectMocks
    private I18nServiceImpl target = new I18nServiceImpl();

    @Mock
    private LocaleProvider localeProviderMock;

    @Mock
    private BundleContext bundleContextMock;

    @Mock
    private ServiceRegistration serviceRegistrationMock;

    FooMessages msg;

    @Before
    public void setUp() {
        I18nServiceImpl.Config config = Mockito.mock(I18nServiceImpl.Config.class);
        Mockito.when(config.defaultLocale()).thenReturn("hu-HU");

        MockitoAnnotations.initMocks(this);

        Mockito.when(bundleContextMock.registerService(Mockito.any(String.class), Mockito.any(), Mockito.any(Dictionary.class)))
                .thenReturn(serviceRegistrationMock);

        MockOsgi.setReferences(target, localeProviderMock);
        target.activate(bundleContextMock, config);
        msg = target.register(FooMessages.class);
    }

    @Test
    public void testDefaultWithDefaultLocale() {
        Mockito.when(localeProviderMock.getLocale()).thenReturn(Optional.empty());
        assertHungarian();
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
        assertEquals("Egy üzenet test_None()", msg.test_None());
        assertEquals("Egy üzenet test_String(azz)", msg.test_String("azz"));
        assertEquals("Egy üzenet test_byte(1)", msg.test_byte(new Integer(1).byteValue()));
        assertEquals("Egy üzenet test_Byte(2)", msg.test_Byte(new Integer(2).byteValue()));
        assertEquals("Egy üzenet test_short(3)", msg.test_short(new Integer(3).shortValue()));
        assertEquals("Egy üzenet test_Short(3)", msg.test_Short(new Integer(3).shortValue()));
        assertEquals("Egy üzenet test_int(4)", msg.test_int(4));
        assertEquals("Egy üzenet test_Integer(4)", msg.test_Integer(4));
        assertEquals("Egy üzenet test_long(5)", msg.test_long(5L));
        assertEquals("Egy üzenet test_Long(5)", msg.test_Long(5L));
        assertEquals("Egy üzenet test_float(6.0)", msg.test_float(6f));
        assertEquals("Egy üzenet test_Float(6.0)", msg.test_Float(6f));
        assertEquals("Egy üzenet test_double(7.0)", msg.test_double(new Double(7f)));
        assertEquals("Egy üzenet test_Double(7.0)", msg.test_Double(new Double(7f)));
        assertEquals("Egy üzenet test_char(b)", msg.test_char('b'));
        assertEquals("Egy üzenet test_Character(b)", msg.test_Character('b'));
        assertEquals("Egy üzenet test_Object(object)", msg.test_Object("object"));
        assertEquals("Egy üzenet test_2param(p1,2)", msg.test_2param("p1", 2));
        assertEquals("Egy üzenet test_3param(p1,2,p3)", msg.test_3param("p1", 2, "p3"));
        assertEquals("test_undefined_3param p1 2 p3", msg.test_undefined_3param("p1", 2, "p3"));
        assertEquals("The message is test_3param(p1,2,p3)", msg.test_default_3param("p1", 2, "p3"));
        assertEquals("Egy üzenet test_3param(p1,2,p3)", msg.getMessage("test_3param", "p1", 2, "p3"));
    }

    public void assertEnglish() {
        assertEquals("The message is test_None()", msg.test_None());
        assertEquals("The message is test_String(azz)", msg.test_String("azz"));
        assertEquals("The message is test_byte(1)", msg.test_byte(new Integer(1).byteValue()));
        assertEquals("The message is test_Byte(2)", msg.test_Byte(new Integer(2).byteValue()));
        assertEquals("The message is test_short(3)", msg.test_short(new Integer(3).shortValue()));
        assertEquals("The message is test_Short(3)", msg.test_Short(new Integer(3).shortValue()));
        assertEquals("The message is test_int(4)", msg.test_int(4));
        assertEquals("The message is test_Integer(4)", msg.test_Integer(4));
        assertEquals("The message is test_long(5)", msg.test_long(5L));
        assertEquals("The message is test_Long(5)", msg.test_Long(5L));
        assertEquals("The message is test_float(6.0)", msg.test_float(6f));
        assertEquals("The message is test_Float(6.0)", msg.test_Float(6f));
        assertEquals("The message is test_double(7.0)", msg.test_double(new Double(7f)));
        assertEquals("The message is test_Double(7.0)", msg.test_Double(new Double(7f)));
        assertEquals("The message is test_char(b)", msg.test_char('b'));
        assertEquals("The message is test_Character(b)", msg.test_Character('b'));
        assertEquals("The message is test_Object(object)", msg.test_Object("object"));
        assertEquals("The message is test_2param(p1,2)", msg.test_2param("p1", 2));
        assertEquals("The message is test_3param(p1,2,p3)", msg.test_3param("p1", 2, "p3"));
        assertEquals("test_undefined_3param p1 2 p3", msg.test_undefined_3param("p1", 2, "p3"));
        assertEquals("The message is test_3param(p1,2,p3)", msg.test_default_3param("p1", 2, "p3"));
        assertEquals("The message is test_3param(p1,2,p3)", msg.getMessage("test_3param", "p1", 2, "p3"));
    }
}
