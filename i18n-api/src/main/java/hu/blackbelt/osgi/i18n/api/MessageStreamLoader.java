package hu.blackbelt.osgi.i18n.api;

import java.io.InputStream;

/**
 * Load the stream for the given name.
 */
public interface MessageStreamLoader {
    InputStream load(String name);
}
