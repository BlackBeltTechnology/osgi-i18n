package hu.blackbelt.osgi.i18n.messages;

import hu.blackbelt.osgi.i18n.api.Message;
import hu.blackbelt.osgi.i18n.api.MessageByKey;

public interface FooMessages {

    String test_None();
    
    String test_String(String s);
    
    String test_byte(byte s);

    String test_Byte(Byte s);
    
    String test_short(short s);

    String test_Short(Short s);

    String test_int(int i);

    String test_Integer(Integer i);
    
    String test_long(long i);

    String test_Long(Long i);
    
    String test_float(float i);

    String test_Float(Float i);
    
    String test_double(double i);

    String test_Double(Double i);
    
    String test_char(char i);

    String test_Character(Character i);
    
    String test_Object(Object i);
    
    String test_2param(String p1, int p2);

    String test_3param(String p1, int p2, String p3);

    String test_undefined_3param(String p1, int p2, String p3);

    @Message(value = "The message is test_3param({0},{1},{2})")
    String test_default_3param(String p1, int p2, String p3);

    @MessageByKey
    String getMessage(String key, Object... parameters);

}
