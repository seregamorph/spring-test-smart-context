package com.github.seregamorph.testtimeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.UncheckedIOException;

/**
 * @author Sergey Chernov
 */
public final class JsonSerializers {

    private static final JsonMapper mapper = JsonMapper.builder()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .defaultPrettyPrinter(createPrettyPrinter())
        .findAndAddModules()
        .build();

    public static byte[] serialize(Object obj) {
        try {
            return mapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static DefaultPrettyPrinter createPrettyPrinter() {
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("  ", DefaultIndenter.SYS_LF);
        prettyPrinter.indentArraysWith(indenter);
        return prettyPrinter;
    }

    private JsonSerializers() {
    }
}
