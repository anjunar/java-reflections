package com.anjunar.reflections;

import com.anjunar.reflections.bean.BeanModel;
import com.anjunar.reflections.nodes.FullScanVisitor;
import com.anjunar.reflections.types.ClassSymbol;
import com.google.common.collect.ImmutableSortedMap;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;

public class Resolver {

    private final Map<Class<?>, ClassSymbol> classes;

    private Universe universe;

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
        if (Objects.isNull(universe)) {
            universe = new Universe(classes);
        }
        return universe;
    }

    public static class Universe {

        private final Map<Class<?>, ClassSymbol> classes;

        private final Map<Class<?>, BeanModel> beans;

        public Universe(Map<Class<?>, ClassSymbol> classes) {
            final Set<ClassSymbol> result = new HashSet<>();

            for (ClassSymbol symbol : classes.values()) {
                symbol.accept(new FullScanVisitor() {
                    @Override
                    public void visit(ClassSymbol symbol) {
                        if (!cache.contains(symbol)) {
                            result.add(symbol);
                            super.visit(symbol);
                        }
                    }
                });
            }

            this.classes = result.stream()
                    .collect(ImmutableSortedMap.toImmutableSortedMap(
                            Comparator.comparing(Class::getName),
                            ClassSymbol::getUnderlying,
                            Function.identity()
                    ));

            this.beans = symbols().stream()
                    .map(BeanModel::new)
                    .collect(ImmutableSortedMap.toImmutableSortedMap(
                            Comparator.comparing(Class::getName),
                            bean -> Utils.getRawType(bean.getSymbol().getUnderlying()),
                            Function.identity()
                    ));

        }

        public Collection<ClassSymbol> isExtendingFrom(ClassSymbol aClass) {
            return classes.values()
                    .stream()
                    .filter(classSymbol -> aClass.isAssignableFrom(classSymbol) && !aClass.equals(classSymbol))
                    .toList();
        }

        public ClassSymbol findClass(Class<?> key) {
            return classes.get(key);
        }

        public Set<Class<?>> classes() {
            return classes.keySet();
        }

        public Collection<ClassSymbol> symbols() {
            return classes.values();
        }

        public BeanModel findBean(Class<?> key) {
            return beans.get(key);
        }

        public Collection<BeanModel> beans() {
            return beans.values();
        }

    }
}
