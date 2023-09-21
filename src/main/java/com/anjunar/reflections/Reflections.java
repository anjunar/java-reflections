package com.anjunar.reflections;

import com.anjunar.reflections.nodes.FullScanVisitor;
import com.anjunar.reflections.types.ClassSymbol;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Reflections {

    public static Resolver init(Set<Class<?>> classes) {

        List<ClassSymbol> symbols = classes.stream()
                .map(clazz -> ClassSymbol.newInstance(clazz, null))
                .toList();

        final Set<ClassSymbol> resolved = new HashSet<>();

        for (ClassSymbol symbol : symbols) {
            symbol.accept(new FullScanVisitor() {
                @Override
                public void visit(ClassSymbol symbol) {
                    if (! cache.contains(symbol)) {
                        resolved.add(symbol);
                        super.visit(symbol);
                    }
                }
            });
        }

        return new Resolver(resolved.stream().collect(Collectors.toMap(ClassSymbol::getUnderlying, Function.identity())));
    }
}
