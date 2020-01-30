package hu.blackbelt.osgi.i18n.resourcebundle.utils;

import hu.blackbelt.osgi.i18n.api.MessageStreamLoader;

import java.io.InputStream;

/**
 * Loades message file from given classloader.
 */
public class ClassLoaderBasedMessageStreamLoader implements MessageStreamLoader {
    ClassLoader classLoader;

    public ClassLoaderBasedMessageStreamLoader(ClassLoader cl) {
        this.classLoader = cl;
    }

    @Override
    public InputStream load(String name) {
        InputStream in = null;
        ClassLoader cl = classLoader;
        if (cl != null) {
            in = classLoader.getResourceAsStream(name);
        }

        if (in == null) {
            cl = Thread.currentThread().getContextClassLoader();
            if (cl != null) {
                in = cl.getResourceAsStream(name);
            }
            if (in == null) {
                cl = getClass().getClassLoader();
                if (cl != null) {
                    in = getClass().getClassLoader().getResourceAsStream(name);
                }
            }
            if (in == null) {
                cl = ClassLoader.getSystemClassLoader();
                if (cl != null) {
                    in = cl.getResourceAsStream(name);
                }
            }
        }
        return in;
    }
}
