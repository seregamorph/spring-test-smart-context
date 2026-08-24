package com.github.seregamorph.testtimeline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author Sergey Chernov
 */
final class MoreFileUtils {

    static void write(File file, byte[] bytes) {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private MoreFileUtils() {
    }
}
