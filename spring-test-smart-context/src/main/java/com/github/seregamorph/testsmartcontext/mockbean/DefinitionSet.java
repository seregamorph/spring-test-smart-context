package com.github.seregamorph.testsmartcontext.mockbean;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

class DefinitionSet implements Iterable<Definition> {

    private final Set<Definition> definitions = new LinkedHashSet<>();

    DefinitionSet() {
    }

    DefinitionSet(DefinitionSet definitions) {
        addAll(definitions);
    }

    @Override
    public Iterator<Definition> iterator() {
        return this.definitions.iterator();
    }

    void addAll(DefinitionSet definitions) {
        this.definitions.addAll(definitions.definitions);
    }

    boolean add(Definition definition) {
        return this.definitions.add(definition);
    }

    boolean isEmpty() {
        return this.definitions.isEmpty();
    }
}
