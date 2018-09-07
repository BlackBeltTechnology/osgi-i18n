package hu.blackbelt.osgi.i18n.resourcebundle.utils;

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
