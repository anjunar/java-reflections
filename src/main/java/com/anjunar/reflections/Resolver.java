package com.anjunar.reflections;

import com.anjunar.reflections.nodes.FullScanVisitor;
import com.anjunar.reflections.types.ClassSymbol;
import com.google.common.collect.ImmutableSortedMap;

import java.util.*;
import java.util.function.Function;

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

    public Universe universe() {
        return new Universe(classes);
    }

    public static class Universe {

        private final Map<Class<?>, ClassSymbol> classes;

        public Universe(Map<Class<?>, ClassSymbol> classes) {
            final Set<ClassSymbol> result = new HashSet<>();

            for (ClassSymbol symbol : classes.values()) {
                symbol.accept(new FullScanVisitor() {
                    @Override
                    public void visit(ClassSymbol symbol) {
                        if (! cache.contains(symbol)) {
                            result.add(symbol);
                            super.visit(symbol);
                        }
                    }
                });
            }

            Comparator<Class<?>> comparator = Comparator.comparing(Class::getName);
            Function<ClassSymbol, Class<?>> keyFunction = ClassSymbol::getUnderlying;
            Function<ClassSymbol, ClassSymbol> valueFunction = Function.identity();
            this.classes = result.stream().collect(ImmutableSortedMap.toImmutableSortedMap(comparator, keyFunction, valueFunction));

        }

        public Collection<ClassSymbol> isExtendingFrom(ClassSymbol aClass) {
            return classes.values()
                    .stream()
                    .filter(classSymbol -> aClass.isAssignableFrom(classSymbol) && ! aClass.equals(classSymbol))
                    .toList();
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
}
