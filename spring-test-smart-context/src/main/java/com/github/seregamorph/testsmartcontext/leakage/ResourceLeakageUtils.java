package com.github.seregamorph.testsmartcontext.leakage;

import java.io.File;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;

final class ResourceLeakageUtils {

    private static final Log log = LogFactory.getLog(ResourceLeakageUtils.class);

    @Nullable
    static File getReportsBaseDir() {
        // todo target
        // "basedir" is provided by Maven
        String basedirProperty = System.getProperty("basedir");
        if (basedirProperty == null) {
            return null;
        }

        File basedir = new File(basedirProperty, "leakage-detector");
        if ((basedir.mkdir() || basedir.exists()) && basedir.isDirectory()) {
            return basedir;
        }
        log.warn("Failed to create " + basedir);
        return null;
    }
}
