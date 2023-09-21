package com.anjunar.reflections;

import com.anjunar.reflections.types.ClassSymbol;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class Resolver {

    private final Map<Class<?>, ClassSymbol> classes;

    public Resolver(Map<Class<?>, ClassSymbol> classes) {
        this.classes = classes;
    }

    public ClassSymbol get(Class<?> key) {
        return classes.get(key);
    }

    public Set<Class<?>> classes() {
        return classes.keySet();
    }

    public Collection<ClassSymbol> symbols() {
        return classes.values();
    }
}
