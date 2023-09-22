package com.anjunar.reflections;

import com.anjunar.reflections.nodes.FullScanVisitor;
import com.anjunar.reflections.types.ClassSymbol;
import com.google.common.collect.ImmutableSortedMap;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Reflections {

    public static Resolver init(Set<Class<?>> classes) {

        List<ClassSymbol> symbols = classes.stream()
                .map(clazz -> ClassSymbol.newInstance(clazz, null))
                .toList();

        Comparator<Class<?>> comparator = Comparator.comparing(Class::getName);
        Function<ClassSymbol, Class<?>> keyFunction = ClassSymbol::getUnderlying;
        Function<ClassSymbol, ClassSymbol> valueFunction = Function.identity();
        return new Resolver(symbols.stream().collect(ImmutableSortedMap.toImmutableSortedMap(comparator, keyFunction, valueFunction)));
    }
}
