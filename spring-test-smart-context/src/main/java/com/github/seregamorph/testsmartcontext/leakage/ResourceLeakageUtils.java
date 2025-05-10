package com.github.seregamorph.testsmartcontext.leakage;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 *
 * @author Sergey Chernov
 */
final class ResourceLeakageUtils {

    private static final Logger logger = LoggerFactory.getLogger(ResourceLeakageUtils.class);

    @Nullable
    static File getReportsBaseDir() {
        // todo target
        // "basedir" is provided by Maven, it's module root
        String basedirProperty = System.getProperty("basedir");
        if (basedirProperty == null) {
            return null;
        }

        File basedir = new File(basedirProperty, "leakage-detector");
        if ((basedir.mkdir() || basedir.exists()) && basedir.isDirectory()) {
            return basedir;
        }
        logger.warn("Failed to create {}", basedir);
        return null;
    }
}
